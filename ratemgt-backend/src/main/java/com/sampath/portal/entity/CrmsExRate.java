package com.sampath.portal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CRMS_EX_RATES_SYN")//CRMSAPN
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmsExRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RE_ROW_ID")
    private Long id;

    @Column(name = "RE_CUR_CODE")
    private String curCode; // currency code - AUD, USD

    @Column(name = "RE_RATE_TYPE")
    private String rateType; // EXRT or CURRT

    // Columns for EXRT mapping
    @Column(name = "RE_TTBUY")
    private Double reTtBuy;  // if rate_code TTBUY

    @Column(name = "RE_ODBUY")
    private Double reOdBuy; // if rate_code ODBUY

    @Column(name = "RE_TTSEL")
    private Double reTtSel; // if rate_code TTSEL

    // Columns for CURRT mapping
    @Column(name = "RE_BYOVC")
    private Double reByOvc; // if rate_code CBCL or CBCS setReByOvc

    @Column(name = "RE_RATE_WEF")
    private LocalDateTime recWefTime;

    @Column(name = "RE_ORDER")
    private Long order;
   
    @Column(name = "RE_CROSSCURBID")
    private Long crossCurBid;

    @Column(name = "RE_REC_CREATE_USER")
    private String createdUser; 
    
     // last create time
    @Column(name = "RE_REC_CREATE_TIME")
    private LocalDateTime recCreateTime;

    @Column(name = "RE_REC_VERIFIED_TIME")
    private LocalDateTime verifiedTime;

    @Column(name = "RE_REC_VERIFIED_USER")
    private String verifiedUser; 

    @Column(name = "RE_ACTIVE_STATUS")
    private String activeStatus; 

    @Column(name = "RE_SPECIAL_REMARKS")
    private String specRemarks; 

    @Column(name = "RE_BYOVC_OUT")
    private Long byovcOut;

   


}

