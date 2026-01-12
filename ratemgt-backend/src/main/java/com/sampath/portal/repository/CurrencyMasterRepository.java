package com.sampath.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sampath.portal.entity.CurrencyMaster;

public interface CurrencyMasterRepository extends JpaRepository<CurrencyMaster, Long> {
    CurrencyMaster findByCurrencyCode(String currencyCode);

    // Native query for Oracle, using TRUNC to ignore time
    @Query(value = "SELECT * FROM CUSSEG.currency_master WHERE TRUNC(currency_date) = :currencyDate", nativeQuery = true)
    List<CurrencyMaster> findByCurrencyDate(@Param("currencyDate") java.sql.Date currencyDate);

}