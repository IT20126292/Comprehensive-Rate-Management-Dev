package com.sampath.portal.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currency_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "currency_name", nullable = false)
    private String currencyName;

    @Column(name = "currency_rate", nullable = false)
    private Double currencyRate;

    @Column(name = "currency_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate currencyDate;
}
