package org.egov.pl.service.notification;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.models.PetLicenseRequest;
import org.egov.pl.models.SMSRequest;
import org.egov.pl.repository.ServiceRequestRepository;
import org.egov.pl.util.BPAConstants;
import org.egov.pl.util.BPANotificationUtil;
import org.egov.pl.util.NotificationUtil;
import org.egov.pl.util.PLConstants;
import org.egov.pl.models.*;
import org.egov.pl.web.models.uservevents.EventRequest;
import org.egov.pl.config.PLConfiguration;
import org.egov.pl.service.notification.PLNotificationService;
import org.egov.pl.util.PLConstants;
import org.egov.pl.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.pl.util.BPAConstants.NOTIFICATION_APPROVED;
import static org.egov.pl.util.PLConstants.businessService_BPA;
import static org.egov.pl.util.PLConstants.businessService_PL;


@Slf4j
@Service
public class PLNotificationService {


    private PLConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private NotificationUtil util;

	private BPANotificationUtil bpaNotificationUtil;

	@Autowired
	public PLNotificationService(PLConfiguration config, ServiceRequestRepository serviceRequestRepository, NotificationUtil util, BPANotificationUtil bpaNotificationUtil) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.util = util;
		this.bpaNotificationUtil = bpaNotificationUtil;
	}

    /**
     * Creates and send the sms based on the tradeLicenseRequest
     * @param request The tradeLicenseRequest listenend on the kafka topic
     */
    public void process(PetLicenseRequest request){

        String businessService = request.getLicenses().isEmpty()?null:request.getLicenses().get(0).getBusinessService();
		if (businessService == null)
			businessService = businessService_PL;
		switch(businessService)
		{
			case businessService_PL:
				List<SMSRequest> smsRequestsPL = new LinkedList<>();
				if(null != config.getIsPLSMSEnabled()) {
					if(config.getIsPLSMSEnabled()) {
						enrichSMSRequest(request,smsRequestsPL);
						if(!CollectionUtils.isEmpty(smsRequestsPL))
							util.sendSMS(smsRequestsPL,true);
					}
				}
				if(null != config.getIsUserEventsNotificationEnabledForPL()) {
//					if(config.getIsUserEventsNotificationEnabledForPL()) {
//						EventRequest eventRequest = getEventsForPL(request);
//						if(null != eventRequest)
//							util.sendEventNotification(eventRequest);
//					}
				}
				break;

			case businessService_BPA:
				List<SMSRequest> smsRequestsBPA = new LinkedList<>();
				if (null != config.getIsBPASMSEnabled()) {
					if (config.getIsBPASMSEnabled()) {
						enrichSMSRequest(request, smsRequestsBPA);
						if (!CollectionUtils.isEmpty(smsRequestsBPA))
							util.sendSMS(smsRequestsBPA, true);
					}
				}
				if(null != config.getIsUserEventsNotificationEnabledForBPA()) {
					if(config.getIsUserEventsNotificationEnabledForBPA()) {
						EventRequest eventRequest = getEventsForBPA(request, false, null);
//						if(null != eventRequest)
//							util.sendEventNotification(eventRequest);
					}
				}
				break;
		}
    }


    /**
     * Enriches the smsRequest with the customized messages
     * @param request The tradeLicenseRequest from kafka topic
     * @param smsRequests List of SMSRequets
     */
    private void enrichSMSRequest(PetLicenseRequest request,List<SMSRequest> smsRequests){
        String tenantId = request.getLicenses().get(0).getTenantId();
        for(PetLicense license : request.getLicenses()){
			String businessService = license.getBusinessService();
			if (businessService == null)
				businessService = businessService_PL;
			String message = null;
			if (businessService.equals(businessService_PL)) {
				String localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
				message = util.getCustomizedMsg(request.getRequestInfo(), license, localizationMessages);
			} else {
				String localizationMessages = bpaNotificationUtil.getLocalizationMessages(tenantId, request.getRequestInfo());
				message = bpaNotificationUtil.getCustomizedMsg(request.getRequestInfo(), license, localizationMessages);
			}
            if(message==null) continue;

            Map<String,String > mobileNumberToOwner = new HashMap<>();

//            license.getPetLicenseDetail().getOwner().forEach(owner -> {
                if(license.getPetLicenseDetail().getOwner().getMobileNumber()!=null)
                    mobileNumberToOwner.put(license.getPetLicenseDetail().getOwner().getMobileNumber(),license.getPetLicenseDetail().getOwner().getName());
//            });
            smsRequests.addAll(util.createSMSRequest(message,mobileNumberToOwner));
        }
    }
    
    /**
     * Creates and registers an event at the egov-user-event service at defined trigger points as that of sms notifs.
     * 
     * Assumption - The PetLicenseRequest received will always contain only one PetLicense.
     * 
     * @param request
     * @return
     */
    private EventRequest getEventsForTL(PetLicenseRequest request) {
    	List<Event> events = new ArrayList<>();
        String tenantId = request.getLicenses().get(0).getTenantId();
        String localizationMessages = util.getLocalizationMessages(tenantId,request.getRequestInfo());
        for(PetLicense license : request.getLicenses()){

            String message = util.getCustomizedMsg(request.getRequestInfo(), license, localizationMessages);
            if(message == null) continue;
            Map<String,String > mobileNumberToOwner = new HashMap<>();
//            license.getPetLicenseDetail().getOwners().forEach(owner -> {
                if(license.getPetLicenseDetail().getOwner().getMobileNumber()!=null)
                    mobileNumberToOwner.put(license.getPetLicenseDetail().getOwner().getMobileNumber(),license.getPetLicenseDetail().getOwner().getName());
//            });
            List<SMSRequest> smsRequests = util.createSMSRequest(message,mobileNumberToOwner);
        	Set<String> mobileNumbers = smsRequests.stream().map(SMSRequest :: getMobileNumber).collect(Collectors.toSet());
        	Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, request.getRequestInfo(), request.getLicenses().get(0).getTenantId());
    		if (CollectionUtils.isEmpty(mapOfPhnoAndUUIDs.keySet())) {
    			log.info("UUID search failed!");
    			continue;
    		}
            Map<String,String > mobileNumberToMsg = smsRequests.stream().collect(Collectors.toMap(SMSRequest::getMobileNumber, SMSRequest::getMessage));		
            for(String mobile: mobileNumbers) {
    			if(null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberToMsg.get(mobile)) {
    				log.error("No UUID/SMS for mobile {} skipping event", mobile);
    				continue;
    			}
    			List<String> toUsers = new ArrayList<>();
    			toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
    			Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
    			List<String> payTriggerList = Arrays.asList(config.getPayTriggers().split("[,]"));
    			Action action = null;
    			if(payTriggerList.contains(license.getStatus())) {
                    List<ActionItem> items = new ArrayList<>();
        			String actionLink = config.getPayLink().replace("$mobile", mobile)
        						.replace("$applicationNo", license.getApplicationNumber())
        						.replace("$tenantId", license.getTenantId());
        			actionLink = config.getUiAppHost() + actionLink;
        			ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
        			items.add(item);
        			action = Action.builder().actionUrls(items).build();
    			}

				
				events.add(Event.builder().tenantId(license.getTenantId()).description(mobileNumberToMsg.get(mobile))
						.eventType(PLConstants.USREVENTS_EVENT_TYPE).name(PLConstants.USREVENTS_EVENT_NAME)
						.postedBy(PLConstants.USREVENTS_EVENT_POSTEDBY).source(Source.WEBAPP).recepient(recepient)
						.eventDetails(null).actions(action).build());
    			
    		}
        }
        if(!CollectionUtils.isEmpty(events)) {
    		return null;//EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
        }else {
        	return null;
        }
		
    }

	public EventRequest getEventsForBPA(PetLicenseRequest request, boolean isStatusPaid, String paidMessage) {
		List<Event> events = new ArrayList<>();
		String tenantId = request.getLicenses().get(0).getTenantId();
		for(PetLicense license : request.getLicenses()){
			String message = null;
			if(isStatusPaid)
			{
				message = paidMessage;
			}
			else {
				String localizationMessages = bpaNotificationUtil.getLocalizationMessages(tenantId,request.getRequestInfo());
				message = bpaNotificationUtil.getCustomizedMsg(request.getRequestInfo(), license, localizationMessages);
			}
			if(message == null) continue;
			Map<String,String > mobileNumberToOwner = new HashMap<>();
//			license.getPetLicenseDetail().getOwner().forEach(owner -> {
				if(license.getPetLicenseDetail().getOwner().getMobileNumber()!=null)
					mobileNumberToOwner.put(license.getPetLicenseDetail().getOwner().getMobileNumber(),license.getPetLicenseDetail().getOwner().getName());
//			});
			List<SMSRequest> smsRequests = util.createSMSRequest(message,mobileNumberToOwner);
			Set<String> mobileNumbers = smsRequests.stream().map(SMSRequest :: getMobileNumber).collect(Collectors.toSet());
			Map<String, String> mapOfPhnoAndUUIDs = fetchUserUUIDs(mobileNumbers, request.getRequestInfo(), request.getLicenses().get(0).getTenantId());
			if (CollectionUtils.isEmpty(mapOfPhnoAndUUIDs.keySet())) {
				log.info("UUID search failed!");
				continue;
			}
			Map<String,String > mobileNumberToMsg = smsRequests.stream().collect(Collectors.toMap(SMSRequest::getMobileNumber, SMSRequest::getMessage));
			for(String mobile: mobileNumbers) {
				if(null == mapOfPhnoAndUUIDs.get(mobile) || null == mobileNumberToMsg.get(mobile)) {
					log.error("No UUID/SMS for mobile {} skipping event", mobile);
					continue;
				}
				List<String> toUsers = new ArrayList<>();
				toUsers.add(mapOfPhnoAndUUIDs.get(mobile));
				Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
				List<String> payTriggerList = Arrays.asList(config.getPayTriggers().split("[,]"));
				Action action = null;
				if(payTriggerList.contains(license.getStatus()) && !isStatusPaid) {
					List<ActionItem> items = new ArrayList<>();
					String actionLink = config.getPayLink().replace("$mobile", mobile)
							.replace("$applicationNo", license.getApplicationNumber())
							.replace("$tenantId", license.getTenantId());
					actionLink = config.getUiAppHost() + actionLink;
					ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
					items.add(item);
					action = Action.builder().actionUrls(items).build();
				}


				events.add(Event.builder().tenantId(license.getTenantId()).description(mobileNumberToMsg.get(mobile))
						.eventType(BPAConstants.USREVENTS_EVENT_TYPE).name(BPAConstants.USREVENTS_EVENT_NAME)
						.postedBy(BPAConstants.USREVENTS_EVENT_POSTEDBY).source(Source.WEBAPP).recepient(recepient)
						.eventDetails(null).actions(action).build());

			}
		}
		if(!CollectionUtils.isEmpty(events)) {
			return null;//EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
		}else {
			return null;
		}

	}
    
    
    
    /**
     * Fetches UUIDs of CITIZENs based on the phone number.
     * 
     * @param mobileNumbers
     * @param requestInfo
     * @param tenantId
     * @return
     */
    private Map<String, String> fetchUserUUIDs(Set<String> mobileNumbers, RequestInfo requestInfo, String tenantId) {
    	Map<String, String> mapOfPhnoAndUUIDs = new HashMap<>();
    	StringBuilder uri = new StringBuilder();
    	uri.append(config.getUserHost()).append(config.getUserSearchEndpoint());
    	Map<String, Object> userSearchRequest = new HashMap<>();
    	userSearchRequest.put("RequestInfo", requestInfo);
		userSearchRequest.put("tenantId", tenantId);
		userSearchRequest.put("userType", "CITIZEN");
    	for(String mobileNo: mobileNumbers) {
    		userSearchRequest.put("userName", mobileNo);
    		try {
    			Object user = serviceRequestRepository.fetchResult(uri, userSearchRequest);
    			if(null != user) {
    				String uuid = JsonPath.read(user, "$.user[0].uuid");
    				mapOfPhnoAndUUIDs.put(mobileNo, uuid);
    			}else {
        			log.error("Service returned null while fetching user for username - "+mobileNo);
    			}
    		}catch(Exception e) {
    			log.error("Exception while fetching user for username - "+mobileNo);
    			log.error("Exception trace: ",e);
    			continue;
    		}
    	}
    	return mapOfPhnoAndUUIDs;
    }

}