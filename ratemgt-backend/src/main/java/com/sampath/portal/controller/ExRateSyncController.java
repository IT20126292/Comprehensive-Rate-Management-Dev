package com.sampath.portal.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.entity.TbaadmRtl;
import com.sampath.portal.service.ExRateSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/exrates")
@RequiredArgsConstructor
@Slf4j
public class ExRateSyncController {

    private final ExRateSyncService exRateSyncService;

    @PostMapping("/process/today")
    public ResponseEntity<String> processTodayRates(@RequestBody List<RateItem> filteredData) {
        log.info("Manual trigger: processing today's rates");
        log.info("currency code :" + filteredData.get(0).getFixCurrencyCode());
        exRateSyncService.processTodayRates(filteredData);
        return ResponseEntity.ok("Updated");
    }

    // Optional health endpoint
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/currencies/{currencyDate}")
    public List<TbaadmRtl> getAllCurrenciesRateByDate(@PathVariable String currencyDate) {
        log.info("Fetching all currency rates for date for get:" +  currencyDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        log.info("IN IN");

        LocalDate localDate = LocalDate.parse(currencyDate, formatter);
         log.info("IN IN IN");

        log.info("Fetching all currency rates for local date:" + localDate);
        List<TbaadmRtl> currencies = exRateSyncService.getFilterdRTLListbyDateAndMaxListNum(localDate);
        log.info("Fetched {} currencies", currencies.size());
        return currencies;
    }

    @PostMapping("/fetch/{currencyDate}")
    public String fetchAndSaveRates(@PathVariable String currencyDate) {
        log.info("Fetching all currency rates for date: {}", currencyDate);
        log.info("Fetch and Save Rates Controller IN");
        exRateSyncService.fetchAndSaveRates(currencyDate);
        log.info("Data fetched and inserted successfully from RTL!");
        return "Data fetched and inserted successfully!";
    }

    // Optional health endpoint
    @GetMapping("/delete/rlist")
    public void deleteAllRlistTable() {
        exRateSyncService.deleteAllRlistTable();
    }

    @GetMapping("/search/{date}")
    public List<RateRequest> getAllRequestByDate(@PathVariable String date) {
        log.info("Fetching all requests for date for get:" +  date);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        log.info("Fetching all requests for local date:" + localDate);
        return exRateSyncService.getRtListByDate(localDate);
      
    }

}
