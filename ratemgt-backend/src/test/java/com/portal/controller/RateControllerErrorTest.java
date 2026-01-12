package com.portal.controller;

import com.sampath.portal.SegmentationApplication;
import com.sampath.portal.controller.RateController;

import com.sampath.portal.service.RateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateController.class)
@AutoConfigureMockMvc(addFilters = false)

class RateControllerErrorTest {

    @Autowired MockMvc mockMvc;
    @MockBean RateService rateService;

    @Test
    void get_not_found_maps_to_400() throws Exception {
        when(rateService.getRequestById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/rates/999"))
                .andExpect(status().isBadRequest());
    }
}

