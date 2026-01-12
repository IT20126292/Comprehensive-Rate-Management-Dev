package com.portal.service;

import com.sampath.portal.entity.CurrencyMaster;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.repository.CurrencyMasterRepository;
import com.sampath.portal.repository.RateRequestRepository;
import com.sampath.portal.service.RateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock RateRequestRepository rateRequestRepository;
    @Mock CurrencyMasterRepository currencyMasterRepository;

    @InjectMocks RateService rateService;

    RateRequest existing;

    @BeforeEach
    void setup() {
        existing = new RateRequest();
        existing.setId(5L);
        existing.setStatus("In Progress");
    }

    @Test
    void submit_saves() {
        when(rateRequestRepository.save(any(RateRequest.class))).thenAnswer(i -> i.getArgument(0));
        RateRequest saved = rateService.submitRateRequest(existing);
        verify(rateRequestRepository).save(existing);
        assertEquals("In Progress", saved.getStatus());
    }

    @Test
    void get_all_requests() {
        when(rateRequestRepository.findLatest5Records()).thenReturn(List.of(existing));
        List<RateRequest> all = rateService.getAllRequests();
        assertEquals(1, all.size());
    }

    @Test
    void get_by_id() {
        when(rateRequestRepository.findById(5L)).thenReturn(Optional.of(existing));
        Optional<RateRequest> out = rateService.getRequestById(5L);
        assertEquals(5L, out.get().getId());
    }

    @Test
    void approve_sets_fields_and_saves() {
        when(rateRequestRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(rateRequestRepository.save(any(RateRequest.class))).thenAnswer(i -> i.getArgument(0));

        RateRequest input = new RateRequest();
        input.setStatus("APPROVED");
        input.setComent("ok");

        RateRequest result = rateService.approveRequest(5L, "checker1", input);

        verify(rateRequestRepository).save(existing);
        assertEquals("APPROVED", result.getStatus());
        assertEquals("checker1", result.getReviewedBy());
        assertEquals("ok", result.getComent());
    }

    @Test
    void reject_sets_fields_and_saves() {
        when(rateRequestRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(rateRequestRepository.save(any(RateRequest.class))).thenAnswer(i -> i.getArgument(0));

        RateRequest result = rateService.rejectRequest(5L, "checker1");

        verify(rateRequestRepository).save(existing);
        assertEquals("REJECTED", result.getStatus());
        assertEquals("checker1", result.getReviewedBy());
        assertEquals("checker1", result.getComent());
    }

    @Test
    void get_all_currencies_by_date_converts_sql_date() {
        LocalDate ld = LocalDate.of(2025, 7, 26);
        when(currencyMasterRepository.findByCurrencyDate(any(Date.class))).thenReturn(List.of(new CurrencyMaster()));

        rateService.getAllCurrenciesByDate(ld);

        ArgumentCaptor<Date> captor = ArgumentCaptor.forClass(Date.class);
        verify(currencyMasterRepository).findByCurrencyDate(captor.capture());
        assertEquals(Date.valueOf(ld), captor.getValue());
    }
}

