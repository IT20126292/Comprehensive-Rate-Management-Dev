package com.sampath.portal.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RATELIST_TABLE", schema = "CRMSNAPN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbaadmRtl {

    @EmbeddedId
    private TbaadmRtlId id;

    @Column(name = "DEL_FLG")
    private char delFlag;

    @Column(name = "ENTITY_CRE_FLG")
    private char entityCreFlag;

    @Column(name = "FXD_CRNCY_UNITS")
    private BigDecimal fxdCrncyUnit;

    @Column(name = "VAR_CRNCY_UNITS", columnDefinition = "NUMBER")
    private BigDecimal varCrncyUnit;

    @Column(name = "CUST_VAR_CRNCY_UNITS")
    private BigDecimal custVarCrncyUnit;

    @Column(name = "LCHG_USER_ID")
    private String userId;

    @Column(name = "LCHG_TIME")
    private LocalDateTime lchgTime;

    @Column(name = "RCRE_USER_ID")
    private String creUserId;

    @Column(name = "RCRE_TIME")
    private LocalDateTime rcreTime;

    @Column(name = "VAR_CRNCY_TOL_PCNT_PLUS")
    private BigDecimal varCrncyTolpcntPlus;

    @Column(name = "VAR_CRNCY_TOL_PCNT_MINUS")
    private BigDecimal varCrncyTolpcntMinus;

    @Column(name = "TS_CNT")
    private Long tsCnt;

    @Column(name = "VAR_CRNCY_TOL_PCNT_PLUS_TR")
    private BigDecimal varCrncyTolpcntPlusTr;

    @Column(name = "VAR_CRNCY_TOL_PCNT_MINUS_TR")
    private BigDecimal varCrncyTolpcntMinusTr;

    @Column(name = "SRL_NUM")
    private String srlNum;

    @Column(name = "BANK_ID")
    private String bankId;

    @Column(name = "LOW_SLAB_AMT")
    private BigDecimal lowSlbAmt;

    @Column(name = "HIGH_SLAB_AMT")
    private BigDecimal highSlbAmt;

    @Column(name = "SLAB_CRNCY_CODE")
    private String slbCrnvyCode;
}
