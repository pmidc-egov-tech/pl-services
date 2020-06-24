package org.egov.pl.models;

import org.egov.pl.models.SMSRequest;
import org.egov.pl.models.SMSRequest.SMSRequestBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SMSRequest {
    private String mobileNumber;
    private String message;

}
