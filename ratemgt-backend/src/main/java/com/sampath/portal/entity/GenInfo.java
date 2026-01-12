package com.sampath.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GEN_INFO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenInfo {

    @Id
    @Column(name = "GEN_CURR_TYPE", nullable = false)
    private String genCurrType;

    @Column(name = "GEN_EBUY_RATE")
    private Double genEbuyRate;

    @Column(name = "GEN_ESELL_RATE")
    private Double genEsellRate;

    @Column(name = "GEN_CNBUY_RATE")
    private Double genCnbuyRate;

    @Column(name = "GEN_CNSELL_RATE")
    private Double genCnsellRate;

    @Column(name = "GEN_NRFC_SDR")
    private Double genNrfcsdr;

    @Column(name = "GEN_NRFC_FDR")
    private Double genNrfcfdr;

    @Column(name = "GEN_RFC_SDR")
    private Double genRfcsdr;

    @Column(name = "GEN_RFC_FDR")
    private Double genRfcfdr;

    @Column(name = "GEN_HAS_NRFC")
    private String genHasNrfc;
}