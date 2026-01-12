package com.sampath.portal.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sampath.portal.dto.RateListResponse;
import com.sampath.portal.service.ForexService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/exrates")
@Slf4j
public class ForexController {

    private final ForexService forexService;

    public ForexController(ForexService forexService) {
        this.forexService = forexService;
    }

    @PostMapping("/fetchAll")
    public String fetchRates() {
        log.info("Fetch Forex IN");
        return forexService.fetchRates();
    }
}
