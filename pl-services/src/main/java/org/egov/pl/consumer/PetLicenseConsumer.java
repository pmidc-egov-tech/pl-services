package org.egov.pl.consumer;

import java.util.HashMap;

import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.service.PetLicenseService;
import org.egov.pl.service.notification.PLNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class PetLicenseConsumer {

    private PLNotificationService notificationService;

    private PetLicenseService petLicenseService;

    @Autowired
    public PetLicenseConsumer(PLNotificationService notificationService, PetLicenseService petLicenseService) {
        this.notificationService = notificationService;
        this.petLicenseService = petLicenseService;
    }

    @KafkaListener(topics = {"${persister.update.petlicense.topic}","${persister.save.petlicense.topic}","${persister.update.petlicense.workflow.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        PetLicenseRequest petLicenseRequest = new PetLicenseRequest();
        try {
            log.info("Consuming record: " + record);
            petLicenseRequest = mapper.convertValue(record, PetLicenseRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        log.info("PetLicense Received: "+petLicenseRequest.getLicenses().get(0).getApplicationNumber());
   
       // notificationService.process(petLicenseRequest);
    }
}
