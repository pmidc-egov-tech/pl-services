package org.egov.pl.models;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.tl.web.models.Document;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Pet License
 */
@ApiModel(description = "A Object holds the basic data for a Pet License")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetLicense {

	@Size(max = 64)
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("uuid")
	private String uuid = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("name")
	private String name = null;

	/**
	 * Unique Identifier of the Pet License (UUID)
	 */
	public enum LicenseTypeEnum {
		TEMPORARY("TEMPORARY"),

		PERMANENT("PERMANENT");

		private String value;

		LicenseTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static LicenseTypeEnum fromValue(String text) {
			for (LicenseTypeEnum b : LicenseTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("businessService")
	private String businessService = "PL";

	@JsonProperty("licenseType")
	private LicenseTypeEnum licenseType = null;

	@Size(max = 64)
	@JsonProperty("licenseNumber")
	private String licenseNumber = null;

	@Size(max = 64)
	@JsonProperty("applicationNumber")
	private String applicationNumber;

	@Size(max = 64)
	@JsonProperty("oldLicenseNumber")
	private String oldLicenseNumber = null;

	@JsonProperty("applicationDate")
	private Long applicationDate = null;

	@JsonProperty("issuedDate")
	private Long issuedDate = null;

	@JsonProperty("validFrom")
	private Long validFrom = null;

	@JsonProperty("validTo")
	private Long validTo = null;

	@NotNull
	@Size(max = 64)
	@JsonProperty("action")
	private String action = null;
	
    @JsonProperty("assignee")
    private List<String> assignee = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@Size(max = 64)
	@JsonProperty("status")
	private String status = null;
	
    @Size(max=128)
    private String comment;
    
    @JsonProperty("wfDocuments")
    private List<Document> wfDocuments;

	@Valid
	@NotNull
	@JsonProperty("petLicenseDetail")
	private PetLicenseDetail petLicenseDetail = null;
}
