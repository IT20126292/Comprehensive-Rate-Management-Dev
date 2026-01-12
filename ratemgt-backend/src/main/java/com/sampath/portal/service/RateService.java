package com.sampath.portal.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sampath.portal.entity.CurrencyMaster;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.repository.CurrencyMasterRepository;
import com.sampath.portal.repository.RateRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateService {

    private final RateRequestRepository rateRequestRepository;
    private final CurrencyMasterRepository currencyMasterRepository;

    public RateRequest submitRateRequest(RateRequest request) {
        return rateRequestRepository.save(request);
    }

    public List<RateRequest> getAllRequests() {
        return rateRequestRepository.findLatest5Records();
    }

    public Optional<RateRequest> getRequestById(Long id) {
        return rateRequestRepository.findById(id);
    }

    @Transactional
    public RateRequest approveRequest(Long id, String reviewer, RateRequest rateRequest) {
    log.info("Request comment : {}", rateRequest.getComent());

    RateRequest request = rateRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));

    try {
        request.setStatus(rateRequest.getStatus());
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewer);
        request.setComent(rateRequest.getComent());

        RateRequest savedRequest = rateRequestRepository.save(request);
        log.info("✅ Successfully " +rateRequest.getStatus() + "  for ID: {}", savedRequest.getId());

        // If any error occurs later, Spring rolls back automatically
        return savedRequest;
    } catch (Exception ex) {
        log.error("❌ Error while processing request: {}", ex.getMessage());
        throw new RuntimeException("Approval process failed — rolling back transaction");
    }
}

    public RateRequest rejectRequest(Long id, String reviewer) {
        log.info("submit in reject");
        RateRequest request = rateRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus("REJECTED");
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(reviewer);
        request.setComent(reviewer);

        return rateRequestRepository.save(request);
    }

    public List<CurrencyMaster> getAllCurrenciesByDate(LocalDate currencyDate) {
        log.info("Date is before"+ currencyDate);
        Date oracleDate = Date.valueOf(currencyDate);
        log.info("Date is after "+ oracleDate);
        return currencyMasterRepository.findByCurrencyDate(oracleDate);
    }
   
}

