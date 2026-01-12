package com.portal.service;

import com.sampath.portal.entity.CrmsExRate;
import com.sampath.portal.entity.GenInfo;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.entity.TbaadmRtl;
import com.sampath.portal.repository.CrmsExRateRepository;
import com.sampath.portal.repository.GenInfoRepository;
import com.sampath.portal.repository.RateRequestRepository;
import com.sampath.portal.repository.TbaadmRtlRepository;
import com.sampath.portal.service.ExRateSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExRateSyncServiceTest {

    @Mock TbaadmRtlRepository tbaadmRtlRepository;
    @Mock CrmsExRateRepository crmsExRateRepository;
    @Mock GenInfoRepository genInfoRepository;
    @Mock RateRequestRepository rateRequestRepository;

    @InjectMocks ExRateSyncService service;

    @Test
    void process_today_non_usd_exrt_and_currt() {
        // EXRT update for TTBY
        CrmsExRate exrt = CrmsExRate.builder().id(1L).curCode("EUR").rateType("EXRT").build();
        when(crmsExRateRepository.findFirstByCurCodeAndRateType("EUR", "EXRT"))
                .thenReturn(Optional.of(exrt));

        // CURRT update for CBCL (non-USD)
        CrmsExRate currt = CrmsExRate.builder().id(2L).curCode("EUR").rateType("CURRT").build();
        when(crmsExRateRepository.findFirstByCurCodeAndRateType("EUR", "CURRT"))
                .thenReturn(Optional.of(currt));

        // GenInfo entry present
        when(genInfoRepository.findByGenCurrType("EUR")).thenReturn(Optional.of(GenInfo.builder().genCurrType("EUR").build()));

        RateItem r1 = RateItem.builder().fixCurrencyCode("EUR").rateCode("TTBY").varCurnyUnit(1.23).build();
        RateItem r2 = RateItem.builder().fixCurrencyCode("EUR").rateCode("CBCL").varCurnyUnit(2.34).build();

        service.processTodayRates(List.of(r1, r2));

        // verify EXRT save
        verify(crmsExRateRepository, atLeastOnce()).save(argThat(row ->
                row.getId().equals(1L) && Double.valueOf(1.23).equals(row.getReTtBuy())
        ));
        // verify CURRT save
        verify(crmsExRateRepository, atLeastOnce()).save(argThat(row ->
                row.getId().equals(2L) && Double.valueOf(2.34).equals(row.getReByOvc())
        ));
        // verify GenInfo save called at least once
        verify(genInfoRepository, atLeastOnce()).save(any(GenInfo.class));
    }

    @Test
    void process_today_usd_l_and_s() {
        // USD(L)
        CrmsExRate usdL = CrmsExRate.builder().id(10L).curCode("USD(L)").rateType("CURRT").build();
        when(crmsExRateRepository.findFirstByCurCodeAndRateType("USD(L)", "CURRT"))
                .thenReturn(Optional.of(usdL));
        // USD(S)
        CrmsExRate usdS = CrmsExRate.builder().id(11L).curCode("USD(S)").rateType("CURRT").build();
        when(crmsExRateRepository.findFirstByCurCodeAndRateType("USD(S)", "CURRT"))
                .thenReturn(Optional.of(usdS));

        when(genInfoRepository.findByGenCurrType("USD")).thenReturn(Optional.of(GenInfo.builder().genCurrType("USD").build()));

        RateItem l = RateItem.builder().fixCurrencyCode("USD").rateCode("CBCL").varCurnyUnit(100.0).build();
        RateItem s = RateItem.builder().fixCurrencyCode("USD").rateCode("CBCS").varCurnyUnit(200.0).build();

        service.processTodayRates(List.of(l, s));

        verify(crmsExRateRepository).save(argThat(row -> row.getId().equals(10L) && Double.valueOf(100.0).equals(row.getReByOvc())));
        verify(crmsExRateRepository).save(argThat(row -> row.getId().equals(11L) && Double.valueOf(200.0).equals(row.getReByOvc())));
    }

    @Test
    void rtl_list_by_date_empty_when_no_max() {
        when(tbaadmRtlRepository.findMaxRtlistNumForDate(any())).thenReturn(null);
        List<TbaadmRtl> out = service.getFilterdRTLListbyDateAndMaxListNum(LocalDate.of(2025, 7, 26));
        assertTrue(out.isEmpty());
    }

    @Test
    void get_rt_list_by_date_delegates_repo() {
        RateRequest rr = new RateRequest();
        rr.setId(7L);
        when(rateRequestRepository.findByDate(any())).thenReturn(List.of(rr));
        List<RateRequest> out = service.getRtListByDate(LocalDate.of(2025, 7, 26));
        assertEquals(1, out.size());
        assertEquals(7L, out.get(0).getId());
    }

    @Test
    void delete_all_rlist_table_calls_repo() {
        service.deleteAllRlistTable();
        verify(tbaadmRtlRepository).deleteAll();
    }
}

