package org.egov.pl.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.repository.IdGenRepository;
import org.egov.pl.util.PLConstants;
import org.egov.pl.util.AnimalCategoryUtil;
import org.egov.pl.models.*;
import org.egov.pl.web.models.Idgen.IdResponse;
import org.egov.pl.web.models.user.UserDetailResponse;
import org.egov.pl.web.models.workflow.BusinessService;
import org.egov.pl.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import static org.egov.pl.util.PLConstants.*;


@Service
public class EnrichmentService {

    private IdGenRepository idGenRepository;
    private PLConfiguration config;
    private AnimalCategoryUtil tradeUtil;
    private BoundaryService boundaryService;
    private UserService userService;
    private WorkflowService workflowService;

    @Autowired
    public EnrichmentService(IdGenRepository idGenRepository, PLConfiguration config, AnimalCategoryUtil tradeUtil,
                             BoundaryService boundaryService,UserService userService,WorkflowService workflowService) {
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.tradeUtil = tradeUtil;
        this.boundaryService = boundaryService;
        this.userService = userService;
        this.workflowService = workflowService;
    }


    /**
     * Enriches the incoming createRequest
     * @param petLicenseRequest The create request for the petLicense
     */
    public void enrichPLCreateRequest(PetLicenseRequest petLicenseRequest,Object mdmsData) {
        RequestInfo requestInfo = petLicenseRequest.getRequestInfo();
        String uuid = requestInfo.getUserInfo().getUuid();
        AuditDetails auditDetails = tradeUtil.getAuditDetails(uuid, true);
        petLicenseRequest.getLicenses().forEach(petLicense -> {
        	petLicense.setAuditDetails(auditDetails);
        	petLicense.setId(UUID.randomUUID().toString());
        	petLicense.setApplicationDate(auditDetails.getCreatedTime());
        	petLicense.getPetLicenseDetail().setId(UUID.randomUUID().toString());
        	petLicense.getPetLicenseDetail().setAuditDetails(auditDetails);
            String businessService = petLicense.getBusinessService();
            if (businessService == null)
            {
                businessService = businessService_PL;
                petLicense.setBusinessService(businessService);
            }
            switch (businessService) {
                case businessService_PL:
                    Map<String, Long> taxPeriods = tradeUtil.getTaxPeriods(petLicense, mdmsData);
                    if (petLicense.getLicenseType().equals(PetLicense.LicenseTypeEnum.PERMANENT) || petLicense.getValidTo() == null)
                    	petLicense.setValidTo(taxPeriods.get(PLConstants.MDMS_ENDDATE));
//                    if (!CollectionUtils.isEmpty(petLicense.getPetLicenseDetail().getAccessories()))
//                    	petLicense.getPetLicenseDetail().getAccessories().forEach(accessory -> {
//                            accessory.setTenantId(petLicense.getTenantId());
//                            accessory.setId(UUID.randomUUID().toString());
//                            accessory.setActive(true);
//                        });
                    break;
            }
            petLicense.getPetLicenseDetail().getAddress().setTenantId(petLicense.getTenantId());
            petLicense.getPetLicenseDetail().getAddress().setId(UUID.randomUUID().toString());
//            petLicense.getPetLicenseDetail().getTradeUnits().forEach(tradeUnit -> {
//                tradeUnit.setTenantId(petLicense.getTenantId());
//                tradeUnit.setId(UUID.randomUUID().toString());
//                tradeUnit.setActive(true);
//            });

            if (petLicense.getAction().equalsIgnoreCase(ACTION_APPLY)) {
            	petLicense.getPetLicenseDetail().getApplicationDocuments().forEach(document -> {
                    document.setId(UUID.randomUUID().toString());
                    document.setActive(true);
                });
            }

//            petLicense.getPetLicenseDetail().getOwner().forEach(owner -> {
             
                OwnerInfo owner =  petLicense.getPetLicenseDetail().getOwner();
                owner.setUserActive(true);
                if (!CollectionUtils.isEmpty(owner.getDocuments()))
                    owner.getDocuments().forEach(document -> {
                        document.setId(UUID.randomUUID().toString());
                        document.setActive(true);
                    });
//            });

//            if (petLicense.getPetLicenseDetail().getSubOwnerShipCategory().contains(config.getInstitutional())) {
//                petLicense.getPetLicenseDetail().getInstitution().setId(UUID.randomUUID().toString());
//                petLicense.getPetLicenseDetail().getInstitution().setActive(true);
//                petLicense.getPetLicenseDetail().getInstitution().setTenantId(petLicense.getTenantId());
//                petLicense.getPetLicenseDetail().getOwners().forEach(owner -> {
//                    owner.setInstitutionId(petLicense.getPetLicenseDetail().getInstitution().getId());
//                });
//            }

//            if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
//                petLicense.setAccountId(requestInfo.getUserInfo().getUuid());

        });
        setIdgenIds(petLicenseRequest);
        setStatusForCreate(petLicenseRequest);
        String businessService = petLicenseRequest.getLicenses().isEmpty()?null:petLicenseRequest.getLicenses().get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_PL;
        switch (businessService) {
            case businessService_PL:
                boundaryService.getAreaType(petLicenseRequest, config.getHierarchyTypeCode());
                break;
        }
    }


    /**
     * Returns a list of numbers generated from idgen
     *
     * @param requestInfo RequestInfo from the request
     * @param tenantId    tenantId of the city
     * @param idKey       code of the field defined in application properties for which ids are generated for
     * @param idformat    format in which ids are to be generated
     * @param count       Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }


    /**
     * Sets the ApplicationNumber for given petLicenseRequest
     *
     * @param request PetLicenseRequest which is to be created
     */
    private void setIdgenIds(PetLicenseRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getLicenses().get(0).getTenantId();
        List<PetLicense> licenses = request.getLicenses();
        String businessService = licenses.isEmpty() ? null : licenses.get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_PL;
        List<String> applicationNumbers = null;
        switch (businessService) {
//            case businessService_PL:
//                applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenNameTL(), config.getApplicationNumberIdgenFormatTL(), request.getLicenses().size());
//                break;

            case businessService_BPA:
                applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenNameBPA(), config.getApplicationNumberIdgenFormatBPA(), request.getLicenses().size());
                break;
        }
        ListIterator<String> itr = applicationNumbers.listIterator();

        Map<String, String> errorMap = new HashMap<>();
        if (applicationNumbers.size() != request.getLicenses().size()) {
            errorMap.put("IDGEN ERROR ", "The number of LicenseNumber returned by idgen is not equal to number of PetLicenses");
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);

        licenses.forEach(petLicense -> {
            petLicense.setApplicationNumber(itr.next());
        });
    }


    /**
     * Adds the ownerIds from userSearchReponse to search criteria
     * @param criteria The PetLicense search Criteria
     * @param userDetailResponse The response of user search
     */
    public void enrichTLCriteriaWithOwnerids(PetLicenseSearchCriteria criteria, UserDetailResponse userDetailResponse){
        if(CollectionUtils.isEmpty(criteria.getOwnerIds())){
            Set<String> ownerids = new HashSet<>();
            userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
            criteria.setOwnerIds(new ArrayList<>(ownerids));
        }
    }


    /**
     * Creates search criteria of only the petLicense ids
     * @param licenses The licenses whose ids are to extracted
     * @return The search criteria
     */
    public PetLicenseSearchCriteria getTLSearchCriteriaFromTLIds(List<PetLicense> licenses){
        PetLicenseSearchCriteria criteria = new PetLicenseSearchCriteria();
        List<String> ids = new ArrayList<>();
        licenses.forEach(license -> ids.add(license.getId()));
        criteria.setIds(ids);
        criteria.setTenantId(licenses.get(0).getTenantId());
        criteria.setBusinessService(licenses.get(0).getBusinessService());
        return criteria;
    }


    /**
     * Enriches search criteria with ownerIds from license
     * @param criteria PetLicense search criteria
     * @param licenses The petLicense whose owners are to be enriched
     */
    public PetLicenseSearchCriteria enrichTLSearchCriteriaWithOwnerids(PetLicenseSearchCriteria criteria, List<PetLicense> licenses) {
        PetLicenseSearchCriteria searchCriteria = new PetLicenseSearchCriteria();
        searchCriteria.setTenantId(criteria.getTenantId());
        Set<String> ownerids = new HashSet<>();
        licenses.forEach(license -> {
            ownerids.add(license.getPetLicenseDetail().getOwner().getUuid());
        });

      /*  licenses.forEach(petLicense -> {
            ownerids.add(petLicense.getCitizenInfo().getUuid());
            });*/
        searchCriteria.setBusinessService(licenses.get(0).getBusinessService());
        searchCriteria.setOwnerIds(new ArrayList<>(ownerids));
        return searchCriteria;
    }



    /**
     * Enriches the boundary object in address
     * @param petLicenseRequest The create request
     */
    public void enrichBoundary(PetLicenseRequest petLicenseRequest){
        List<PetLicenseRequest> requests = getRequestByTenantId(petLicenseRequest);
        requests.forEach(tenantWiseRequest -> {
           boundaryService.getAreaType(tenantWiseRequest,config.getHierarchyTypeCode());
        });
    }


    /**
     *
     * @param request
     * @return
     */
    private List<PetLicenseRequest> getRequestByTenantId(PetLicenseRequest request){
        List<PetLicense> licenses = request.getLicenses();
        RequestInfo requestInfo = request.getRequestInfo();

        Map<String,List<PetLicense>> tenantIdToProperties = new HashMap<>();
        if(!CollectionUtils.isEmpty(licenses)){
            licenses.forEach(license -> {
                if(tenantIdToProperties.containsKey(license.getTenantId()))
                    tenantIdToProperties.get(license.getTenantId()).add(license);
                else{
                    List<PetLicense> list = new ArrayList<>();
                    list.add(license);
                    tenantIdToProperties.put(license.getTenantId(),list);
                }
            });
        }
        List<PetLicenseRequest> requests = new LinkedList<>();

        tenantIdToProperties.forEach((key,value)-> {
            requests.add(new PetLicenseRequest(requestInfo,value));
        });
        return requests;
    }



    /**
     * Enriches the owner fields from user search response
     * @param userDetailResponse user search response
     * @param licenses licenses whose owners are to be enriches
     */
    public void enrichOwner(UserDetailResponse userDetailResponse, List<PetLicense> licenses){
        List<OwnerInfo> users = userDetailResponse.getUser();
        Map<String,OwnerInfo> userIdToOwnerMap = new HashMap<>();
        users.forEach(user -> userIdToOwnerMap.put(user.getUuid(),user));
        licenses.forEach(license -> {
            
                    if(userIdToOwnerMap.get(license.getPetLicenseDetail().getOwner().getUuid())==null)
                        throw new CustomException("OWNER SEARCH ERROR","The owner of the tradeCategoryDetail "+license.getPetLicenseDetail().getId()+" is not coming in user search");
                    else
                    	license.getPetLicenseDetail().getOwner().addUserDetail(userIdToOwnerMap.get(license.getPetLicenseDetail().getOwner().getUuid()));
                

           /* if(userIdToOwnerMap.get(license.getCitizenInfo().getUuid())!=null)
                license.getCitizenInfo().addCitizenDetail(userIdToOwnerMap.get(license.getCitizenInfo().getUuid()));
            else
                throw new CustomException("CITIZENINFO ERROR","The citizenInfo of trade License with ApplicationNumber: "+license.getApplicationNumber()+" cannot be found");
*/
        });
    }


    /**
     * Sets status for create request
     * @param petLicenseRequest The create request
     */
    private void setStatusForCreate(PetLicenseRequest petLicenseRequest) {
        petLicenseRequest.getLicenses().forEach(license -> {
            String businessService = petLicenseRequest.getLicenses().isEmpty()?null:petLicenseRequest.getLicenses().get(0).getBusinessService();
            if (businessService == null)
                businessService = businessService_PL;
            switch (businessService) {
                case businessService_PL:
                    if (license.getAction().equalsIgnoreCase(ACTION_INITIATE))
                        license.setStatus(STATUS_INITIATED);
                    if (license.getAction().equalsIgnoreCase(ACTION_APPLY))
                        license.setStatus(STATUS_APPLIED);
                    break;

                case businessService_BPA:
                    license.setStatus(STATUS_INITIATED);
                    break;
            }
        });
    }


    /**
     * Enriches the update request
     * @param petLicenseRequest The input update request
     */
    public void enrichPLUpdateRequest(PetLicenseRequest petLicenseRequest, BusinessService businessService){
        RequestInfo requestInfo = petLicenseRequest.getRequestInfo();
        AuditDetails auditDetails = tradeUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
        petLicenseRequest.getLicenses().forEach(petLicense -> {
            petLicense.setAuditDetails(auditDetails);
            enrichAssignes(petLicense);
            String nameOfBusinessService = petLicense.getBusinessService();
            if(nameOfBusinessService==null)
            {
                nameOfBusinessService=businessService_PL;
                petLicense.setBusinessService(nameOfBusinessService);
            }
//            if ((nameOfBusinessService.equals(businessService_BPA) && (petLicense.getStatus().equalsIgnoreCase(STATUS_INITIATED))) || workflowService.isStateUpdatable(petLicense.getStatus(), businessService)) {
//                petLicense.getPetLicenseDetail().setAuditDetails(auditDetails);
//                if (!CollectionUtils.isEmpty(petLicense.getPetLicenseDetail().getAccessories())) {
//                    petLicense.getPetLicenseDetail().getAccessories().forEach(accessory -> {
//                        if (accessory.getId() == null) {
//                            accessory.setTenantId(petLicense.getTenantId());
//                            accessory.setId(UUID.randomUUID().toString());
//                            accessory.setActive(true);
//                        }
//                    });
//                }

//                petLicense.getPetLicenseDetail().getTradeUnits().forEach(tradeUnit -> {
//                    if (tradeUnit.getId() == null) {
//                        tradeUnit.setTenantId(petLicense.getTenantId());
//                        tradeUnit.setId(UUID.randomUUID().toString());
//                        tradeUnit.setActive(true);
//                    }
//                });

//                petLicense.getPetLicenseDetail().getOwner()=(owner -> {
                    if(petLicense.getPetLicenseDetail().getOwner().getUuid()==null || petLicense.getPetLicenseDetail().getOwner().getUserActive()==null)
                    	petLicense.getPetLicenseDetail().getOwner().setUserActive(true);
                    if (!CollectionUtils.isEmpty(petLicense.getPetLicenseDetail().getOwner().getDocuments()))
                    	petLicense.getPetLicenseDetail().getOwner().getDocuments().forEach(document -> {
                            if (document.getId() == null) {
                                document.setId(UUID.randomUUID().toString());
                                document.setActive(true);
                            }
                        });
//                });

//                if(petLicense.getPetLicenseDetail().getSubOwnerShipCategory().contains(config.getInstitutional())
//                        && petLicense.getPetLicenseDetail().getInstitution().getId()==null){
//                    petLicense.getPetLicenseDetail().getInstitution().setId(UUID.randomUUID().toString());
//                    petLicense.getPetLicenseDetail().getInstitution().setActive(true);
//                    petLicense.getPetLicenseDetail().getInstitution().setTenantId(petLicense.getTenantId());
//                    petLicense.getPetLicenseDetail().getOwners().forEach(owner -> {
//                        owner.setInstitutionId(petLicense.getPetLicenseDetail().getInstitution().getId());
//                    });
//                }

                if(!CollectionUtils.isEmpty(petLicense.getPetLicenseDetail().getApplicationDocuments())){
                    petLicense.getPetLicenseDetail().getApplicationDocuments().forEach(document -> {
                        if(document.getId()==null){
                            document.setId(UUID.randomUUID().toString());
                            document.setActive(true);
                        }
                    });
                }
            
            else {
                if(!CollectionUtils.isEmpty(petLicense.getPetLicenseDetail().getVerificationDocuments())){
                    petLicense.getPetLicenseDetail().getVerificationDocuments().forEach(document -> {
                        if(document.getId()==null){
                            document.setId(UUID.randomUUID().toString());
                            document.setActive(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * Sets the licenseNumber generated by idgen
     * @param request The update request
     */
    private void setLicenseNumberAndIssueDate(PetLicenseRequest request,List<String>endstates) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getLicenses().get(0).getTenantId();
        List<PetLicense> licenses = request.getLicenses();
        int count=0;
        for(int i=0;i<licenses.size();i++){
            PetLicense license = licenses.get(i);
           if((license.getStatus()!=null) && license.getStatus().equalsIgnoreCase(endstates.get(i)))
               count++;
        }
        if(count!=0) {
            List<String> licenseNumbers = null;
            String businessService = licenses.isEmpty()?null:licenses.get(0).getBusinessService();
            if (businessService == null)
                businessService = businessService_PL;
            switch (businessService) {
                case businessService_PL:
                    licenseNumbers=getIdList(requestInfo, tenantId, config.getLicenseNumberIdgenNamePL(), config.getLicenseNumberIdgenFormatPL(), count);
                    break;

                case businessService_BPA:
                    licenseNumbers=getIdList(requestInfo, tenantId, config.getLicenseNumberIdgenNameBPA(), config.getLicenseNumberIdgenFormatBPA(), count);
                    break;
            }
            ListIterator<String> itr = licenseNumbers.listIterator();

            Map<String, String> errorMap = new HashMap<>();
            if (licenseNumbers.size() != count) {
                errorMap.put("IDGEN ERROR ", "The number of LicenseNumber returned by idgen is not equal to number of PetLicenses");
            }

            if (!errorMap.isEmpty())
                throw new CustomException(errorMap);

            for (int i = 0; i < licenses.size(); i++) {
                PetLicense license = licenses.get(i);
                if ((license.getStatus() != null) && license.getStatus().equalsIgnoreCase(endstates.get(i))) {
                    license.setLicenseNumber(itr.next());
                    Long time = System.currentTimeMillis();
                    license.setIssuedDate(time);
                    license.setValidFrom(time);
                }
            }
        }
    }


    /**
     * Adds accountId of the logged in user to search criteria
     * @param requestInfo The requestInfo of searhc request
     * @param criteria The petLicenseSearch criteria
     */
    public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo,PetLicenseSearchCriteria criteria){
        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            criteria.setAccountId(requestInfo.getUserInfo().getUuid());
            criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
            criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
        }

    }

    /**
     * Enriches the petLicenses with ownerInfo and Boundary data
     * @param licenses The licenses to be enriched
     * @param criteria The search criteria of licenses containing the ownerIds
     * @param requestInfo The requestInfo of search
     * @return enriched petLicenses
     */
    public List<PetLicense> enrichPetLicenseSearch(List<PetLicense> licenses, PetLicenseSearchCriteria criteria, RequestInfo requestInfo){

        String businessService = licenses.isEmpty()?null:licenses.get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_PL;
        PetLicenseSearchCriteria searchCriteria = enrichTLSearchCriteriaWithOwnerids(criteria,licenses);
        switch (businessService) {
            case businessService_PL:
                enrichBoundary(new PetLicenseRequest(requestInfo, licenses));
                break;
        }
        UserDetailResponse userDetailResponse = userService.getUser(searchCriteria,requestInfo);
        enrichOwner(userDetailResponse,licenses);
        return licenses;
    }


    /**
     * Enriches the object after status is assigned
     * @param petLicenseRequest The update request
     */
    public void postStatusEnrichment(PetLicenseRequest petLicenseRequest,List<String>endstates){
        setLicenseNumberAndIssueDate(petLicenseRequest,endstates);
    }


    /**
     * Creates search criteria from list of trade license
     * @param licenses The licenses whose ids are to be added to search
     * @return petLicenseSearch criteria on basis of petlicense id
     */
    public PetLicenseSearchCriteria getPetLicenseCriteriaFromIds(List<PetLicense> licenses){
        PetLicenseSearchCriteria criteria = new PetLicenseSearchCriteria();
        Set<String> licenseIds = new HashSet<>();
        licenses.forEach(license -> licenseIds.add(license.getId()));
        criteria.setIds(new LinkedList<>(licenseIds));
        criteria.setBusinessService(licenses.get(0).getBusinessService());
        return criteria;
    }

    /**
     * In case of SENDBACKTOCITIZEN enrich the assignee with the owners and creator of license
     * @param license License to be enriched
     */
    public void enrichAssignes(PetLicense license){

            if(license.getAction().equalsIgnoreCase(CITIZEN_SENDBACK_ACTION)){

                    List<String> assignes = new LinkedList<>();

                    // Adding owners to assignes list
//                    license.getPetLicenseDetail().getOwner().forEach(ownerInfo -> {
                       assignes.add(license.getPetLicenseDetail().getOwner().getUuid());
//                    });

                    // Adding creator of license
//                    if(license.get!=null)
//                        assignes.add(license.getAccountId());

                    license.setAssignee(assignes);
            }
    }




}
