package com.sampath.portal.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class Rate {

    @JsonProperty("fxdCrncyCode")
    private String fxdCrncyCode;             // "USD"

    @JsonProperty("varCrncyCode")
    private String varCrncyCode;             // "LKR"

    @JsonProperty("rateCode")
    private String rateCode;                 // "CSOS" / "NOR"

    @JsonProperty("entityCreFlg")
    private String entityCreFlg;             // "Y"

    @JsonProperty("delLFlg")
    private String delFlg;                   // "N"

    @JsonProperty("rtListDate")
    private String rtListDate;               // "21-07-2024"

    @JsonProperty("rtListNum")
    private String rtListNum;                // "03"

    @JsonProperty("fxdCrncyUnits")
    private String fxdCrncyUnits;              // 1

    @JsonProperty("varCrncyUnits")
    private String varCrncyUnits;            // 296.4

    @JsonProperty("custVarCrncyUnits")
    private String custVarCrncyUnits;        // 77.65

    @JsonProperty("lchgUserId")
    private String lchgUserId;               // "42579"

    @JsonProperty("lchgTime")
    private String lchgTime;                 // "20-08-2025 13:53:20"

    @JsonProperty("rcreUserId")
    private String rcreUserId;               // "KASTURI2"

    @JsonProperty("rcreTime")
    private String rcreTime;                 // "03-05-1999 13:03:19"

    @JsonProperty("varCrncyTolPcntPlus")
    private String varCrncyTolPcntPlus;      // "2"

    @JsonProperty("varCrncyTolPcntMinus")
    private String varCrncyTolPcntMinus;     // "2"

    @JsonProperty("srlNum")
    private String srlNum;                   // "001"

    @JsonProperty("bankId")
    private String bankId;
}
