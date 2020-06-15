package org.egov.pl.models;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representation of a address. Indiavidual APIs may choose to extend from this
 * using allOf if more details needed to be added in their case.
 */
@ApiModel(description = "Representation of a address. Indiavidual APIs may choose to extend from this using allOf if more details needed to be added in their case. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("tenantId")
	private String tenantId = null;

	@Size(max = 64)
	@JsonProperty("doorNo")
	private String doorNo = null;

	@JsonProperty("latitude")
	private Double latitude = null;

	@JsonProperty("longitude")
	private Double longitude = null;

	@Size(max = 64)
	@JsonProperty("addressId")
	private String addressId = null;

	@Size(max = 64)
	@JsonProperty("addressNumber")
	private String addressNumber = null;

	@Size(max = 64)
	@JsonProperty("type")
	private String type = null;

	@Size(max = 64)
	@JsonProperty("addressLine1")
	private String addressLine1 = null;

	@Size(max = 254)
	@JsonProperty("addressLine2")
	private String addressLine2 = null;

	@Size(max = 64)
	@JsonProperty("landmark")
	private String landmark = null;

	@Size(max = 64)
	@JsonProperty("city")
	private String city = null;

	@Size(max = 64)
	@JsonProperty("pincode")
	private String pincode = null;

	@Size(max = 64)
	@JsonProperty("detail")
	private String detail = null;

	@Size(max = 64)
	@JsonProperty("buildingName")
	private String buildingName = null;

	@Size(max = 64)
	@JsonProperty("street")
	private String street = null;

	@Valid
	@JsonProperty("locality")
	private Boundary locality = null;

}
