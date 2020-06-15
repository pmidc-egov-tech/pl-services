package org.egov.pl.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User role carries the tenant related role information for the user. A user
 * can have multiple roles per tenant based on the need of the tenant. A user
 * may also have multiple roles for multiple tenants.
 */
@ApiModel(description = "User role carries the tenant related role information for the user. A user can have multiple roles per tenant based on the need of the tenant. A user may also have multiple roles for multiple tenants.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRole {
	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("roles")
	@Valid
	private List<Role> roles = new ArrayList<Role>();

	public TenantRole tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

}
