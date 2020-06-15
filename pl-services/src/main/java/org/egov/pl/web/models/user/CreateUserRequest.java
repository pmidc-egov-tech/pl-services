package org.egov.pl.web.models.user;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pl.models.OwnerInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CreateUserRequest {

	@JsonProperty("requestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("user")
	private OwnerInfo user;

}
