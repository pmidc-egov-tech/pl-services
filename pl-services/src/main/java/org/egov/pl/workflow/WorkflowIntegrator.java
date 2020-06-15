package org.egov.pl.workflow;

import static org.egov.pl.util.PLConstants.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String TENANTIDKEY = "tenantId";

	private static final String BUSINESSSERVICEKEY = "businessService";

	private static final String ACTIONKEY = "action";

	private static final String COMMENTKEY = "comment";

	private static final String MODULENAMEKEY = "moduleName";

	private static final String BUSINESSIDKEY = "businessId";

	private static final String DOCUMENTSKEY = "documents";

	private static final String ASSIGNEEKEY = "assignes";

	private static final String UUIDKEY = "uuid";

	private static final String PLMODULENAMEVALUE = "PL";

	private static final String BPAMODULENAMEVALUE = "BPAREG";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	private RestTemplate rest;

	private PLConfiguration config;

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromPetTypeOnly;

	@Autowired
	public WorkflowIntegrator(RestTemplate rest, PLConfiguration config) {
		this.rest = rest;
		this.config = config;
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the trade-license request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to trade-license object
	 *
	 * @param petLicenseRequest
	 */
	public void callWorkFlow(PetLicenseRequest petLicenseRequest) {

		String wfTenantId = petLicenseRequest.getLicenses().get(0).getTenantId();
		String businessServiceFromMDMS = petLicenseRequest.getLicenses().isEmpty() ? null
				: petLicenseRequest.getLicenses().get(0).getBusinessService();
		if (businessServiceFromMDMS == null)
			businessServiceFromMDMS = businessService_PL;
		JSONArray array = new JSONArray();
		for (PetLicense license : petLicenseRequest.getLicenses()) {
			if ((businessServiceFromMDMS.equals(businessService_PL))
					|| (!license.getAction().equalsIgnoreCase(TRIGGER_NOWORKFLOW))) {
				JSONObject obj = new JSONObject();

				List<Map<String, String>> uuidmaps = new LinkedList<>();

				if (!CollectionUtils.isEmpty(license.getAssignee())) {

					// Adding assignes to processInstance
					license.getAssignee().forEach(assignee -> {
						Map<String, String> uuidMap = new HashMap<>();
						uuidMap.put(UUIDKEY, assignee);
						uuidmaps.add(uuidMap);
					});

				}

				obj.put(BUSINESSIDKEY, license.getApplicationNumber());
				obj.put(TENANTIDKEY, wfTenantId);
				switch (businessServiceFromMDMS) {
				case businessService_PL:
					obj.put(BUSINESSSERVICEKEY, config.getPlBusinessServiceValue());
					obj.put(MODULENAMEKEY, PLMODULENAMEVALUE);
					break;

				}
				obj.put(ACTIONKEY, license.getAction());
				obj.put(COMMENTKEY, license.getComment());
				if (!CollectionUtils.isEmpty(license.getAssignee()))
					obj.put(ASSIGNEEKEY, uuidmaps);
				obj.put(DOCUMENTSKEY, license.getWfDocuments());
				array.add(obj);
			}
		}
		if (!array.isEmpty()) {
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, petLicenseRequest.getRequestInfo());
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()), workFlowRequest,
						String.class);
			} catch (HttpClientErrorException e) {

				/*
				 * extracting message from client error exception
				 */
				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					log.error("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			/*
			 * on success result from work-flow read the data and set the status back to TL
			 * object
			 */
			DocumentContext responseContext = JsonPath.parse(response);
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			Map<String, String> idStatusMap = new HashMap<>();
			responseArray.forEach(object -> {

				DocumentContext instanceContext = JsonPath.parse(object);
				idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
			});

			// setting the status back to PL object from wf response
			petLicenseRequest.getLicenses()
					.forEach(plObj -> plObj.setStatus(idStatusMap.get(plObj.getApplicationNumber())));
		}
	}
}