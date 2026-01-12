package com.sampath.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sampath.portal.entity.RateItem;

public interface RateItemRepository extends JpaRepository<RateItem, Long> {
}
