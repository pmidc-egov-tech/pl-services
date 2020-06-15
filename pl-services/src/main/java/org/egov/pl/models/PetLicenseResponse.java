package org.egov.pl.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contract class to send response.
 */
@ApiModel(description = "Contract class to send response.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetLicenseResponse {
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("Licenses")
	@Valid
	private List<PetLicense> licenses = null;

	public PetLicenseResponse addLicensesItem(PetLicense licensesItem) {
		if (this.licenses == null) {
			this.licenses = new ArrayList<>();
		}
		this.licenses.add(licensesItem);
		return this;
	}
}
