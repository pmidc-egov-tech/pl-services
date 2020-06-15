package org.egov.pl.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Boundary
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary {
	@Size(max = 64)
	@JsonProperty("code")
	private String code = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("label")
	private String label = null;

	@JsonProperty("latitude")
	private String latitude = null;

	@JsonProperty("longitude")
	private String longitude = null;

	@JsonProperty("children")
	@Valid
	private List<Boundary> children = null;

	@JsonProperty("materializedPath")
	private String materializedPath = null;

	public Boundary addChildrenItem(Boundary childrenItem) {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}
		this.children.add(childrenItem);
		return this;
	}
}
