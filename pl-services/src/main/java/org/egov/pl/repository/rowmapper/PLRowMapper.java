package org.egov.pl.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.pl.models.AuditDetails;
import org.egov.pl.models.PetLicense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

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
                        .id(id)
                        .build();

                petLicenseMap.put(id,currentPetLicense);
            }

        }

        return new ArrayList<>(petLicenseMap.values());

    }





}
