package com.sampath.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sampath.portal.entity.TbaadmRtl;
import com.sampath.portal.entity.TbaadmRtlId;

public interface TbaadmRtlRepository extends JpaRepository<TbaadmRtl, TbaadmRtlId> {

    //DB Schema : CRMSNAPN
    @Query(value = "SELECT MAX(RTLIST_NUM) FROM CRMSNAPN.RATELIST_TABLE WHERE TRUNC(RTLIST_DATE) = :date", nativeQuery = true)
    Long findMaxRtlistNumForDate(@Param("date") java.sql.Date date);

    @Query(value = "SELECT DISTINCT * FROM CRMSNAPN.RATELIST_TABLE WHERE FXD_CRNCY_CODE IN (SELECT FXD_CRNCY_CODE FROM CUR_LIST) AND DEL_FLG = 'N' AND ENTITY_CRE_FLG = 'Y' AND TRUNC(RTLIST_DATE) = :date AND RTLIST_NUM = :rtlistNum AND RATECODE IN (:ratecodes) ORDER BY CASE FXD_CRNCY_CODE WHEN 'USD' THEN 1 WHEN 'GBP' THEN 2 WHEN 'EUR' THEN 3 WHEN 'JPY' THEN 4 WHEN 'AUD' THEN 5 WHEN 'NZD' THEN 6 WHEN 'CHF' THEN 7 WHEN 'SEK' THEN 8 WHEN 'DKK' THEN 9 WHEN 'CAD' THEN 10 WHEN 'SGD' THEN 11 WHEN 'HKD' THEN 12 WHEN 'NOK' THEN 13 WHEN 'CNY' THEN 14 WHEN 'ZAR' THEN 15 WHEN 'AED' THEN 16 WHEN 'INR' THEN 17 ELSE 18 END", nativeQuery = true)
    List<TbaadmRtl> findFilteredForDateAndRtlist(@Param("date") java.sql.Date date, @Param("rtlistNum") Long rtlistNum, @Param("ratecodes") List<String> ratecodes);
}

