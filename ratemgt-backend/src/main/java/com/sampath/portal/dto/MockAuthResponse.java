package com.sampath.portal.dto;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MockAuthResponse {
    @JsonProperty("Response")
    private boolean response;

    @JsonProperty("UserID")
    private String userID;

    @JsonProperty("UserClass")
    private String userClass;   // "10" = Admin, "11" = User //UpmAd Admin = "10", User = "50"

    @JsonProperty("UPMData")
    private Map<String, Object> UPMData;

    @JsonProperty("Code")
    private int code;

    @JsonProperty("SubCode")
    private int subCode;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("RequestID")
    private String requestID;

    
}