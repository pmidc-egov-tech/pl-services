package org.egov.pl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@Import({TracerConfiguration.class})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class PLConfiguration {


    @Value("${app.timezone}")
    private String timeZone;

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

    @Bean
    @Autowired
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    @Value("${egov.user.username.prefix}")
    private String usernamePrefix;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.idgen.pl.applicationNum.name}")
    private String applicationNumberIdgenNamePL;

    @Value("${egov.idgen.pl.applicationNum.format}")
    private String applicationNumberIdgenFormatPL;

    @Value("${egov.idgen.pl.licensenumber.name}")
    private String licenseNumberIdgenNamePL;

    @Value("${egov.idgen.pl.licensenumber.format}")
    private String licenseNumberIdgenFormatPL;

    @Value("${egov.idgen.bpa.applicationNum.name}")
    private String applicationNumberIdgenNameBPA;

    @Value("${egov.idgen.bpa.applicationNum.format}")
    private String applicationNumberIdgenFormatBPA;

    @Value("${egov.idgen.bpa.licensenumber.name}")
    private String licenseNumberIdgenNameBPA;

    @Value("${egov.idgen.bpa.licensenumber.format}")
    private String licenseNumberIdgenFormatBPA;

    //Persister Config
    @Value("${persister.save.tradelicense.topic}")
    private String saveTopic;

    @Value("${persister.update.tradelicense.topic}")
    private String updateTopic;

    @Value("${persister.update.tradelicense.workflow.topic}")
    private String updateWorkflowTopic;

    @Value("${persister.update.tradelicense.adhoc.topic}")
    private String updateAdhocTopic;


    //Location Config
    @Value("${egov.location.host}")
    private String locationHost;

    @Value("${egov.location.context.path}")
    private String locationContextPath;

    @Value("${egov.location.endpoint}")
    private String locationEndpoint;

    @Value("${egov.location.hierarchyTypeCode}")
    private String hierarchyTypeCode;

    @Value("${egov.pl.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.pl.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.pl.max.limit}")
    private Integer maxSearchLimit;

    
    
    // tradelicense Calculator
    @Value("${egov.pl.calculator.host}")
    private String calculatorHost;

    @Value("${egov.pl.calculator.calculate.endpoint}")
    private String calculateEndpointPL;

    @Value("${egov.bpa.calculator.calculate.endpoint}")
    private String calculateEndpointBPA;

    @Value("${egov.pl.calculator.getBill.endpoint}")
    private String getBillEndpoint;

    //Institutional key word
    @Value("${egov.ownershipcategory.institutional}")
    private String institutional;


    @Value("${egov.receipt.businessservicePL}")
    private String businessServicePL;


    @Value("${egov.receipt.businessserviceBPA}")
    private String businessServiceBPA;

    //Property Service
    @Value("${egov.property.service.host}")
    private String propertyHost;

    @Value("${egov.property.service.context.path}")
    private String propertyContextPath;

    @Value("${egov.property.endpoint}")
    private String propertySearchEndpoint;


    //SMS
    @Value("${kafka.topics.notification.sms}")
    private String smsNotifTopic;

    @Value("${notification.sms.enabled.forPL}")
    private Boolean isPLSMSEnabled;

    @Value("${notification.sms.enabled.forBPA}")
    private Boolean isBPASMSEnabled;

    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.localization.statelevel}")
    private Boolean isLocalizationStateLevel;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;


    //Allowed Search Parameters
    @Value("${citizen.allowed.search.params}")
    private String allowedCitizenSearchParameters;

    @Value("${employee.allowed.search.params}")
    private String allowedEmployeeSearchParameters;


    @Value("${egov.pl.previous.allowed}")
    private Boolean isPreviousPLAllowed;

    @Value("${egov.pl.min.period}")
    private Long minPeriod;


    // Workflow
    @Value("${create.pl.workflow.name}")
    private String plBusinessServiceValue;

    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;


    @Value("${is.external.workflow.enabled}")
    private Boolean isExternalWorkFlowEnabled;

    //USER EVENTS
    @Value("${egov.ui.app.host}")
    private String uiAppHost;

    @Value("${egov.usr.events.create.topic}")
    private String saveUserEventsTopic;

    @Value("${egov.usr.events.pay.link}")
    private String payLink;

    @Value("${egov.usr.events.pay.code}")
    private String payCode;

    @Value("${egov.user.event.notification.enabledForPL}")
    private Boolean isUserEventsNotificationEnabledForPL;

    @Value("${egov.user.event.notification.enabledForBPA}")
    private Boolean isUserEventsNotificationEnabledForBPA;

    @Value("${egov.usr.events.pay.triggers}")
    private String payTriggers;


}
