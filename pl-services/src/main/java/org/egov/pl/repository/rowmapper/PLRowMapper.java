package org.egov.pl.repository.rowmapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pl.models.AuditDetails;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.Address;
import org.egov.pl.models.AnimalColor;
import org.egov.pl.models.Boundary;
import org.egov.pl.models.Document;
import org.egov.pl.models.OwnerInfo;
import org.egov.pl.models.PetLicense;
import org.egov.pl.models.PetLicenseDetail;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class PLRowMapper  implements ResultSetExtractor<List<PetLicense>> {

	 @Autowired
	    private ObjectMapper mapper;



	    public List<PetLicense> extractData(ResultSet rs) throws SQLException, DataAccessException {

	        Map<String, PetLicense> petLicenseMap = new HashMap<>();

	        while (rs.next()) {
	            String id = rs.getString("pl_id");
	            PetLicense currentPetLicense = petLicenseMap.get(id);
	            String tenantId = rs.getString("pl_tenantId");

	            if(currentPetLicense == null){
	                Long lastModifiedTime = rs.getLong("pl_lastModifiedTime");
	                if(rs.wasNull()){lastModifiedTime = null;}
	                Long issuedDate = (Long) rs.getObject("issueddate");
	                Long validFrom = (Long) rs.getObject("validfrom");
	                Long validTo = (Long) rs.getObject("validto");
	                Long applicationDate = (Long) rs.getObject("applicationdate");

	                AuditDetails auditdetails = AuditDetails.builder()
	                        .createdBy(rs.getString("pl_createdBy"))
	                        .createdTime(rs.getLong("pl_createdTime"))
	                        .lastModifiedBy(rs.getString("pl_lastModifiedBy"))
	                        .lastModifiedTime(lastModifiedTime)
	                        .build();

	                currentPetLicense = PetLicense.builder().auditDetails(auditdetails)
	                        .licenseNumber(rs.getString("licensenumber"))
	                        .licenseType(PetLicense.LicenseTypeEnum.fromValue(rs.getString("licensetype")))
	                        .oldLicenseNumber(rs.getString("oldlicensenumber"))
	                        .applicationDate(applicationDate)
	                        .applicationNumber(rs.getString("applicationnumber"))
	                        .issuedDate(issuedDate)
	                        .validFrom(validFrom)
	                        .validTo(validTo)
	                        .action(rs.getString("action"))
	                        .status(rs.getString("status"))
	                        .tenantId(tenantId)
	                        .businessService(rs.getString("businessservice"))
	                        .id(id)
	                        .build();

	                petLicenseMap.put(id,currentPetLicense);
	            }
	            addChildrenToProperty(rs, currentPetLicense);

	        }

	        return new ArrayList<>(petLicenseMap.values());

	    }



	    private void addChildrenToProperty(ResultSet rs, PetLicense petLicense) throws SQLException {

	        String tenantId = petLicense.getTenantId();
	        String petLicenseDetailId = rs.getString("pld_id");
	        if(petLicense.getPetLicenseDetail()==null){

	            Boundary locality = Boundary.builder().code(rs.getString("locality"))
	                    .build();

	            Double latitude = (Double) rs.getObject("latitude");
	            Double longitude = (Double) rs.getObject("longitude");

	            Address address = Address.builder().addressId(rs.getString("addressId"))
	                    .addressLine1(rs.getString("addressLine1"))
	                    .addressLine2(rs.getString("addressLine2"))
	                    .addressNumber(rs.getString("addressNumber"))
	                    .buildingName(rs.getString("buildingName"))
	                    .city(rs.getString("city"))
	                    .detail(rs.getString("detail"))
	                    .id(rs.getString("tl_ad_id"))
	                    .landmark(rs.getString("landmark"))
	                    .latitude(latitude)
	                    .locality(locality)
	                    .longitude(longitude)
	                    .pincode(rs.getString("pincode"))
	                    .doorNo(rs.getString("doorno"))
	                    .street(rs.getString("street"))
	                    .tenantId(tenantId)
	                    .type(rs.getString("type"))
	                    .build();

			/*
			 * Institution institution = null; if(rs.getString("instiid")!=null &&
			 * rs.getBoolean("instiactive")) { institution = Institution.builder()
			 * .id(rs.getString("instiid")) .tenantId(rs.getString("institenantId"))
			 * .name(rs.getString("name")) .type(rs.getString("institutionType"))
			 * .designation(rs.getString("designation"))
			 * .active(rs.getBoolean("instiactive"))
			 * .contactNo(rs.getString("insticontactno"))
			 * .instituionName(rs.getString("instiinstituionname"))
			 * .organisationRegistrationNo(rs.getString("instiorganisationregistrationno"))
			 * .address(rs.getString("address")) .build(); }
			 */

	            AuditDetails auditdetails = AuditDetails.builder()
	                    .createdBy(rs.getString("pld_createdBy"))
	                    .createdTime(rs.getLong("pld_createdTime"))
	                    .lastModifiedBy(rs.getString("pld_lastModifiedBy"))
	                    .lastModifiedTime(rs.getLong("pld_createdTime"))
	                    .build();

	            Double operationalArea = (Double) rs.getObject("operationalArea");
	            Integer noOfEmployees = (Integer) rs.getObject("noOfEmployees");
	            PGobject pgObj = (PGobject) rs.getObject("additionaldetail");
	            try {
	                PetLicenseDetail petLicenseDetail = PetLicenseDetail.builder()
	                        .channel(PetLicenseDetail.ChannelEnum.fromValue(rs.getString("channel")))
	                        .animalAge(rs.getInt("animalAge"))
	                        .address(address)
	                        .auditDetails(auditdetails)
	                        .build();

	                if(pgObj!=null){
	                    JsonNode additionalDetail = mapper.readTree(pgObj.getValue());
	                    petLicenseDetail.setAdditionalDetail(additionalDetail);
	                }

	                petLicense.setPetLicenseDetail(petLicenseDetail);
	            }
	            catch (IOException e){
	                throw new CustomException("PARSING ERROR","The additionalDetail json cannot be parsed");
	            }
	        }

//	        if(rs.getString("pl_un_id")!=null && rs.getBoolean("pl_un_active")){
//	            TradeUnit tradeUnit = TradeUnit.builder()
//	                    .tradeType(rs.getString("pl_un_tradeType"))
//	                    .uom(rs.getString("pl_un_uom"))
//	                    .id(rs.getString("pl_un_id"))
//	                    .uomValue(rs.getString("pl_un_uomvalue"))
//	                    .tenantId(tenantId)
//	                    .active(rs.getBoolean("pl_un_active"))
//	                    .build();
//	            petLicense.getPetLicenseDetail().addTradeUnitsItem(tradeUnit);
//	        }

//	        if(rs.getString("pl_acc_id")!=null && rs.getBoolean("pl_acc_active")) {
//	            Integer count = rs.getInt("count");
//	            if(rs.wasNull()){count = null;}
//	            Accessory accessory = Accessory.builder()
//	                    .accessoryCategory(rs.getString("accessoryCategory"))
//	                    .uom(rs.getString("pl_acc_uom"))
//	                    .id(rs.getString("pl_acc_id"))
//	                    .uomValue(rs.getString("pl_acc_uomvalue"))
//	                    .tenantId(tenantId)
//	                    .active(rs.getBoolean("pl_acc_active"))
//	                    .count(count)
//	                    .build();
//	            petLicense.getPetLicenseDetail().addAccessoriesItem(accessory);
//	        }


	        Document ownerDocument = Document.builder().id(rs.getString("ownerdocid"))
	                .documentType(rs.getString("ownerdocType"))
	                .fileStoreId(rs.getString("ownerfileStoreId"))
	                .documentUid(rs.getString("ownerdocuid"))
	                .active(rs.getBoolean("ownerdocactive"))
	                .build();





	        // Add owner document to the specific petLicense for which it was used
	        String docuserid = rs.getString("docuserid");
	        String doctradeLicenseDetailId = rs.getString("doctradelicensedetailid");
	        if(petLicenseDetailId.equalsIgnoreCase(doctradeLicenseDetailId) && docuserid!=null
	                && rs.getBoolean("ownerdocactive") && rs.getBoolean("useractive")) {
//	            petLicense.getPetLicenseDetail().getOwner().forEach(ownerInfo -> {
	                if (docuserid.equalsIgnoreCase(petLicense.getPetLicenseDetail().getOwner().getUuid()))
	                	petLicense.getPetLicenseDetail().getOwner().addDocumentsItem(ownerDocument);
//	            });
	        }

	        if(rs.getString("pl_ap_doc_id")!=null && rs.getBoolean("pl_ap_doc_active")) {
	            Document applicationDocument = Document.builder()
	                    .documentType(rs.getString("pl_ap_doc_documenttype"))
	                    .fileStoreId(rs.getString("pl_ap_doc_filestoreid"))
	                    .id(rs.getString("pl_ap_doc_id"))
	                    .tenantId(tenantId)
	                    .active(rs.getBoolean("pl_ap_doc_active"))
	                    .build();
	            petLicense.getPetLicenseDetail().addApplicationDocumentsItem(applicationDocument);
	        }

	        if(rs.getString("pl_ver_doc_id")!=null && rs.getBoolean("pl_ver_doc_active")) {
	            Document verificationDocument = Document.builder()
	                    .documentType(rs.getString("pl_ver_doc_documenttype"))
	                    .fileStoreId(rs.getString("pl_ver_doc_filestoreid"))
	                    .id(rs.getString("pl_ver_doc_id"))
	                    .tenantId(tenantId)
	                    .active(rs.getBoolean("pl_ver_doc_active"))
	                    .build();
	            petLicense.getPetLicenseDetail().addVerificationDocumentsItem(verificationDocument);
	        }
	    }




	}
