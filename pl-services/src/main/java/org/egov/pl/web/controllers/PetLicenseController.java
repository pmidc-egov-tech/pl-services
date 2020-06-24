package org.egov.pl.web.controllers;


import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.models.PetLicenseResponse;
import org.egov.pl.models.PetLicenseSearchCriteria;
import org.egov.pl.models.RequestInfoWrapper;
import org.egov.pl.service.PaymentUpdateService;
import org.egov.pl.service.PetLicenseService;
import org.egov.pl.service.notification.PaymentNotificationService;
import org.egov.pl.util.ResponseInfoFactory;
import org.egov.pl.web.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import javax.validation.constraints.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;

@RestController
    @RequestMapping("/v1")
    public class PetLicenseController {

        private final ObjectMapper objectMapper;

        private final HttpServletRequest request;

        private final PetLicenseService petLicenseService;

        private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public PetLicenseController(ObjectMapper objectMapper, HttpServletRequest request,
                                  PetLicenseService petLicenseService, ResponseInfoFactory responseInfoFactory) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.petLicenseService = petLicenseService;
        this.responseInfoFactory = responseInfoFactory;
    }


    @PostMapping({"/{servicename}/_create", "/_create"})
    public ResponseEntity<PetLicenseResponse> create(@Valid @RequestBody PetLicenseRequest petLicenseRequest,
                                                       @PathVariable(required = false) String servicename) {
        List<PetLicense> licenses = petLicenseService.create(petLicenseRequest, servicename);
        PetLicenseResponse response = PetLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(petLicenseRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{servicename}/_search", "/_search"}, method = RequestMethod.POST)
    public ResponseEntity<PetLicenseResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
                                                       @Valid @ModelAttribute PetLicenseSearchCriteria criteria,
                                                       @PathVariable(required = false) String servicename) {
        List<PetLicense> licenses = petLicenseService.search(criteria, requestInfoWrapper.getRequestInfo(), servicename);

        PetLicenseResponse response = PetLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = {"/{servicename}/_update", "/_update"}, method = RequestMethod.POST)
    public ResponseEntity<PetLicenseResponse> update(@Valid @RequestBody PetLicenseRequest petLicenseRequest,
                                                       @PathVariable(required = false) String servicename) {
        List<PetLicense> licenses = petLicenseService.update(petLicenseRequest, servicename);

        PetLicenseResponse response = PetLicenseResponse.builder().licenses(licenses).responseInfo(
                responseInfoFactory.createResponseInfoFromRequestInfo(petLicenseRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
