package org.egov.pl.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.producer.Producer;
import org.egov.pl.repository.builder.PLQueryBuilder;
import org.egov.pl.repository.rowmapper.PLRowMapper;
import org.egov.pl.workflow.WorkflowService;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Slf4j
@Repository
public class PLRepository {

    private JdbcTemplate jdbcTemplate;

    private PLQueryBuilder queryBuilder;

    private PLRowMapper rowMapper;

    private Producer producer;

    private PLConfiguration config;

    private WorkflowService workflowService;


    @Autowired
    public PLRepository(JdbcTemplate jdbcTemplate, PLQueryBuilder queryBuilder, PLRowMapper rowMapper,
                        Producer producer, PLConfiguration config, WorkflowService workflowService) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
        this.producer = producer;
        this.config = config;
        this.workflowService = workflowService;
    }


    /**
     * Searhces license in databse
     *
     * @param criteria The tradeLicense Search criteria
     * @return List of TradeLicense from seach
     */
    public List<PetLicense> getLicenses(PetLicenseSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getPLSearchQuery(criteria, preparedStmtList);
        log.info("Query: " + query);
        List<PetLicense> licenses =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        sortChildObjectsById(licenses);
        return licenses;
    }

    /**
     * Pushes the request on save topic
     *
     * @param petLicenseRequest The petLciense create request
     */
    public void save(PetLicenseRequest petLicenseRequest) {
        producer.push(config.getSaveTopic(), petLicenseRequest);
    }
    /**
     * Pushes the update request to update topic or on workflow topic depending on the status
     *
     * @param petLicenseRequest The update requuest
     */
    public void update(PetLicenseRequest petLicenseRequest,Map<String,Boolean> idToIsStateUpdatableMap) {
        RequestInfo requestInfo = petLicenseRequest.getRequestInfo();
        List<PetLicense> licenses = petLicenseRequest.getLicenses();

        List<PetLicense> licesnsesForStatusUpdate = new LinkedList<>();
        List<PetLicense> licensesForUpdate = new LinkedList<>();
        List<PetLicense> licensesForAdhocChargeUpdate = new LinkedList<>();


        for (PetLicense license : licenses) {
            if (idToIsStateUpdatableMap.get(license.getId())) {
                licensesForUpdate.add(license);
            }
//            else if(license.getAction().equalsIgnoreCase(ACTION_ADHOC))
//                licensesForAdhocChargeUpdate.add(license);
            else {
                licesnsesForStatusUpdate.add(license);
            }
        }

        if (!CollectionUtils.isEmpty(licensesForUpdate))
            producer.push(config.getUpdateTopic(), new PetLicenseRequest(requestInfo, licensesForUpdate));

        if (!CollectionUtils.isEmpty(licesnsesForStatusUpdate))
            producer.push(config.getUpdateWorkflowTopic(), new PetLicenseRequest(requestInfo, licesnsesForStatusUpdate));

        if(!licensesForAdhocChargeUpdate.isEmpty())
            producer.push(config.getUpdateAdhocTopic(),new PetLicenseRequest(requestInfo,licensesForAdhocChargeUpdate));

    }




    /**
     * Sorts the child objects by  there ids
     * @param petLicenses The list of petLicenses
     */
    private void sortChildObjectsById(List<PetLicense> petLicenses){
        if(CollectionUtils.isEmpty(petLicenses))
            return;
//        petLicenses.forEach(license -> {
//            license.getPetLicenseDetail().getOwner().sort(Comparator.comparing(User::getUuid));
//            if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getAnimalColor()))
//                license.getPetLicenseDetail().getAnimalColor().sort(Comparator.comparing(Accessory::getId));
//            if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getApplicationDocuments()))
//                license.getPetLicenseDetail().getApplicationDocuments().sort(Comparator.comparing(Document::getId));
//            if(!CollectionUtils.isEmpty(license.getPetLicenseDetail().getVerificationDocuments()))
//                license.getPetLicenseDetail().getVerificationDocuments().sort(Comparator.comparing(Document::getId));
//        });
    }


}
