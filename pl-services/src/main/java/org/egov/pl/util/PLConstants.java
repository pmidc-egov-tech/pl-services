package org.egov.pl.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PLConstants {
	
	public static final String businessService_PL = "PL";

    public static final String MODULE = "rainmaker-pl";

    public static final String NOTIFICATION_LOCALE = "en_IN";

    public static final String NOTIFICATION_INITIATED = "pl.en.counter.initiate";

    public static final String NOTIFICATION_APPLIED = "pl.en.counter.submit";

    public static final String NOTIFICATION_FIELD_INSPECTION = "pl.en.field.inspection";

    public static final String NOTIFICATION_PAYMENT_OWNER = "pl.en.counter.payment.successful.owner";

    public static final String NOTIFICATION_PAYMENT_PAYER = "pl.en.counter.payment.successful.payer";

    public static final String NOTIFICATION_PAID = "pl.en.counter.pending.approval";

    public static final String NOTIFICATION_APPROVED = "pl.en.counter.approved";

    public static final String NOTIFICATION_REJECTED = "pl.en.counter.rejected";

    public static final String NOTIFICATION_CANCELLED = "pl.en.counter.cancelled";

    public static final String NOTIFICATION_FIELD_CHANGED = "pl.en.edit.field.change";

    public static final String NOTIFICATION_OBJECT_ADDED = "pl.en.edit.object.added";

    public static final String NOTIFICATION_OBJECT_REMOVED = "pl.en.edit.object.removed";

    public static final String NOTIFICATION_OBJECT_MODIFIED = "pl.en.edit.object.modified";

    public static final String NOTIFICATION_SENDBACK_CITIZEN = "pl.en.sendback.citizen";

    public static final String NOTIFICATION_FORWARD_CITIZEN = "pl.en.forward.citizen";


    public static final String DEFAULT_OBJECT_MODIFIED_MSG = "Dear <1>,Your Pet License with application number <APPLICATION_NUMBER> was modified.";


    // MDMS

    public static final String PET_LICENSE_MODULE = "PetLicense";

    public static final String PET_LICENSE_MODULE_CODE = "PL";

    public static final String COMMON_MASTERS_MODULE = "common-masters";


    // mdms master names

    public static final String 	ANIMAL_CATEGORY = "AnimalCategory";
    
    public static final String 	ANIMAL_SUB_CATEGORY = "AnimalSubCategory";

    public static final String LICENSE_TYPE = "LicenseType";

    public static final String ANIMAL_COLOR = "AnimalColor";


    // mdms path codes

    public static final String PL_JSONPATH_CODE = "$.MdmsRes.PetLicense";

    public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";

    public static final String LICENSETYPE_JSONPATH_CODE = "$.MdmsRes.PetLicense.LicenseType.*.code";

    public static final String ANIMAL_CATAGORY_JSONPATH_UOM = "$.MdmsRes.PetLicense.AnimalCategory.*.code";

    public static final String ANIMAL_SUB_CATAGORY_JSONPATH_CODE = "$.MdmsRes.PetLicense.AnimalSubCategory.*.code";

    public static final String ANIMAL_COLOR_JSONPATH_UOM = "$.MdmsRes.PetLicense.AnimalColor.*.code";


    //FINANCIAL YEAR

    public static final String MDMS_EGF_MASTER = "egf-master";

    public static final String MDMS_FINANCIALYEAR = "FinancialYear";

    public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

    public static final String MDMS_STARTDATE = "startingDate";

    public static final String MDMS_ENDDATE = "endingDate";

    // error constants

    public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";

    public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";


    // PL actions

    public static final String ACTION_INITIATE = "INITIATE";

    public static final String ACTION_APPLY = "APPLY";

    public static final String ACTION_APPROVE = "APPROVE";

    public static final String ACTION_REJECT = "REJECT";

    public static final String TRIGGER_NOWORKFLOW = "NOWORKFLOW";

    public static final String ACTION_CANCEL = "CANCEL";

    public static final String ACTION_PAY = "PAY";

    public static final String STATUS_INITIATED = "INITIATED";

    public static final String STATUS_APPLIED = "APPLIED";

    public static final String STATUS_APPROVED = "APPROVED";

    public static final String STATUS_REJECTED = "REJECTED";

    public static final String STATUS_FIELDINSPECTION = "FIELDINSPECTION";

    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String STATUS_PAID = "PAID";

    public static final String BILL_AMOUNT_JSONPATH = "$.billResponse.Bill[0].totalAmount";


    // ACTION_STATUS combinations for notification

    public static final String ACTION_STATUS_INITIATED = "INITIATE_INITIATED";

    public static final String ACTION_STATUS_APPLIED = "APPLY_APPLIED";

    public static final String ACTION_STATUS_APPROVED = "APPROVE_PENDINGPAYMENT";

    public static final String ACTION_STATUS_REJECTED = "REJECT_REJECTED";

    public static final String ACTION_STATUS_FIELDINSPECTION = "FORWARD_FIELDINSPECTION";

    public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

    public static final String ACTION_SENDBACKTOCITIZEN_FIELDINSPECTION = "SENDBACKTOCITIZEN_CITIZENACTIONREQUIRED";

    public static final String ACTION_FORWARD_CITIZENACTIONREQUIRED = "FORWARDTOEMPLOYEE_FIELDINSPECTION";

    public static final String ACTION_STATUS_PAID = "PAID";

    public static final String VARIABLE_ACTION = "action";

    public static final String VARIABLE_WFDOCUMENTS = "wfDocuments";

    public static final String VARIABLE_ACTIVE = "active";

    public static final String VARIABLE_USERACTIVE = "userActive";

    public static final String VARIABLE_CREATEDBY = "createdBy";

    public static final String VARIABLE_LASTMODIFIEDBY = "lastModifiedBy";

    public static final String VARIABLE_CREATEDTIME = "createdTime";

    public static final String VARIABLE_LASTMODIFIEDTIME = "lastModifiedTime";

    public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
    
    public static final String USREVENTS_EVENT_NAME = "Pet License";
    
    public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-PL";

    public static final String CITIZEN_SENDBACK_ACTION = "SENDBACKTOCITIZEN";


    public static final List<String> FIELDS_TO_IGNORE = Collections.unmodifiableList(Arrays.asList(VARIABLE_ACTION, VARIABLE_WFDOCUMENTS,
            VARIABLE_CREATEDBY, VARIABLE_LASTMODIFIEDBY, VARIABLE_CREATEDTIME, VARIABLE_LASTMODIFIEDTIME));


    public PLConstants() {
    }


}
