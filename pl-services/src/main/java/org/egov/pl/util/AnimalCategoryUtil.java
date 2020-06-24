package org.egov.pl.util;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.repository.ServiceRequestRepository;
import org.egov.pl.web.models.workflow.BusinessService;
import org.egov.pl.models.AuditDetails;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.egov.pl.util.PLConstants.*;
import static org.egov.pl.util.PLConstants.COMMON_MASTERS_MODULE;

@Component
@Slf4j
public class AnimalCategoryUtil {

    private PLConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private WorkflowService workflowService;

    @Autowired
    public AnimalCategoryUtil(PLConfiguration config, ServiceRequestRepository serviceRequestRepository,
                     WorkflowService workflowService) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.workflowService = workflowService;
    }



    /**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
        Long time = System.currentTimeMillis();
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }


    /**
     * Creates url for pl-calculator service
     * @return url for pl-calculator service
     */
    public StringBuilder getCalculationURI(String businessService) {
        StringBuilder uri = new StringBuilder();
        uri.append(config.getCalculatorHost());
        if (businessService == null)
            businessService = businessService_PL;
        switch (businessService) {
            case businessService_PL:
                uri.append(config.getCalculateEndpointPL());
                break;

            case businessService_BPA:
                uri.append(config.getCalculateEndpointBPA());
                break;
        }
        return uri;
    }


    /**
     * Creates search url for pt-services-v2 service
     * @return url for pt-services-v2 service search
     */
    public String getPropertySearchURL(){
        StringBuilder url = new StringBuilder(config.getPropertyHost());
        url.append(config.getPropertyContextPath());
        url.append(config.getPropertySearchEndpoint());
        url.append("?");
        url.append("tenantId=");
        url.append("{1}");
        url.append("&");
        url.append("ids=");
        url.append("{2}");
        return url.toString();
    }


    /**
     * Creates request to search UOM from MDMS
     * @param requestInfo The requestInfo of the request
     * @param tenantId The tenantId of the tradeLicense
     * @return request to search UOM from MDMS
     */
    public ModuleDetail getAnimalCategoryModuleRequest() {

        // master details for PL module
        List<MasterDetail> plMasterDetails = new ArrayList<>();

        // filter to only get code field from master data
        final String filterCode = "$.[?(@.active==true)].code";

        plMasterDetails.add(MasterDetail.builder().name(ANIMAL_CATEGORY).build());
        plMasterDetails.add(MasterDetail.builder().name(ANIMAL_SUB_CATEGORY).build());
        ModuleDetail plModuleDtls = ModuleDetail.builder().masterDetails(plMasterDetails)
                .moduleName(PET_LICENSE_MODULE).build();

        // master details for common-masters module
//        List<MasterDetail> commonMasterDetails = new ArrayList<>();
//        commonMasterDetails.add(MasterDetail.builder().name(ANIMAL_COLOR).build());
//        ModuleDetail commonMasterMDtl = ModuleDetail.builder().masterDetails(commonMasterDetails)
//                .moduleName(COMMON_MASTERS_MODULE).build();

//        ModuleDetail plModuleDtls = ModuleDetail.builder().masterDetails(plMasterDetails)
//                .moduleName(PET_LICENSE_MODULE).build();
        /*MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(tlModuleDtls,commonMasterMDtl)).tenantId(tenantId)
                .build();*/

        return plModuleDtls;

    }


    /**
     * Creates request to search UOM from MDMS
     * @return request to search UOM from MDMS
     */
    public ModuleDetail getAnimalColorRequest() {

        // master details for PL module
        List<MasterDetail> plMasterDetails = new ArrayList<>();

        // filter to only get code field from master data


        plMasterDetails.add(MasterDetail.builder().name(ANIMAL_COLOR).build());
        ModuleDetail plModuleDtls = ModuleDetail.builder().masterDetails(plMasterDetails)
                .moduleName(PET_LICENSE_MODULE).build();

        /*MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Collections.singletonList(tlModuleDtls)).tenantId(tenantId)
                .build();*/

        return plModuleDtls;
    }


    /**
     * Returns the url for mdms search endpoint
     *
     * @return url for mdms search endpoint
     */
    public StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
    }


    /**
     * Creates map containing the startTime and endTime of the given tradeLicense
     * @param license The create or update petLicense request
     * @return Map containing startTime and endTime
     */
    public Map<String,Long> getTaxPeriods(PetLicense license,Object mdmsData){
        Map<String,Long> taxPeriods = new HashMap<>();
        try {
            String jsonPath = PLConstants.MDMS_FINACIALYEAR_PATH.replace("{}",license.getFinancialYear());
            List<Map<String,Object>> jsonOutput =  JsonPath.read(mdmsData, jsonPath);
            Map<String,Object> financialYearProperties = jsonOutput.get(0);
            Object startDate = financialYearProperties.get(PLConstants.MDMS_STARTDATE);
            Object endDate = financialYearProperties.get(PLConstants.MDMS_ENDDATE);
            taxPeriods.put(PLConstants.MDMS_STARTDATE,(Long) startDate);
            taxPeriods.put(PLConstants.MDMS_ENDDATE,(Long) endDate);

        } catch (Exception e) {
            log.error("Error while fetching MDMS data", e);
            throw new CustomException("INVALID FINANCIALYEAR", "No data found for the financialYear: "+license.getFinancialYear());
        }
        return taxPeriods;
    }


    /**
     * Creates request to search financialYear in mdms
     * @return MDMS request for financialYear
     */
    private ModuleDetail getFinancialYearRequest() {

        // master details for PL module
        List<MasterDetail> plMasterDetails = new ArrayList<>();

        // filter to only get code field from master data

        final String filterCodeForUom = "$.[?(@.active==true && @.module=='PL')]";

        plMasterDetails.add(MasterDetail.builder().name(PLConstants.MDMS_FINANCIALYEAR).filter(filterCodeForUom).build());

        ModuleDetail plModuleDtls = ModuleDetail.builder().masterDetails(plMasterDetails)
                .moduleName(PLConstants.MDMS_EGF_MASTER).build();


  /*      MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Collections.singletonList(tlModuleDtls)).tenantId(tenantId)
                .build();*/

        return plModuleDtls;
    }


    private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo,String tenantId){
        ModuleDetail financialYearRequest = getFinancialYearRequest();
        List<ModuleDetail> petModuleRequest = (List<ModuleDetail>) getAnimalCategoryModuleRequest();

        List<ModuleDetail> moduleDetails = new LinkedList<>();
        moduleDetails.add(financialYearRequest);
        moduleDetails.addAll(petModuleRequest);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        return mdmsCriteriaReq;
    }

    public List<String> getBPAEndState(PetLicenseRequest petLicenseRequest) {

        List<String> endstates = new ArrayList<>();
        for (PetLicense petLicense : petLicenseRequest.getLicenses()) {
            String animalCategory = petLicense.getPetLicenseDetail().getAnimalCategory().getName();
            Object mdmsData = mDMSCallForBPA(petLicenseRequest.getRequestInfo(), petLicense.getTenantId(), animalCategory);
            List<String> res = JsonPath.read(mdmsData, BPAConstants.MDMS_ENDSTATEPATH);
            endstates.add(res.get(0));
        }
        return endstates;
    }

    public List<String> getusernewRoleFromMDMS(PetLicense license,RequestInfo requestInfo){
        String animalCategory=license.getPetLicenseDetail().getAnimalCategory().getName();
        Object mdmsData=mDMSCallForBPA(requestInfo,license.getTenantId(),animalCategory);
        List<List<String>>res=JsonPath.read(mdmsData, BPAConstants.MDMS_BPAROLEPATH);
        return  res.get(0);
    }

    public Object mDMSCall(PetLicenseRequest petLicenseRequest){
        RequestInfo requestInfo = petLicenseRequest.getRequestInfo();
        String tenantId = petLicenseRequest.getLicenses().get(0).getTenantId();
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo,tenantId);
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        return result;
    }




    public Object mDMSCallForBPA(RequestInfo requestInfo,String tenantId,String tradetype){


        List<MasterDetail> masterDetails = new ArrayList<>();


        final String filterCodeForLicenseetypes = "$.[?(@.tradeType =='"+tradetype+"')]";

        masterDetails.add(MasterDetail.builder().name(BPAConstants.TRADETYPE_TO_ROLEMAPPING).filter(filterCodeForLicenseetypes).build());

        ModuleDetail moduleDetail = ModuleDetail.builder().masterDetails(masterDetails)
                .moduleName(BPAConstants.MDMS_MODULE_BPAREGISTRATION).build();


        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Collections.singletonList(moduleDetail)).tenantId(tenantId)
                .build();


        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
                .requestInfo(requestInfo).build();
        Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
        return result;
    }




    /**
     * Creates a map of id to isStateUpdatable
     * @param searchresult Licenses from DB
     * @param businessService The businessService configuration
     * @return Map of is to isStateUpdatable
     */
    public Map<String, Boolean> getIdToIsStateUpdatableMap(BusinessService businessService, List<PetLicense> searchresult) {
        Map<String, Boolean> idToIsStateUpdatableMap = new HashMap<>();
        searchresult.forEach(result -> {
            String nameofBusinessService = result.getBusinessService();
            if (StringUtils.equals(nameofBusinessService,businessService_BPA) && (result.getStatus().equalsIgnoreCase(STATUS_INITIATED))) {
                idToIsStateUpdatableMap.put(result.getId(), true);
            } else
                idToIsStateUpdatableMap.put(result.getId(), workflowService.isStateUpdatable(result.getStatus(), businessService));
        });
        return idToIsStateUpdatableMap;
    }
}
