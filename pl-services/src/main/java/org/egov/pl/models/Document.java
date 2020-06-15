package org.egov.pl.models;

import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class Document {
	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@JsonProperty("active")
	private Boolean active;

	@Size(max = 64)
	@JsonProperty("tenantId")
	private String tenantId = null;

	@Size(max = 64)
	@JsonProperty("documentType")
	private String documentType = null;

	@Size(max = 64)
	@JsonProperty("fileStoreId")
	private String fileStoreId = null;

	@Size(max = 64)
	@JsonProperty("documentUid")
	private String documentUid;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;
}
