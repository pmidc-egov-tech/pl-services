package org.egov.pl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.models.PetLicenseSearchCriteria;
import org.egov.pl.repository.PLRepository;
import org.egov.pl.util.AnimalCategoryUtil;
import org.egov.pl.web.models.collection.PaymentDetail;
import org.egov.pl.web.models.collection.PaymentRequest;
import org.egov.pl.web.models.workflow.BusinessService;
import org.egov.pl.workflow.WorkflowIntegrator;
import org.egov.pl.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.pl.util.PLConstants.*;


@Service
@Slf4j
public class PaymentUpdateService {

	private PetLicenseService petLicenseService;

	private PLConfiguration config;

	private PLRepository repository;

	private WorkflowIntegrator wfIntegrator;

	private EnrichmentService enrichmentService;

	private ObjectMapper mapper;

	private WorkflowService workflowService;

	private AnimalCategoryUtil util;

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromTradeTypeOnly;

	@Autowired
	public PaymentUpdateService(PetLicenseService petLicenseService, PLConfiguration config, PLRepository repository,
								WorkflowIntegrator wfIntegrator, EnrichmentService enrichmentService, ObjectMapper mapper,
								WorkflowService workflowService,AnimalCategoryUtil util) {
		this.petLicenseService = petLicenseService;
		this.config = config;
		this.repository = repository;
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.mapper = mapper;
		this.workflowService = workflowService;
		this.util = util;
	}




	final String tenantId = "tenantId";

	final String businessService = "businessService";

	final String consumerCode = "consumerCode";

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record,PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			String tenantId = paymentRequest.getPayment().getTenantId();
			for(PaymentDetail paymentDetail : paymentDetails){
				if (paymentDetail.getBusinessService().equalsIgnoreCase(businessService_PL) || paymentDetail.getBusinessService().equalsIgnoreCase(businessService_BPA)) {
					PetLicenseSearchCriteria searchCriteria = new PetLicenseSearchCriteria();
					searchCriteria.setTenantId(tenantId);
					searchCriteria.setApplicationNumber(paymentDetail.getBill().getConsumerCode());
					searchCriteria.setBusinessService(paymentDetail.getBusinessService());
					List<PetLicense> licenses = petLicenseService.getLicensesWithOwnerInfo(searchCriteria, requestInfo);
					String wfbusinessServiceName = null;
					switch (paymentDetail.getBusinessService()) {
						case businessService_PL:
							wfbusinessServiceName = config.getPlBusinessServiceValue();
							break;

//						case businessService_BPA:
//							String tradeType = licenses.get(0).getPetLicenseDetail().getTradeUnits().get(0).getTradeType();
//							if (pickWFServiceNameFromTradeTypeOnly)
//								tradeType = tradeType.split("\\.")[0];
//							wfbusinessServiceName = tradeType;
//							break;
					}
				BusinessService businessService = workflowService.getBusinessService(licenses.get(0).getTenantId(), requestInfo,wfbusinessServiceName);


					if (CollectionUtils.isEmpty(licenses))
						throw new CustomException("INVALID RECEIPT",
								"No tradeLicense found for the comsumerCode " + searchCriteria.getApplicationNumber());

					licenses.forEach(license -> license.setAction(ACTION_PAY));

					// FIXME check if the update call to repository can be avoided
					// FIXME check why aniket is not using request info from consumer
					// REMOVE SYSTEM HARDCODING AFTER ALTERING THE CONFIG IN WF FOR PL

					Role role = Role.builder().code("SYSTEM_PAYMENT").tenantId(licenses.get(0).getTenantId()).build();
					requestInfo.getUserInfo().getRoles().add(role);
					PetLicenseRequest updateRequest = PetLicenseRequest.builder().requestInfo(requestInfo)
							.licenses(licenses).build();

					/*
					 * calling workflow to update status
					 */
					wfIntegrator.callWorkFlow(updateRequest);

					updateRequest.getLicenses()
							.forEach(obj -> log.info(" the status of the application is : " + obj.getStatus()));

					List<String> endStates = Collections.nCopies(updateRequest.getLicenses().size(), STATUS_APPROVED);
					switch (paymentDetail.getBusinessService()) {
						case businessService_BPA:
							endStates = util.getBPAEndState(updateRequest);
							break;
					}
					enrichmentService.postStatusEnrichment(updateRequest,endStates);

					/*
					 * calling repository to update the object in PL tables
					 */
					Map<String,Boolean> idToIsStateUpdatableMap = util.getIdToIsStateUpdatableMap(businessService,licenses);
					repository.update(updateRequest,idToIsStateUpdatableMap);
			}
		 }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Extracts the required fields as map
	 * 
	 * @param context The documentcontext of the incoming receipt
	 * @return Map containing values of required fields
	 */
	private Map<String, String> enrichValMap(DocumentContext context) {
		Map<String, String> valMap = new HashMap<>();
		try {
			valMap.put(businessService, context.read("$.Payments.*.paymentDetails[?(@.businessService=='PL')].businessService"));
			valMap.put(consumerCode, context.read("$.Payments.*.paymentDetails[?(@.businessService=='PL')].bill.consumerCode"));
			valMap.put(tenantId, context.read("$.Payments[0].tenantId"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("PAYMENT ERROR", "Unable to fetch values from payment");
		}
		return valMap;
	}

}
