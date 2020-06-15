package org.egov.pl.workflow;

import static org.egov.pl.util.PLConstants.ACTION_APPROVE;

import java.util.Map;

import org.apache.kafka.clients.producer.Producer;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.PetLicenseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PLWorkflowService {

	private ActionValidator actionValidator;
	private Producer producer;
	private PLConfiguration config;
	private WorkflowConfig workflowConfig;

	@Autowired
	public PLWorkflowService(ActionValidator actionValidator, Producer producer, PLConfiguration config,
			WorkflowConfig workflowConfig) {
		this.actionValidator = actionValidator;
		this.producer = producer;
		this.config = config;
		this.workflowConfig = workflowConfig;
	}

	/**
	 * Validates and updates the status
	 * 
	 * @param request The update petLicense Request
	 */
	public void updateStatus(PetLicenseRequest request) {
		actionValidator.validateUpdateRequest(request, null);
		changeStatus(request);
	}

	/**
	 * Changes the status of the petLicense according to action status mapping
	 * 
	 * @param request The update petLicenseRequest
	 */
	private void changeStatus(PetLicenseRequest request) {
		Map<String, String> actionToStatus = workflowConfig.getActionStatusMap();
		request.getLicenses().forEach(license -> {
			license.setStatus(actionToStatus.get(license.getAction()));
			if (license.getAction().equalsIgnoreCase(ACTION_APPROVE)) {
				Long time = System.currentTimeMillis();
				license.setIssuedDate(time);
				license.setValidFrom(time);
			}
		});
	}

}
