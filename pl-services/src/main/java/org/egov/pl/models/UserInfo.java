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
 * This is acting ID token of the authenticated user on the server. Any value
 * provided by the clients will be ignored and actual user based on authtoken
 * will be used on the server.
 */
@ApiModel(description = "This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("uuid")
	private String uuid = null;

	@JsonProperty("userName")
	private String userName = null;

	@JsonProperty("password")
	private String password = null;

	@JsonProperty("idToken")
	private String idToken = null;

	@JsonProperty("mobile")
	private String mobile = null;

	@JsonProperty("email")
	private String email = null;

	@JsonProperty("primaryrole")
	@Valid
	private List<Role> primaryrole = new ArrayList<Role>();

	@JsonProperty("additionalroles")
	@Valid
	private List<TenantRole> additionalroles = null;

}
