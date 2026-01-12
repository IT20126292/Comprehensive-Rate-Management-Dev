package com.portal.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.controller.RateController;
import com.sampath.portal.entity.CurrencyMaster;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.service.RateService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SegmentationApplication.class)
class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateService rateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSubmitRateRequest() throws Exception {
        RateItem item = new RateItem();
        item.setFixCurrencyCode("USD");
        item.setRateCode("TTBY");

        RateRequest request = new RateRequest();
        request.setRequestedBy("maker1");
        request.setRateItems(Collections.singletonList(item));
        request.setStatus("In Progress");

        when(rateService.submitRateRequest(any(RateRequest.class))).thenReturn(request);

        mockMvc.perform(post("/api/rates/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedBy").value("maker1"));
    }

    @Test
    void testGetAllRequests() throws Exception {
        RateRequest request = new RateRequest();
        request.setId(5L);
        request.setRequestedBy("maker1");

        when(rateService.getAllRequests()).thenReturn(Collections.singletonList(request));

        mockMvc.perform(get("/api/rates/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestedBy").value("maker1"));
    }

    @Test
    void testGetRequestById() throws Exception {
        RateRequest mockRequest = new RateRequest();
        mockRequest.setId(5L);

        when(rateService.getRequestById(5L)).thenReturn(Optional.of(mockRequest));

        mockMvc.perform(get("/api/rates/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void testApproveRequest() throws Exception {
        RateRequest body = new RateRequest();
        body.setStatus("APPROVED");
        body.setComent("Looks good");

        RateRequest response = new RateRequest();
        response.setId(5L);
        response.setStatus("APPROVED");
        response.setComent("Looks good");

        when(rateService.approveRequest(eq(5L), eq("checker1"), any(RateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/rates/approve/5")
                        .param("reviewer", "checker1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.coment").value("Looks good"));
    }

    @Test
    void testRejectRequest() throws Exception {
        RateRequest request = new RateRequest();
        request.setId(5L);
        request.setStatus("REJECTED");

        when(rateService.rejectRequest(5L, "checker1")).thenReturn(request);

        mockMvc.perform(put("/api/rates/reject/5")
                        .param("reviewer", "checker1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void testGetCurrenciesForDate() throws Exception {
        CurrencyMaster currency = new CurrencyMaster();
        currency.setCurrencyCode("USD");
        currency.setCurrencyRate(300.5);
        currency.setCurrencyDate(LocalDate.of(2025, 7, 26));

        when(rateService.getAllCurrenciesByDate(LocalDate.of(2025, 7, 26)))
                .thenReturn(Collections.singletonList(currency));

        mockMvc.perform(get("/api/rates/currencies/2025-07-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currencyCode").value("USD"));
    }
}
