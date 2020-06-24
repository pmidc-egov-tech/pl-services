package org.egov.pl.service;

import static org.egov.pl.util.PLConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.OwnerInfo;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.repository.PLRepository;
import org.egov.pl.service.notification.EditNotificationService;
import org.egov.pl.util.AnimalCategoryUtil;
import org.egov.pl.validator.PLValidator;
import org.egov.pl.web.models.user.UserDetailResponse;
import org.egov.pl.web.models.workflow.BusinessService;
import org.egov.pl.workflow.ActionValidator;
import org.egov.pl.workflow.PLWorkflowService;
import org.egov.pl.workflow.WorkflowIntegrator;
import org.egov.pl.workflow.WorkflowService;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.repository.PLRepository;
import org.egov.pl.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PetLicenseService {

	private WorkflowIntegrator wfIntegrator;

	private UserService userService;

	private PLRepository repository;

	private ActionValidator actionValidator;

	private PLValidator plValidator;

	private PLWorkflowService pLWorkflowService;

	private PLConfiguration config;

	private WorkflowService workflowService;

	private AnimalCategoryUtil animalCategoryUtil;
	
	private CalculationService calculationService;
	
	private AnimalCategoryUtil util;
	
	private EnrichmentService enrichmentService;
	
	private DiffService diffService;
	
	private EditNotificationService editNotificationService;

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromTradeTypeOnly;

	@Autowired
	public PetLicenseService(WorkflowIntegrator wfIntegrator, EnrichmentService enrichmentService,
			UserService userService, PLRepository repository, ActionValidator actionValidator, PLValidator plValidator,
			PLWorkflowService pLWorkflowService, CalculationService calculationService, AnimalCategoryUtil util,
			DiffService diffService, PLConfiguration config, EditNotificationService editNotificationService,
			WorkflowService workflowService, AnimalCategoryUtil animalCategoryUtil) {
		this.wfIntegrator = wfIntegrator;
		this.enrichmentService = enrichmentService;
		this.userService = userService;
		this.repository = repository;
		this.actionValidator = actionValidator;
		this.plValidator = plValidator;
		this.pLWorkflowService = pLWorkflowService;
		this.calculationService = calculationService;
		this.util = util;
		this.diffService = diffService;
		this.config = config;
		this.editNotificationService = editNotificationService;
		this.workflowService = workflowService;
		this.animalCategoryUtil = animalCategoryUtil;
	}

	/**
	 * creates the PetLicense for the given request
	 * 
	 * @param petLicenseRequest The PetLicense Create Request
	 * @return The list of created traddeLicense
	 */
	public List<PetLicense> create(PetLicenseRequest petLicenseRequest, String businessServicefromPath) {
		if (businessServicefromPath == null)
			businessServicefromPath = businessService_PL;
		plValidator.validateBusinessService(petLicenseRequest, businessServicefromPath);
		Object mdmsData = util.mDMSCall(petLicenseRequest);
		actionValidator.validateCreateRequest(petLicenseRequest);
		enrichmentService.enrichPLCreateRequest(petLicenseRequest, mdmsData);
		plValidator.validateCreate(petLicenseRequest, mdmsData);
//		switch (businessServicefromPath) {
//		case businessService_BPA:
//			validateMobileNumberUniqueness(petLicenseRequest);
//			break;
//		}
//		userService.createUser(petLicenseRequest, false);
		calculationService.addCalculation(petLicenseRequest);

		/*
		 * call workflow service if it's enable else uses internal workflow process
		 */
		switch (businessServicefromPath) {
		case businessService_PL:
			if (config.getIsExternalWorkFlowEnabled())
				wfIntegrator.callWorkFlow(petLicenseRequest);
			break;
		}
		repository.save(petLicenseRequest);
		return petLicenseRequest.getLicenses();
	}

//	public void validateMobileNumberUniqueness(PetLicenseRequest request) {
//		for (PetLicense license : request.getLicenses()) {
//			for (AnimalCategoryUtil animalCategoryUnits : license.getPetLicenseDetail().getAnimalCategory()) {
//				String tradetypeOfNewLicense = tradeUnit.getTradeType().split("\\.")[0];
//				List<String> mobileNumbers = license.getTradeLicenseDetail().getOwners().stream()
//						.map(OwnerInfo::getMobileNumber).collect(Collectors.toList());
//				for (String mobno : mobileNumbers) {
//					TradeLicenseSearchCriteria tradeLicenseSearchCriteria = TradeLicenseSearchCriteria.builder()
//							.tenantId(license.getTenantId()).businessService(license.getBusinessService())
//							.mobileNumber(mobno).build();
//					List<PetLicense> licensesFromSearch = getLicensesFromMobileNumber(tradeLicenseSearchCriteria,
//							request.getRequestInfo());
//					List<String> tradeTypeResultforSameMobNo = new ArrayList<>();
//					for (PetLicense result : licensesFromSearch) {
//						if (!StringUtils.equals(result.getApplicationNumber(), license.getApplicationNumber())
//								&& !StringUtils.equals(result.getStatus(), STATUS_REJECTED)) {
//							tradeTypeResultforSameMobNo.add(result.getTradeLicenseDetail().getTradeUnits().get(0)
//									.getTradeType().split("\\.")[0]);
//						}
//					}
//					if (tradeTypeResultforSameMobNo.contains(tradetypeOfNewLicense)) {
//						throw new CustomException("DUPLICATE_TRADETYPEONMOBNO",
//								" Same mobile number can not be used for more than one applications on same license type: "
//										+ tradetypeOfNewLicense);
//					}
//				}
//			}
//		}
//	}

	/**
	 * Searches the PetLicense for the given criteria if search is on owner
	 * paramter then first user service is called followed by query to db
	 * 
	 * @param criteria    The object containing the paramters on which to search
	 * @param requestInfo The search request's requestInfo
	 * @return List of PetLicense for the given criteria
	 */
	public List<PetLicense> search(PetLicenseSearchCriteria criteria, RequestInfo requestInfo,
			String serviceFromPath) {
		List<PetLicense> licenses;
		plValidator.validateSearch(requestInfo, criteria, serviceFromPath);
		criteria.setBusinessService(serviceFromPath);
		enrichmentService.enrichSearchCriteriaWithAccountId(requestInfo, criteria);
		licenses = getLicensesWithOwnerInfo(criteria, requestInfo);
//		if (criteria.getMobileNumber() != null) {
//			licenses = getLicensesFromMobileNumber(criteria, requestInfo);
//		} else {
//			licenses = getLicensesWithOwnerInfo(criteria, requestInfo);
//		}
		return licenses;
	}

	public void checkEndStateAndAddBPARoles(PetLicenseRequest petLicenseRequest) {
		List<String> endstates = animalCategoryUtil.getBPAEndState(petLicenseRequest);
		List<PetLicense> licensesToAddRoles = new ArrayList<>();
		for (int i = 0; i < petLicenseRequest.getLicenses().size(); i++) {
			PetLicense license = petLicenseRequest.getLicenses().get(0);
			if ((license.getStatus() != null) && license.getStatus().equalsIgnoreCase(endstates.get(i))) {
				licensesToAddRoles.add(license);
			}
		}
		if (!licensesToAddRoles.isEmpty()) {
			PetLicenseRequest petLicenseRequestForUserUpdate = PetLicenseRequest.builder()
					.licenses(licensesToAddRoles).requestInfo(petLicenseRequest.getRequestInfo()).build();
//			userService.createUser(petLicenseRequestForUserUpdate, true);
		}
	}

	public List<PetLicense> getLicensesFromMobileNumber(PetLicenseSearchCriteria criteria,
			RequestInfo requestInfo) {
		List<PetLicense> licenses = new LinkedList<>();
		UserDetailResponse userDetailResponse = userService.getUser(criteria, requestInfo);
		// If user not found with given user fields return empty list
		if (userDetailResponse.getUser().size() == 0) {
			return Collections.emptyList();
		}
		enrichmentService.enrichTLCriteriaWithOwnerids(criteria, userDetailResponse);
		licenses = repository.getLicenses(criteria);

		if (licenses.size() == 0) {
			return Collections.emptyList();
		}

		// Add tradeLicenseId of all licenses owned by the user
		criteria = enrichmentService.getPetLicenseCriteriaFromIds(licenses);
		// Get all tradeLicenses with ownerInfo enriched from user service
		licenses = getLicensesWithOwnerInfo(criteria, requestInfo);
		return licenses;
	}

	/**
	 * Returns the PetLicense with enrivhed owners from user servise
	 * 
	 * @param criteria    The object containing the paramters on which to search
	 * @param requestInfo The search request's requestInfo
	 * @return List of PetLicense for the given criteria
	 */
	public List<PetLicense> getLicensesWithOwnerInfo(PetLicenseSearchCriteria criteria, RequestInfo requestInfo) {
		List<PetLicense> licenses = repository.getLicenses(criteria);
		if (licenses.isEmpty())
			return Collections.emptyList();
		licenses = enrichmentService.enrichPetLicenseSearch(licenses, criteria, requestInfo);
		return licenses;
	}

	/**
	 * Returns PetLicense from db for the update request
	 * 
	 * @param request The update request
	 * @return List of tradeLicenses
	 */
	public List<PetLicense> getLicensesWithOwnerInfo(PetLicenseRequest request) {
		PetLicenseSearchCriteria criteria = new PetLicenseSearchCriteria();
		List<String> ids = new LinkedList<>();
		request.getLicenses().forEach(license -> {
			ids.add(license.getId());
		});

		criteria.setTenantId(request.getLicenses().get(0).getTenantId());
		criteria.setIds(ids);
		criteria.setBusinessService(request.getLicenses().get(0).getBusinessService());

		List<PetLicense> licenses = repository.getLicenses(criteria);

		if (licenses.isEmpty())
			return Collections.emptyList();
		licenses = enrichmentService.enrichPetLicenseSearch(licenses, criteria, request.getRequestInfo());
		return licenses;
	}

	/**
	 * Updates the tradeLicenses
	 * 
	 * @param PetLicenseRequest The update Request
	 * @return Updated TradeLcienses
	 */
	public List<PetLicense> update(PetLicenseRequest petLicenseRequest, String businessServicefromPath) {
		if (businessServicefromPath == null)
			businessServicefromPath = businessService_PL;
		plValidator.validateBusinessService(petLicenseRequest, businessServicefromPath);
		Object mdmsData = util.mDMSCall(petLicenseRequest);
		String businessServiceName = null;
		switch (businessServicefromPath) {
		case businessService_PL:
			businessServiceName = config.getPlBusinessServiceValue();
			break;

//		case businessService_BPA:
//			String tradeType = petLicenseRequest.getLicenses().get(0).getPetLicenseDetail().getTradeUnits().get(0)
//					.getTradeType();
//			if (pickWFServiceNameFromTradeTypeOnly)
//				tradeType = tradeType.split("\\.")[0];
//			businessServiceName = tradeType;
//			break;
		}
		BusinessService businessService = workflowService.getBusinessService(
				petLicenseRequest.getLicenses().get(0).getTenantId(), petLicenseRequest.getRequestInfo(),
				businessServiceName);
		List<PetLicense> searchResult = getLicensesWithOwnerInfo(petLicenseRequest);
		actionValidator.validateUpdateRequest(petLicenseRequest, businessService);
		enrichmentService.enrichPLUpdateRequest(petLicenseRequest, businessService);//
		plValidator.validateUpdate(petLicenseRequest, searchResult, mdmsData);
//		switch (businessServicefromPath) {
//		case businessService_BPA:
//			validateMobileNumberUniqueness(petLicenseRequest);
//			break;
//		}
		Map<String, Difference> diffMap = diffService.getDifference(petLicenseRequest, searchResult);
		Map<String, Boolean> idToIsStateUpdatableMap = util.getIdToIsStateUpdatableMap(businessService, searchResult);

		/*
		 * call workflow service if it's enable else uses internal workflow process
		 */
		List<String> endStates = Collections.nCopies(petLicenseRequest.getLicenses().size(), STATUS_APPROVED);
		switch (businessServicefromPath) {
//		case businessService_PL:
//			if (config.getIsExternalWorkFlowEnabled()) {
//				wfIntegrator.callWorkFlow(petLicenseRequest)
//			} else {
//				PLWorkflowService.updateStatus(petLicenseRequest);
//			}
//			break;

		case businessService_BPA:
			endStates = animalCategoryUtil.getBPAEndState(petLicenseRequest);
			wfIntegrator.callWorkFlow(petLicenseRequest);
			break;
		}
		enrichmentService.postStatusEnrichment(petLicenseRequest, endStates);
//		userService.createUser(petLicenseRequest, false);
		calculationService.addCalculation(petLicenseRequest);
		switch (businessServicefromPath) {
		case businessService_PL:
			editNotificationService.sendEditNotification(petLicenseRequest, diffMap);
			break;
		}
		repository.update(petLicenseRequest, idToIsStateUpdatableMap);
		return petLicenseRequest.getLicenses();
	}
}

