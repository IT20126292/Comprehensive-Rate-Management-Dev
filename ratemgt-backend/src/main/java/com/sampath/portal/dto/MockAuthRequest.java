package com.sampath.portal.dto;


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
public class MockAuthRequest {
    private String requestTime;
    private String adUsername;
    private String adUserPassword;
    private String appCode;
}
