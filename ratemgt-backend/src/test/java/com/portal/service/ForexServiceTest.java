package com.portal.service;

import com.sampath.portal.service.ForexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class ForexServiceTest {

    @Mock RestTemplate restTemplate;
    @InjectMocks ForexService forexService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(forexService, "apiUrl", "http://api");
        ReflectionTestUtils.setField(forexService, "apiKey", "key");
    }

    @Test
    void fetch_rates_returns_response() {
        when(restTemplate.postForObject(eq("http://api"), any(HttpEntity.class), eq(String.class))).thenReturn("OK");
        String out = forexService.fetchRates();
        assertEquals("OK", out);
    }

    @Test
    void fetch_rates_builds_request_for_21_07_2024() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForObject(eq("http://api"), any(HttpEntity.class), eq(String.class))).thenReturn("OK");

        String out = forexService.fetchRates();
        assertEquals("OK", out);

        verify(restTemplate, times(1)).postForObject(eq("http://api"), captor.capture(), eq(String.class));
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) captor.getValue().getBody();
        HttpHeaders headers = (HttpHeaders) captor.getValue().getHeaders();
        assertEquals("21-07-2024", body.get("rtlDate"));
        assertEquals("Y", body.get("isCurrency"));
        assertEquals("key", headers.getFirst("apikey"));
    }

    @Test
    void fetch_rates_target_custom_gateway_url() {
        String gw = "https://192.168.129.42:8243/apis/forex/crm/getCurrencyRateData/1.0.0";
        ReflectionTestUtils.setField(forexService, "apiUrl", gw);
        when(restTemplate.postForObject(eq(gw), any(HttpEntity.class), eq(String.class))).thenReturn("OK");
        String out = forexService.fetchRates();
        assertEquals("OK", out);
        verify(restTemplate, times(1)).postForObject(eq(gw), any(HttpEntity.class), eq(String.class));
    }
}