package org.egov.pl.models;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 1. This is the master data for Animal Sub Category. 2. It&#39;s defined under
 * mdms service. 3. To get data from mdms &#39;moduleName&#x3D;PetLicense,
 * masterName&#x3D;AnimalSubCategory&#39;.
 */
@ApiModel(description = "1. This is the master data for Animal Sub Category. 2. It's defined under mdms service. 3. To get data from mdms 'moduleName=PetLicense, masterName=AnimalSubCategory'.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalSubCategory {
	@JsonProperty("name")
	private String name = null;

	@JsonProperty("code")
	private String code = null;

	@JsonProperty("isActive")
	private Boolean isActive = null;

}
