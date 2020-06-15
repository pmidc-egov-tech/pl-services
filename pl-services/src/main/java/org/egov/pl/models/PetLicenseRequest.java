package org.egov.pl.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contract class to receive request.
 */
@ApiModel(description = "Contract class to receive request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetLicenseRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	@JsonProperty("Licenses")
	@Valid
	private List<PetLicense> licenses = null;

	public PetLicenseRequest addLicensesItem(PetLicense licensesItem) {
		if (this.licenses == null) {
			this.licenses = new ArrayList<>();
		}
		this.licenses.add(licensesItem);
		return this;
	}

}
