package com.sampath.portal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sampath.portal.entity.CurrencyMaster;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.service.RateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class RateController {

    private final RateService rateService;

    @PostMapping("/submit")
    public RateRequest submitRateRequest(@RequestBody RateRequest rateRequest) {
        log.info("Submitting new rate request by user: {}", rateRequest.getRequestedBy());
        for (RateItem item : rateRequest.getRateItems()) {
            item.setRateRequest(rateRequest);
        }
        RateRequest savedRequest = rateService.submitRateRequest(rateRequest);
        log.info("Rate request submitted successfully with id: {}", savedRequest.getId());
        return savedRequest;
    }

    @GetMapping("/all")
    public List<RateRequest> getAllRequests() {
        log.debug("Fetching all rate requests");
        List<RateRequest> requests = rateService.getAllRequests();
        log.debug("Fetched {} rate requests", requests.size());
        return requests;
    }

    @GetMapping("/{id}")
    public RateRequest getRequest(@PathVariable Long id) {
        log.debug("Fetching rate request with id: {}", id);
        RateRequest request = rateService.getRequestById(id)
                .orElseThrow(() -> new RuntimeException("Rate request not found with id: " + id));
        log.debug("Rate request found: {}", request.getId());
        return request;
    }

    @PostMapping("/approve/{id}")
    public RateRequest approve(@PathVariable Long id, @RequestParam String reviewer, @RequestBody RateRequest rateRequest) {
        RateRequest request = rateService.approveRequest(id, reviewer, rateRequest);
        log.info("Action :" + rateRequest.getStatus());
        log.info("Rate request " + rateRequest.getStatus() + " with id: {}", request.getId());
        return request;
    }
    

    @PutMapping("/reject/{id}")
    public RateRequest reject(@PathVariable Long id, @RequestParam String reviewer) {
        log.info("Rejecting rate request id: {} by reviewer: {}", id, reviewer);
        RateRequest request = rateService.rejectRequest(id, reviewer);
        log.info("Rate request rejected with id: {}", request.getId());
        return request;
    }

    @GetMapping("/currencies/{currencyDate}")
    public List<CurrencyMaster> getAllCurrenciesRateByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currencyDate) {
        log.info("Fetching all currency rates for date: {}", currencyDate);
        List<CurrencyMaster> currencies = rateService.getAllCurrenciesByDate(currencyDate);
        log.info("Fetched {} currencies", currencies.size());
        return currencies;
    }


}
