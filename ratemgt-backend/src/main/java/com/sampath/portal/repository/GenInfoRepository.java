package com.sampath.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sampath.portal.entity.GenInfo;

@Repository
public interface GenInfoRepository extends JpaRepository<GenInfo, String> {
    
     Optional<GenInfo> findByGenCurrType(String genCurrType);
}