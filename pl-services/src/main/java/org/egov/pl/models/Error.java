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
 * Error object will be returned as a part of reponse body in conjunction with ResponseInfo as part of ErrorResponse whenever the request processing status in the ResponseInfo is FAILED. HTTP return in this scenario will usually be HTTP 400.
 */
@ApiModel(description = "Error object will be returned as a part of reponse body in conjunction with ResponseInfo as part of ErrorResponse whenever the request processing status in the ResponseInfo is FAILED. HTTP return in this scenario will usually be HTTP 400.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Error   {
	@JsonProperty("code")
    private String code = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("params")
    @Valid
    private List<String> params = null;


    public Error addParamsItem(String paramsItem) {
        if (this.params == null) {
        this.params = new ArrayList<>();
        }
    this.params.add(paramsItem);
    return this;
    }

}

