package org.egov.pl.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.repository.ServiceRequestRepository;
import org.egov.pl.util.PLConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MDMSValidator {

	private ServiceRequestRepository requestRepository;

	private PetUtil util;

	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public MDMSValidator(ServiceRequestRepository requestRepository, PetUtil util,
			ServiceRequestRepository serviceRequestRepository) {
		this.requestRepository = requestRepository;
		this.util = util;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	/**
	 * method to validate the mdms data in the request
	 *
	 * @param licenseRequest
	 */
	public void validateMdmsData(PetLicenseRequest licenseRequest, Object mdmsData) {

		Map<String, String> errorMap = new HashMap<>();

		Map<String, List<String>> masterData = getAttributeValues(mdmsData);

		

		licenseRequest.getLicenses().forEach(license -> {

		});

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}

	/**
	 * Validates if MasterData is properly fetched for the given MasterData names
	 * 
	 * @param masterNames
	 * @param codes
	 */
	private void validateIfMasterPresent(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Fetches all the values of particular attribute as map of field name to list
	 *
	 * takes all the masters from each module and adds them in to a single map
	 *
	 * note : if two masters from different modules have the same name then it
	 *
	 * will lead to overriding of the earlier one by the latest one added to the map
	 *
	 * @return Map of MasterData name to the list of code in the MasterData
	 *
	 */
	private Map<String, List<String>> getAttributeValues(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(PLConstants.PL_JSONPATH_CODE, PLConstants.COMMON_MASTER_JSONPATH_CODE);

		final Map<String, List<String>> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetvhing MDMS data", e);
				throw new CustomException(PLConstants.INVALID_TENANT_ID_MDMS_KEY,
						PLConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});

		System.err.println(" the mdms response is : " + mdmsResMap);
		return mdmsResMap;
	}

	/**
	 * Fetches map of UOM to UOMValues
	 * 
	 * @param mdmsData The MDMS data
	 * @return
	 */
	private Map<String, List<String>> getUomMap(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(PLConstants.PL_JSONPATH_CODE);
		final Map<String, List<String>> mdmsResMap = new HashMap<>();

		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetvhing MDMS data", e);
				throw new CustomException(PLConstants.INVALID_TENANT_ID_MDMS_KEY,
						PLConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});

		System.err.println(" the mdms response is : " + mdmsResMap);
		return mdmsResMap;
	}

}
