package com.sampath.portal.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RATE_REQUESTS",schema = "CRMSNAPN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestedBy;
    private LocalDateTime requestedAt;
    private String status; // IN_PROGRESS, APPROVED, REJECTED
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String coment;

    @OneToMany(mappedBy = "rateRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RateItem> rateItems;

    
}
