package org.egov.pl.workflow;

import static org.egov.pl.util.PLConstants.ACTION_APPLY;
import static org.egov.pl.util.PLConstants.ACTION_INITIATE;
import static org.egov.pl.util.PLConstants.STATUS_INITIATED;
import static org.egov.pl.util.PLConstants.TRIGGER_NOWORKFLOW;
import static org.egov.pl.util.PLConstants.businessService_PL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pl.models.OwnerInfo;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.web.models.workflow.BusinessService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ActionValidator {

	private WorkflowConfig workflowConfig;

	private WorkflowService workflowService;

	@Autowired
	public ActionValidator(WorkflowConfig workflowConfig, WorkflowService workflowService) {
		this.workflowConfig = workflowConfig;
		this.workflowService = workflowService;
	}

	/**
	 * Validates create request
	 * 
	 * @param request The petLicense Create request
	 */
	public void validateCreateRequest(PetLicenseRequest request) {
		Map<String, String> errorMap = new HashMap<>();

		request.getLicenses().forEach(license -> {

			String businessService = license.getBusinessService();
			if (businessService == null)
				businessService = businessService_PL;
			switch (businessService) {
			case businessService_PL:
				if (ACTION_INITIATE.equalsIgnoreCase(license.getAction())) {
					if (license.getPetLicenseDetail().getApplicationDocuments() != null)
						errorMap.put("INVALID ACTION", "Action should be APPLY when application document are provided");
				}
				if (ACTION_APPLY.equalsIgnoreCase(license.getAction())) {
					if (license.getPetLicenseDetail().getApplicationDocuments() == null)
						errorMap.put("INVALID ACTION",
								"Action cannot be changed to APPLY. Application document are not provided");
				}
				if (!ACTION_APPLY.equalsIgnoreCase(license.getAction())
						&& !ACTION_INITIATE.equalsIgnoreCase(license.getAction())) {
					errorMap.put("INVALID ACTION", "Action can only be APPLY or INITIATE during create");
				}
				break;

			}
		});
		// validateRole(request);

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validates the update request
	 * 
	 * @param request The petLciense update request
	 */
	public void validateUpdateRequest(PetLicenseRequest request, BusinessService businessService) {
		validateDocumentsForUpdate(request);
		// validateRole(request);
		// validateAction(request);
		validateIds(request, businessService);
	}

	/**
	 * Validates the applicationDocument
	 * 
	 * @param request The petLciense create or update request
	 */
	private void validateDocumentsForUpdate(PetLicenseRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		request.getLicenses().forEach(license -> {
			if (ACTION_INITIATE.equalsIgnoreCase(license.getAction())) {
				if (license.getPetLicenseDetail().getApplicationDocuments() != null)
					errorMap.put("INVALID STATUS", "Status cannot be INITIATE when application document are provided");
			}
			if (ACTION_APPLY.equalsIgnoreCase(license.getAction())) {
				if (license.getPetLicenseDetail().getApplicationDocuments() == null)
					errorMap.put("INVALID STATUS", "Status cannot be APPLY when application document are not provided");
			}
		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validates if the role of the logged in user can perform the given action
	 * 
	 * @param request The petLciense create or update request
	 */
	private void validateRole(PetLicenseRequest request) {
		Map<String, List<String>> roleActionMap = workflowConfig.getRoleActionMap();
		Map<String, String> errorMap = new HashMap<>();
		List<PetLicense> licenses = request.getLicenses();
		RequestInfo requestInfo = request.getRequestInfo();
		List<Role> roles = requestInfo.getUserInfo().getRoles();

		List<String> actions = new LinkedList<>();
		roles.forEach(role -> {
			if (!CollectionUtils.isEmpty(roleActionMap.get(role.getCode()))) {
				actions.addAll(roleActionMap.get(role.getCode()));
			}
		});

		licenses.forEach(license -> {
			if (!actions.contains(license.getAction().toString()))
				errorMap.put("UNAUTHORIZED UPDATE", "The action cannot be performed by this user");
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validate if the action can be performed on the current status
	 * 
	 * @param request The petLciense update request
	 */
	private void validateAction(PetLicenseRequest request) {
		Map<String, List<String>> actionStatusMap = workflowConfig.getActionCurrentStatusMap();
		Map<String, String> errorMap = new HashMap<>();

		request.getLicenses().forEach(license -> {
			if (actionStatusMap.get(license.getStatus().toString()) != null) {
				if (!actionStatusMap.get(license.getStatus().toString()).contains(license.getAction().toString()))
					errorMap.put("UNAUTHORIZED ACTION", "The action " + license.getAction()
							+ " cannot be applied on the status " + license.getStatus());
			}
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validates if the any new object is added in the request
	 * 
	 * @param request The petLciense update request
	 */
	private void validateIds(PetLicenseRequest request, BusinessService businessService) {
		Map<String, String> errorMap = new HashMap<>();
		request.getLicenses().forEach(license -> {

			String namefBusinessService = license.getBusinessService();
			if ((namefBusinessService == null) || (namefBusinessService.equals(businessService_PL))
					&& (!license.getStatus().equalsIgnoreCase(STATUS_INITIATED))) {
				if (!workflowService.isStateUpdatable(license.getStatus(), businessService)) {
					if (license.getId() == null)
						errorMap.put("INVALID UPDATE", "Id of petLicense cannot be null");
					if (license.getPetLicenseDetail().getId() == null)
						errorMap.put("INVALID UPDATE", "Id of petLicenseDetail cannot be null");
					if (license.getPetLicenseDetail().getAddress() == null)
						errorMap.put("INVALID UPDATE", "Id of address cannot be null");
					OwnerInfo owner = license.getPetLicenseDetail().getOwner();
						
						if (owner.getUuid() == null)
							errorMap.put("INVALID UPDATE", "Id of owner cannot be null");
						if (!CollectionUtils.isEmpty(owner.getDocuments())) {
							owner.getDocuments().forEach(document -> {
								if (document.getId() == null)
									errorMap.put("INVALID UPDATE", "Id of owner document cannot be null");
							});
						}
			
					if (!CollectionUtils.isEmpty(license.getPetLicenseDetail().getApplicationDocuments())) {
						license.getPetLicenseDetail().getApplicationDocuments().forEach(document -> {
							if (document.getId() == null)
								errorMap.put("INVALID UPDATE", "Id of applicationDocument cannot be null");
						});
					}
				}
			}
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
