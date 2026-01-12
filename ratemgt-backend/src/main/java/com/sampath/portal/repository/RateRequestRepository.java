package com.sampath.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sampath.portal.entity.RateRequest;

public interface RateRequestRepository extends JpaRepository<RateRequest, Long> {

    //DB Schema : CRMSNAPN
    @Query(value = "SELECT DISTINCT * FROM CRMSNAPN.rate_requests ORDER BY requested_at DESC FETCH FIRST 5 ROWS ONLY", nativeQuery = true)
    List<RateRequest> findLatest5Records();

    @Query(value = "SELECT DISTINCT * FROM CRMSNAPN.rate_requests WHERE TRUNC(REQUESTED_AT) = :date ORDER BY requested_at DESC FETCH FIRST 5 ROWS ONLY", nativeQuery = true)
    List<RateRequest> findByDate(@Param("date") java.sql.Date date);
}