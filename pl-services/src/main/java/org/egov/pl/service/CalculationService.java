package org.egov.pl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.repository.ServiceRequestRepository;
import org.egov.pl.util.AnimalCategoryUtil;
import org.egov.pl.web.models.calculation.Calculation;
import org.egov.pl.web.models.calculation.CalculationReq;
import org.egov.pl.web.models.calculation.CalculationRes;
import org.egov.pl.web.models.calculation.CalulationCriteria;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Service
public class CalculationService {

    private AnimalCategoryUtil utils;

    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;


    @Autowired
    public CalculationService(AnimalCategoryUtil utils, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
        this.utils = utils;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }


    /**
     * Adds the calculation object to the request
     * @param request The input create or update request
     * @return request with calculation object added
     */
    public List<PetLicense> addCalculation(PetLicenseRequest request){
        RequestInfo requestInfo = request.getRequestInfo();
        List<PetLicense> licenses = request.getLicenses();

        if(CollectionUtils.isEmpty(licenses))
            throw new CustomException("INVALID REQUEST","The request for calculation cannot be empty or null");

        CalculationRes response = getCalculation(requestInfo,licenses);
        List<Calculation> calculations = response.getCalculations();
        Map<String,Calculation> applicationNumberToCalculation = new HashMap<>();
        calculations.forEach(calculation -> {
            applicationNumberToCalculation.put(calculation.getPetLicense().getApplicationNumber(),calculation);
            calculation.setPetLicense(null);
        });

        licenses.forEach(license ->{
            license.setCalculation(applicationNumberToCalculation.get(license.getApplicationNumber()));
        });

        return licenses;
    }


    /**
     * Call tl-calculator to get the calculation Object for the given license
     * @param requestInfo The requestInfo of the incoming request
     * @param licenses The tradeLicense to whom the calculation object has to be added
     * @return CalculationRes from the tl-calculator
     */
    private CalculationRes getCalculation(RequestInfo requestInfo,List<PetLicense> licenses){
        StringBuilder uri = utils.getCalculationURI(licenses.get(0).getBusinessService());
        List<CalulationCriteria> criterias = new LinkedList<>();

        licenses.forEach(license -> {
            criterias.add(new CalulationCriteria(license,license.getApplicationNumber(),license.getTenantId()));
        });

        CalculationReq request = CalculationReq.builder().calulationCriteria(criterias)
                .requestInfo(requestInfo)
                .build();

        Object result = serviceRequestRepository.fetchResult(uri,request);
        CalculationRes response = null;
        try{
            response = mapper.convertValue(result,CalculationRes.class);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of calculate");
        }
        return response;
    }

}
