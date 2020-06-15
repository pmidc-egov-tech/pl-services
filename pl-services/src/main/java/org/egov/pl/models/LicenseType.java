package org.egov.pl.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * 1. This is the master data for License Type. 2. It&#39;s defined under mdms service. 3. To get data from mdms &#39;moduleName&#x3D;PetLicense, masterName&#x3D;LicenseType&#39;.
 */
@ApiModel(description = "1. This is the master data for License Type. 2. It's defined under mdms service. 3. To get data from mdms 'moduleName=PetLicense, masterName=LicenseType'.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-05-18T04:08:46.274Z")

public class LicenseType   {
  @JsonProperty("isActive")
  private Boolean isActive = null;

  @JsonProperty("code")
  private String code = null;

  public LicenseType isActive(Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * Active/Deactive flag.
   * @return isActive
  **/
  @ApiModelProperty(value = "Active/Deactive flag.")


  public Boolean isIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public LicenseType code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Unique code of License Type.
   * @return code
  **/
  @ApiModelProperty(required = true, value = "Unique code of License Type.")
  @NotNull

@Size(min=2,max=64) 
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LicenseType licenseType = (LicenseType) o;
    return Objects.equals(this.isActive, licenseType.isActive) &&
        Objects.equals(this.code, licenseType.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isActive, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LicenseType {\n");
    
    sb.append("    isActive: ").append(toIndentedString(isActive)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

