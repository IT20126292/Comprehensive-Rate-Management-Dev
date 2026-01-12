package com.portal.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;

@SpringBootTest(classes = SegmentationApplication.class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use real DB
@ActiveProfiles("test") // Use test profile
@Disabled("Disabled: relies on real DB and data; using mock-based tests instead")
class RateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RateRequest testRequest;

    @BeforeEach
    void setup() {
        // Prepare a sample request
        RateItem item = new RateItem();
        item.setFixCurrencyCode("USD");
        item.setRateCode("TTBY");

        testRequest = new RateRequest();
        testRequest.setRequestedBy("maker1");
        testRequest.setRateItems(Collections.singletonList(item));
        testRequest.setStatus("In Progress");
        testRequest.setRequestedAt(LocalDateTime.now());
    }

    @Test
    void testSubmitRateRequest() throws Exception {
        mockMvc.perform(post("/api/rates/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedBy").value("maker1"))
                .andExpect(jsonPath("$.rateItems[0].currencyCode").value("USD"));
    }

    @Test
    void testGetAllRequests() throws Exception {
        mockMvc.perform(get("/api/rates/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetRequestById() throws Exception {
        // Make sure an ID exists in DB 
        mockMvc.perform(get("/api/rates/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testApproveRequest() throws Exception {
        mockMvc.perform(put("/api/rates/approve/5")
                        .param("reviewer", "checker1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testRejectRequest() throws Exception {
        mockMvc.perform(put("/api/rates/reject/5")
                        .param("reviewer", "checker1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void testGetCurrenciesForDate() throws Exception {
        LocalDate date = LocalDate.of(2025, 7, 26); // Ensure this date exists in DB
        mockMvc.perform(get("/api/rates/currencies/" + date))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
