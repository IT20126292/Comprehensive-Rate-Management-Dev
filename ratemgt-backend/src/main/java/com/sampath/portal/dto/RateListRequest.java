package com.sampath.portal.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateListRequest {

    // Request fields
    private String reqId;
    private String rtlDate;
    private String isCurrency;
    
}