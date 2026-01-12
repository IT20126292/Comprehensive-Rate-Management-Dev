package com.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.controller.ExRateSyncController;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.service.ExRateSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExRateSyncController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SegmentationApplication.class)
class ExRateSyncControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ExRateSyncService exRateSyncService;

    @Test
    void process_today() throws Exception {
        RateItem ri = new RateItem();
        ri.setFixCurrencyCode("USD");
        ri.setRateCode("TTBY");

        doNothing().when(exRateSyncService).processTodayRates(any(List.class));

        mockMvc.perform(post("/api/exrates/process/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Collections.singletonList(ri))))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"));
    }

    @Test
    void ping_ok() throws Exception {
        mockMvc.perform(get("/api/exrates/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void currencies_by_date_empty() throws Exception {
        when(exRateSyncService.getFilterdRTLListbyDateAndMaxListNum(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/exrates/currencies/26-07-2025"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
}
