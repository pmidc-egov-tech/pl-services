package org.egov.pl.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
public class PetLicenseDetail {
	@JsonProperty("owner")
	private Object owner = null;

	/**
	 * License can be created from different channels
	 */
	public enum ChannelEnum {
		COUNTER("COUNTER"),

		CITIZEN("CITIZEN"),

		DATAENTRY("DATAENTRY");

		private String value;

		ChannelEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ChannelEnum fromValue(String text) {
			for (ChannelEnum b : ChannelEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("channel")
	private ChannelEnum channel = null;

	@JsonProperty("address")
	private Address address = null;

	@JsonProperty("animalCategory")
	private AnimalCategory animalCategory = null;

	@JsonProperty("animalColor")
	private AnimalColor animalColor = null;

	@JsonProperty("animalAge")
	private Integer animalAge = null;

	@JsonProperty("applicationDocuments")
	@Valid
	private List<Document> applicationDocuments = null;

	@JsonProperty("verificationDocuments")
	@Valid
	private List<Document> verificationDocuments = null;

	@JsonProperty("additionalDetail")
	private String additionalDetail = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	public PetLicenseDetail owner(Object owner) {
		this.owner = owner;
		return this;
	}

	/**
	 * Pet owner, he will be citizen user in system.
	 * 
	 * @return owner
	 **/
	@ApiModelProperty(required = true, value = "Pet owner, he will be citizen user in system.")
	@NotNull

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public PetLicenseDetail channel(ChannelEnum channel) {
		this.channel = channel;
		return this;
	}

	/**
	 * License can be created from different channels
	 * 
	 * @return channel
	 **/
	@ApiModelProperty(required = true, value = "License can be created from different channels")
	@NotNull

	@Size(min = 2, max = 64)
	public ChannelEnum getChannel() {
		return channel;
	}

	public void setChannel(ChannelEnum channel) {
		this.channel = channel;
	}

	public PetLicenseDetail address(Address address) {
		this.address = address;
		return this;
	}

	/**
	 * Get address
	 * 
	 * @return address
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public PetLicenseDetail animalCategory(AnimalCategory animalCategory) {
		this.animalCategory = animalCategory;
		return this;
	}

	/**
	 * Get animalCategory
	 * 
	 * @return animalCategory
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public AnimalCategory getAnimalCategory() {
		return animalCategory;
	}

	public void setAnimalCategory(AnimalCategory animalCategory) {
		this.animalCategory = animalCategory;
	}

	public PetLicenseDetail animalColor(AnimalColor animalColor) {
		this.animalColor = animalColor;
		return this;
	}

	/**
	 * Get animalColor
	 * 
	 * @return animalColor
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public AnimalColor getAnimalColor() {
		return animalColor;
	}

	public void setAnimalColor(AnimalColor animalColor) {
		this.animalColor = animalColor;
	}

	public PetLicenseDetail animalAge(Integer animalAge) {
		this.animalAge = animalAge;
		return this;
	}

	/**
	 * Age of Animal at the time of Registration.
	 * 
	 * @return animalAge
	 **/
	@ApiModelProperty(value = "Age of Animal at the time of Registration.")

	public Integer getAnimalAge() {
		return animalAge;
	}

	public void setAnimalAge(Integer animalAge) {
		this.animalAge = animalAge;
	}

	public PetLicenseDetail applicationDocuments(List<Document> applicationDocuments) {
		this.applicationDocuments = applicationDocuments;
		return this;
	}

	public PetLicenseDetail addApplicationDocumentsItem(Document applicationDocumentsItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<Document>();
		}
		this.applicationDocuments.add(applicationDocumentsItem);
		return this;
	}

	/**
	 * 1. List of all the required documents. 2. Application can be submitted
	 * without required document 3. Once all the document submitted then only
	 * application submition process will be completed. 4.Provision of the animal
	 * photographs(front, back right side, left side) 5.provision for the passport
	 * size photographs for the owner 6.It should be certified by the civil vet
	 * doctor that animal does not have any defect as on date 7.original copy of the
	 * certificate issued by the civil vet doctor by uploaded for free from
	 * infectious diseases 8. Mandatry application documents for certificate which
	 * is defined under ApplicationDocument master which is defined under MDMS.
	 * 
	 * @return applicationDocuments
	 **/
	@ApiModelProperty(value = "1. List of all the required documents. 2. Application can be submitted without required document 3. Once all the document submitted then only application submition process will be completed.  4.Provision of the animal photographs(front, back right side, left side) 5.provision for the passport size photographs for the owner 6.It should be certified by the civil vet doctor that animal does not have any defect as on date  7.original copy of the certificate issued by the civil vet doctor by uploaded for free from infectious diseases 8. Mandatry application documents for certificate which is defined under ApplicationDocument master which is defined under MDMS.")

	@Valid

	public List<Document> getApplicationDocuments() {
		return applicationDocuments;
	}

	public void setApplicationDocuments(List<Document> applicationDocuments) {
		this.applicationDocuments = applicationDocuments;
	}

	public PetLicenseDetail verificationDocuments(List<Document> verificationDocuments) {
		this.verificationDocuments = verificationDocuments;
		return this;
	}

	public PetLicenseDetail addVerificationDocumentsItem(Document verificationDocumentsItem) {
		if (this.verificationDocuments == null) {
			this.verificationDocuments = new ArrayList<Document>();
		}
		this.verificationDocuments.add(verificationDocumentsItem);
		return this;
	}

	/**
	 * 1. Verification Documents will uploaded at the time of approval. 2. Mandatry
	 * verification documents for certificate which is defined under
	 * VerificationDocument master which is defined under MDMS.
	 * 
	 * @return verificationDocuments
	 **/
	@ApiModelProperty(value = "1. Verification Documents will uploaded at the time of approval. 2. Mandatry verification documents for certificate which is defined under VerificationDocument master which is defined under MDMS.")

	@Valid

	public List<Document> getVerificationDocuments() {
		return verificationDocuments;
	}

	public void setVerificationDocuments(List<Document> verificationDocuments) {
		this.verificationDocuments = verificationDocuments;
	}

	public PetLicenseDetail additionalDetail(String additionalDetail) {
		this.additionalDetail = additionalDetail;
		return this;
	}

	/**
	 * Json object to store additional details about license
	 * 
	 * @return additionalDetail
	 **/
	@ApiModelProperty(value = "Json object to store additional details about license")

	public String getAdditionalDetail() {
		return additionalDetail;
	}

	public void setAdditionalDetail(String additionalDetail) {
		this.additionalDetail = additionalDetail;
	}

	public PetLicenseDetail auditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
		return this;
	}

	/**
	 * Get auditDetails
	 * 
	 * @return auditDetails
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PetLicenseDetail petLicenseDetail = (PetLicenseDetail) o;
		return Objects.equals(this.owner, petLicenseDetail.owner)
				&& Objects.equals(this.channel, petLicenseDetail.channel)
				&& Objects.equals(this.address, petLicenseDetail.address)
				&& Objects.equals(this.animalCategory, petLicenseDetail.animalCategory)
				&& Objects.equals(this.animalColor, petLicenseDetail.animalColor)
				&& Objects.equals(this.animalAge, petLicenseDetail.animalAge)
				&& Objects.equals(this.applicationDocuments, petLicenseDetail.applicationDocuments)
				&& Objects.equals(this.verificationDocuments, petLicenseDetail.verificationDocuments)
				&& Objects.equals(this.additionalDetail, petLicenseDetail.additionalDetail)
				&& Objects.equals(this.auditDetails, petLicenseDetail.auditDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(owner, channel, address, animalCategory, animalColor, animalAge, applicationDocuments,
				verificationDocuments, additionalDetail, auditDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PetLicenseDetail {\n");

		sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
		sb.append("    channel: ").append(toIndentedString(channel)).append("\n");
		sb.append("    address: ").append(toIndentedString(address)).append("\n");
		sb.append("    animalCategory: ").append(toIndentedString(animalCategory)).append("\n");
		sb.append("    animalColor: ").append(toIndentedString(animalColor)).append("\n");
		sb.append("    animalAge: ").append(toIndentedString(animalAge)).append("\n");
		sb.append("    applicationDocuments: ").append(toIndentedString(applicationDocuments)).append("\n");
		sb.append("    verificationDocuments: ").append(toIndentedString(verificationDocuments)).append("\n");
		sb.append("    additionalDetail: ").append(toIndentedString(additionalDetail)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
