package com.sampath.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sampath.portal.entity.CrmsExRate;

import java.util.List;
import java.util.Optional;

public interface CrmsExRateRepository extends JpaRepository<CrmsExRate, Long> {

    List<CrmsExRate> findByCurCode(String curCode);

    Optional<CrmsExRate> findFirstByCurCodeAndRateType(String curCode, String rateType);
}

