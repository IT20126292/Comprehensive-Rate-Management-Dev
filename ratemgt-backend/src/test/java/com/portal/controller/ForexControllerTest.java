package com.portal.controller;

import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.controller.ForexController;
import com.sampath.portal.service.ForexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForexController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SegmentationApplication.class)
class ForexControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ForexService forexService;

    @Test
    void fetch_all_ok() throws Exception {
        when(forexService.fetchRates()).thenReturn("OK");
        mockMvc.perform(post("/api/exrates/fetchAll"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
