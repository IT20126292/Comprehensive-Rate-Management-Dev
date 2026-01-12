package com.sampath.portal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RATE_ITEMS",schema = "CRMSNAPN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FXD_CURRENCY_CODE")
    private String fixCurrencyCode;
    
    @Column(name = "VAR_CURRENCY_CODE")
    private String varCurrencyCode;

    @Column(name = "RATECODE")
    private String rateCode;

    @Column(name = "VAR_CURRENCY_UNIT")
    private Double varCurnyUnit;

    @ManyToOne
    @JoinColumn(name = "RATE_REQUEST_ID")
    @JsonIgnore
    private RateRequest rateRequest;
}
