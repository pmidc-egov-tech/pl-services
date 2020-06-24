package org.egov.pl.validator;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.pl.web.models.user.UserDetailResponse;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.OwnerInfo;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.models.PetLicenseSearchCriteria;
import org.egov.pl.repository.PLRepository;
import org.egov.pl.service.PetLicenseService;
import org.egov.pl.service.UserService;
import org.egov.pl.util.BPAConstants;
import org.egov.pl.util.PLConstants;
import org.egov.pl.util.AnimalCategoryUtil;
import org.egov.pl.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.pl.util.PLConstants.businessService_BPA;
import static org.egov.pl.util.PLConstants.businessService_PL;

@Component
public class PLValidator {


    private PLRepository plRepository;

    private PLConfiguration config;

    private MDMSValidator mdmsValidator;

    private AnimalCategoryUtil animalCategoryUtil;

    private UserService userService;

    @Value("${egov.allowed.businessServices}")
    private String allowedBusinessService;

    @Value("${egov.receipt.businessservicePL}")
    private String businessServicePL;

    @Autowired
    public PLValidator(PLRepository plRepository, PLConfiguration config, 
                       MDMSValidator mdmsValidator, AnimalCategoryUtil animalCategoryUtil,UserService userService) {
        this.plRepository = plRepository;
        this.config = config;
        this.mdmsValidator = mdmsValidator;
        this.animalCategoryUtil = animalCategoryUtil;
        this.userService=userService;
    }


    /**
     *  Validate the create Requesr
     * @param request The input PetLicenseRequest Object
     */
    public void validateCreate(PetLicenseRequest request, Object mdmsData) {
        String businessService = request.getLicenses().isEmpty()?null:request.getLicenses().get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_PL;
  
        mdmsValidator.validateMdmsData(request, mdmsData);
//        validateInstitution(request);
        validateDuplicateDocuments(request);
    }

    public void validateBusinessService(PetLicenseRequest request, String businessServiceFromPath) {
        List<String> allowedservices = Arrays.asList(allowedBusinessService.split(","));
        if (!allowedservices.contains(businessServiceFromPath)) {
            throw new CustomException("BUSINESSSERVICE_NOTALLOWED", " The business service is not allowed in this module");
        }
        for (PetLicense license : request.getLicenses()) {
            String licenseBusinessService = license.getBusinessService()==null?businessService_PL:license.getBusinessService();
            if (!StringUtils.equals(businessServiceFromPath, licenseBusinessService)) {
                throw new CustomException("BUSINESSSERVICE_NOTMATCHING", " The business service inside license not matching with the one sent in path variable");
            }
        }
    }

    private void validatePLSpecificNotNullFields(PetLicenseRequest request) {
        request.getLicenses().forEach(license -> {
            Map<String, String> errorMap = new HashMap<>();
            if ((license.getPetLicenseDetail().getAddress().getLocality() == null)||(license.getPetLicenseDetail().getAddress().getLocality().getCode() == null))
                errorMap.put("NULL_LOCALITY", " Locality cannot be null");

            if (!errorMap.isEmpty())
                throw new CustomException(errorMap);
        });
    }

    private void validateBPASpecificValidations(PetLicenseRequest request) {
        OwnerInfo owner = request.getLicenses().get(0).getPetLicenseDetail().getOwner();
                        if (owner.getGender() == null)
                            throw new CustomException("NULL_USERGENDER", " User gender cannot be null");

                        if (owner.getEmailId() == null)
                            throw new CustomException("NULL_USEREMAIL", " User EmailId cannot be null");

                        if (owner.getPermanentAddress() == null)
                            throw new CustomException("NULL_PERMANENTADDRESS", " User Permanent Address cannot be null");

    }
    /**
     *  Validates the fromDate and toDate of the request
     * @param request The input PetLicenseRequest Object
     */
    private void valideDates(PetLicenseRequest request,Object mdmsData){
        request.getLicenses().forEach(license -> {
            if(license.getValidTo()==null)
                throw new CustomException("INVALID VALIDTO DATE"," Validto cannot be null");
            Map<String,Long> taxPeriods = animalCategoryUtil.getTaxPeriods(license,mdmsData);
            if(license.getValidTo()!=null && license.getValidTo()>taxPeriods.get(PLConstants.MDMS_ENDDATE)){
                Date expiry = new Date(license.getValidTo());
                throw new CustomException("INVALID TO DATE"," Validto cannot be greater than: "+expiry);
            }
            if(license.getLicenseType().toString().equalsIgnoreCase(PetLicense.LicenseTypeEnum.TEMPORARY.toString())) {
                Long startOfDay = getStartOfDay();
                if (!config.getIsPreviousPLAllowed() && license.getValidFrom() != null
                        && license.getValidFrom() < startOfDay)
                    throw new CustomException("INVALID FROM DATE", "The validFrom date cannot be less than CurrentDate");
                if ((license.getValidFrom() != null && license.getValidTo() != null) && (license.getValidTo() - license.getValidFrom()) < config.getMinPeriod())
                    throw new CustomException("INVALID PERIOD", "The license should be applied for minimum of 30 days");
            }
        });
    }

    /**
     * Returns the start of the current day in millis
     * @return time in millis
     */
    private Long getStartOfDay(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTimeInMillis();
    }


   
    /**
     *  Validates the update request
     * @param request The input PetLicenseRequest Object
     */
    public void validateUpdate(PetLicenseRequest request, List<PetLicense> searchResult, Object mdmsData) {
        List<PetLicense> licenses = request.getLicenses();
        if (searchResult.size() != licenses.size())
            throw new CustomException("INVALID UPDATE", "The license to be updated is not in database");
        validateAllIds(searchResult, licenses);
       
        mdmsValidator.validateMdmsData(request, mdmsData);
//        validateAnimalCategory(request);
        validateDuplicateDocuments(request);
        setFieldsFromSearch(request, searchResult, mdmsData);
        validateOwnerActiveStatus(request);
    }


    /**
     * Validates that atleast one tradeUnit is active equal true or new tradeUnit
     * @param request The input PetLicenseRequest Object
     */
//    private void validateTradeUnits(PetLicenseRequest request){
//        Map<String,String> errorMap = new HashMap<>();
//        List<PetLicense> licenses = request.getLicenses();
//
//        for(PetLicense license : licenses)
//        {
//            Boolean flag = false;
//            List<PetUnit> units = license.getPetLicenseDetail().getTradeUnits();
//            for(PetUnit unit : units) {
//                if(unit.getId()!=null && unit.getActive())
//                    flag = true;
//                else if(unit.getId()==null)
//                    flag = true;
//            }
//            if(!flag)
//                errorMap.put("INVALID UPDATE","All TradeUnits are inactive in the tradeLicense: "+license.getApplicationNumber());
//        }
//
//        if(!errorMap.isEmpty())
//            throw new CustomException(errorMap);
//    }





    /**
     * Returns the list of ids of all owners as list for the given tradelicense
     * @param license PetLicense whose ownerIds are to be extracted
     * @return list od OwnerIds
     */
    private String getOwnerIds(PetLicense license){
    	OwnerInfo owner = license.getPetLicenseDetail().getOwner();
        return owner.getUuid();
    }

    /**
     * Returns the list of ids of all tradeUnits as list for the given tradelicense
     * @param license PetLicense whose tradeUnitIds are to be extracted
     * @return list od tradeUnitIdss
     */
//    private List<String> getTradeUnitIds(PetLicense license){
//        List<String> tradeUnitIds = new LinkedList<>();
//        if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getTradeUnits())){
//            license.getPetLicenseDetail().getTradeUnits().forEach(tradeUnit -> {
//                tradeUnitIds.add(tradeUnit.getId());
//            });
//        }
//        return tradeUnitIds;
//    }

    
    /**
     * Returns the list of ids of all ownerDocs as list for the given tradelicense
     * @param license PetLicense whose ownerDocIds are to be extracted
     * @return list od ownerDocIds
     */
    private String getOwnerDocIds(PetLicense license){
    	String ownerDocIds;
    	OwnerInfo owner = license.getPetLicenseDetail().getOwner();
    	ownerDocIds = owner.getUuid();      
        return ownerDocIds;
    }

    /**
     * Returns the list of ids of all applicationDoc as list for the given tradelicense
     * @param license PetLicense whose applicationDocIds are to be extracted
     * @return list od applicationDocIds
     */
    private List<String> getApplicationDocIds(PetLicense license){
        List<String> applicationDocIds = new LinkedList<>();
        if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getApplicationDocuments())){
            license.getPetLicenseDetail().getApplicationDocuments().forEach(document -> {
                applicationDocIds.add(document.getId());
            });
        }
        return applicationDocIds;
    }

    /**
     * Returns the list of ids of all verficationDoc as list for the given tradelicense
     * @param license PetLicense whose VerficationDocIds are to be extracted
     * @return list od VerficationDocIds
     */
    private List<String> getVerficationDocIds(PetLicense license){
        List<String> verficationDocIds = new LinkedList<>();
        if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getVerificationDocuments())) {
            license.getPetLicenseDetail().getVerificationDocuments().forEach(document -> {
                verficationDocIds.add(document.getId());
            });
        }
        return verficationDocIds;
    }


    /**
     * Enriches the immutable fields from database
     * @param request The input PetLicenseRequest
     * @param searchResult The list of searched licenses
     */
    private void setFieldsFromSearch(PetLicenseRequest request, List<PetLicense> searchResult, Object mdmsData) {
        Map<String,PetLicense> idToPetLicenseFromSearch = new HashMap<>();
        searchResult.forEach(petLicense -> {
            idToPetLicenseFromSearch.put(petLicense.getId(),petLicense);
        });
        request.getLicenses().forEach(license -> {
            license.getAuditDetails().setCreatedBy(idToPetLicenseFromSearch.get(license.getId()).getAuditDetails().getCreatedBy());
            license.getAuditDetails().setCreatedTime(idToPetLicenseFromSearch.get(license.getId()).getAuditDetails().getCreatedTime());
            license.setStatus(idToPetLicenseFromSearch.get(license.getId()).getStatus());
            license.setLicenseNumber(idToPetLicenseFromSearch.get(license.getId()).getLicenseNumber());
            String businessService = license.getBusinessService();
            if (businessService == null)
                businessService = businessService_PL;
            switch (businessService) {
                case businessService_PL:
                    if (!idToPetLicenseFromSearch.get(license.getId()).getFinancialYear().equalsIgnoreCase(license.getFinancialYear())
                            && license.getLicenseType().equals(PetLicense.LicenseTypeEnum.PERMANENT)) {
                        Map<String, Long> taxPeriods = animalCategoryUtil.getTaxPeriods(license, mdmsData);
                        license.setValidTo(taxPeriods.get(PLConstants.MDMS_ENDDATE));
                    }
                    break;
            }
        });
    }

    /**
     * Validates if all ids are same as obtained from search result
     * @param searchResult The license from search
     * @param licenses The licenses from the update Request
     */
    private void validateAllIds(List<PetLicense> searchResult,List<PetLicense> licenses){

        Map<String,PetLicense> idToPetLicenseFromSearch = new HashMap<>();
        searchResult.forEach(tradeLicense -> {
            idToPetLicenseFromSearch.put(tradeLicense.getId(),tradeLicense);
        });

        Map<String,String> errorMap = new HashMap<>();
        licenses.forEach(license -> {
            PetLicense searchedLicense = idToPetLicenseFromSearch.get(license.getId());

            if(!searchedLicense.getApplicationNumber().equalsIgnoreCase(license.getApplicationNumber()))
                errorMap.put("INVALID UPDATE","The application number from search: "+searchedLicense.getApplicationNumber()
                        +" and from update: "+license.getApplicationNumber()+" does not match");

            if(!searchedLicense.getPetLicenseDetail().getId().
                    equalsIgnoreCase(license.getPetLicenseDetail().getId()))
                errorMap.put("INVALID UPDATE","The id "+license.getPetLicenseDetail().getId()+" does not exist");

            if(!searchedLicense.getPetLicenseDetail().getAddress().getId().
                    equalsIgnoreCase(license.getPetLicenseDetail().getAddress().getId()))
                errorMap.put("INVALID UPDATE","The id "+license.getPetLicenseDetail().getAddress().getId()+" does not exist");

//            compareIdList(getTradeUnitIds(searchedLicense),getTradeUnitIds(license),errorMap);
//            compareIdList(getAccessoryIds(searchedLicense),getAccessoryIds(license),errorMap);
//            compareIdList(getOwnerIds(searchedLicense),getOwnerIds(license),errorMap);
//            compareIdList(getOwnerDocIds(searchedLicense),getOwnerDocIds(license),errorMap);
            compareIdList(getApplicationDocIds(searchedLicense),getApplicationDocIds(license),errorMap);
            compareIdList(getVerficationDocIds(searchedLicense),getVerficationDocIds(license),errorMap);
        });

        if(!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
    }


    /**
     * Checks if the ids are present in the searchedIds
     * @param searchIds Ids got from search
     * @param updateIds The ids received from update Request
     * @param errorMap The map for collecting errors
     */
    private void compareIdList(List<String> searchIds,List<String> updateIds,Map<String,String> errorMap){
        if(!CollectionUtils.isEmpty(searchIds))
            searchIds.forEach(searchId -> {
                if(!updateIds.contains(searchId))
                    errorMap.put("INVALID UPDATE","The id: "+searchId+" was not present in update request");
            });
    }


    /**
     * Validates if the search parameters are valid
     * @param requestInfo The requestInfo of the incoming request
     * @param criteria The PetLicenseSearch Criteria
     */
    public void validateSearch(RequestInfo requestInfo, PetLicenseSearchCriteria criteria, String serviceFromPath) {
        String serviceInSearchCriteria = criteria.getBusinessService();
        if ((serviceInSearchCriteria != null) && (!StringUtils.equals(serviceFromPath, serviceInSearchCriteria))) {
            throw new CustomException("INVALID SEARCH", "Business service in Path param and requestbody not matching");
        }

        List<String> allowedservices = Arrays.asList(allowedBusinessService.split(","));
        if ((serviceFromPath != null) && (!allowedservices.contains(serviceFromPath))) {
            throw new CustomException("INVALID SEARCH", "Search not allowed on this business service");
        }

        if(!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" )&& criteria.isEmpty())
            throw new CustomException("INVALID SEARCH","Search without any paramters is not allowed");

        if(!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" )&& criteria.tenantIdOnly())
            throw new CustomException("INVALID SEARCH","Search based only on tenantId is not allowed");

        if(!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" )&& !criteria.tenantIdOnly()
                && criteria.getTenantId()==null)
            throw new CustomException("INVALID SEARCH","TenantId is mandatory in search");

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" ) && !criteria.isEmpty()
                && !criteria.tenantIdOnly() && criteria.getTenantId()==null)
            throw new CustomException("INVALID SEARCH","TenantId is mandatory in search");

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" )&& criteria.tenantIdOnly())
            throw new CustomException("INVALID SEARCH","Search only on tenantId is not allowed");

        String allowedParamStr = null;

        if(requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN" ))
            allowedParamStr = config.getAllowedCitizenSearchParameters();
        else if(requestInfo.getUserInfo().getType().equalsIgnoreCase("EMPLOYEE" ))
            allowedParamStr = config.getAllowedEmployeeSearchParameters();
        else throw new CustomException("INVALID SEARCH","The userType: "+requestInfo.getUserInfo().getType()+
                    " does not have any search config");

        if(StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
            throw new CustomException("INVALID SEARCH","No search parameters are expected");
        else {
            List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
            validateSearchParams(criteria, allowedParams);
        }
    }


    /**
     * Validates if the paramters coming in search are allowed
     * @param criteria PetLicense search criteria
     * @param allowedParams Allowed Params for search
     */
    private void validateSearchParams(PetLicenseSearchCriteria criteria,List<String> allowedParams){

        if(criteria.getApplicationNumber()!=null && !allowedParams.contains("applicationNumber"))
            throw new CustomException("INVALID SEARCH","Search on applicationNumber is not allowed");

        if(criteria.getTenantId()!=null && !allowedParams.contains("tenantId"))
            throw new CustomException("INVALID SEARCH","Search on tenantId is not allowed");

        if(criteria.getToDate()!=null && !allowedParams.contains("toDate"))
            throw new CustomException("INVALID SEARCH","Search on toDate is not allowed");

        if(criteria.getFromDate()!=null && !allowedParams.contains("fromDate"))
            throw new CustomException("INVALID SEARCH","Search on fromDate is not allowed");

        if(criteria.getStatus()!=null && !allowedParams.contains("status"))
            throw new CustomException("INVALID SEARCH","Search on Status is not allowed");

        if(criteria.getIds()!=null && !allowedParams.contains("ids"))
            throw new CustomException("INVALID SEARCH","Search on ids is not allowed");

        if(criteria.getMobileNumber()!=null && !allowedParams.contains("mobileNumber"))
            throw new CustomException("INVALID SEARCH","Search on mobileNumber is not allowed");

        if(criteria.getLicenseNumber()!=null && !allowedParams.contains("licenseNumber"))
            throw new CustomException("INVALID SEARCH","Search on licenseNumber is not allowed");

        if(criteria.getOldLicenseNumber()!=null && !allowedParams.contains("oldLicenseNumber"))
            throw new CustomException("INVALID SEARCH","Search on oldLicenseNumber is not allowed");

        if(criteria.getOffset()!=null && !allowedParams.contains("offset"))
            throw new CustomException("INVALID SEARCH","Search on offset is not allowed");

        if(criteria.getLimit()!=null && !allowedParams.contains("limit"))
            throw new CustomException("INVALID SEARCH","Search on limit is not allowed");

    }


    /**
     * Validates application documents for duplicates
     * @param request The tradeLcienseRequest
     */
    private void validateDuplicateDocuments(PetLicenseRequest request){
        List<String> documentFileStoreIds = new LinkedList();
        request.getLicenses().forEach(license -> {
            if(license.getPetLicenseDetail().getApplicationDocuments()!=null){
                license.getPetLicenseDetail().getApplicationDocuments().forEach(
                        document -> {
                            if(documentFileStoreIds.contains(document.getFileStoreId()))
                                throw new CustomException("DUPLICATE_DOCUMENT ERROR","Same document cannot be used multiple times");
                            else documentFileStoreIds.add(document.getFileStoreId());
                        }
                );
            }
        });
    }


    /**
     * Checks if atleast one owner is active in PL
     * @param request The update request
     */
    private void validateOwnerActiveStatus(PetLicenseRequest request){
        Map<String,String> errorMap = new HashMap<>();
        OwnerInfo ownerInfo = request.getLicenses().get(0).getPetLicenseDetail().getOwner();
            Boolean flag = false;
                if(ownerInfo.getUserActive()){
                    flag=true;
                }
            if(!flag)
                errorMap.put("INVALID OWNER","All owners are inactive for application:  "+request.getLicenses().get(0).getApplicationNumber());
       
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }






}


