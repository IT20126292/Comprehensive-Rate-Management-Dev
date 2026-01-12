package com.sampath.portal.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sampath.portal.dto.RateListResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ForexService {

    private final RestTemplate restTemplate;

    @Value("${upstream.api.url}")
    private String apiUrl;

    @Value("${upstream.api.key}")
    private String apiKey;

    public ForexService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchRates() {
        log.info("Fetch and Save Rates IN");
        Map<String, Object> request = new HashMap<>();
        request.put("reqId", "21ba2220-91ff-5f3e-44c6-3c17805fx456");
        request.put("rtlDate", "21-07-2024");
        request.put("isCurrency", "Y");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("apikey", apiKey);

        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(request, headers);

        String response = restTemplate.postForObject(apiUrl, entity, String.class);

        log.info("Response for Fetch Forex Servive" + response);
        return response;
    }
}