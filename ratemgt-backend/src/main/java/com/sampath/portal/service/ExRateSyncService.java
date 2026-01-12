package com.sampath.portal.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import com.sampath.portal.dto.RateListRequest;
import com.sampath.portal.dto.RateListResponse;
import com.sampath.portal.entity.CrmsExRate;
import com.sampath.portal.entity.GenInfo;
import com.sampath.portal.entity.Rate;
import com.sampath.portal.entity.RateItem;
import com.sampath.portal.entity.RateRequest;
import com.sampath.portal.entity.TbaadmRtl;
import com.sampath.portal.entity.TbaadmRtlId;
import com.sampath.portal.repository.CrmsExRateRepository;
import com.sampath.portal.repository.GenInfoRepository;
import com.sampath.portal.repository.RateRequestRepository;
import com.sampath.portal.repository.TbaadmRtlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExRateSyncService {
     @Autowired
    private RateRequestRepository rateRequestRepository;

    @Value("${upstream.api.url}")
    private String rtlApi;

    @Value("${upstream.api.key}")
    private String apiKey;

    private String rateTypeLitereal = "CURRT";

    private final TbaadmRtlRepository tbaadmRtlRepository;
    private final CrmsExRateRepository crmsExRateRepository;
    private final GenInfoRepository genInfoRepository;

    private final RestTemplate restTemplate = new RestTemplate();


    
    @Transactional
    public void processTodayRates(List<RateItem> filtered) {
        for (RateItem t : filtered) {
            try {
                log.info("Rate Item " + t);
                processSingleRow(t);
            } catch (Exception ex) {
                log.error("Failed to process tbaadm_rtl rate_code {} : {}", t.getRateCode(), ex.getMessage(), ex);
            }
        }
    }

    @Transactional
    public List<TbaadmRtl> getFilterdRTLListbyDateAndMaxListNum(LocalDate today) {
        
        log.info("Processing tbaadm_rtl for date: {}", today);

        log.info("Normal Date is before"+ today);
        Date oracleDate = Date.valueOf(today);
        log.info("Oracle Date is after "+ oracleDate);

        List<String> ratecodes = Arrays.asList("TTBY", "TTSL", "ODBY", "CBCL", "CNSR", "CBCS");

        Long maxRtlist = tbaadmRtlRepository.findMaxRtlistNumForDate(oracleDate); // getting max Rlist num for today
        if (maxRtlist == null) {
            log.info("No rtlist_num found for date: {}", today);
            return Collections.emptyList();
        }
        log.info("............................................");
        for(String ratecode : ratecodes){
            log.info("ratecodes check : " + ratecode);
        }
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        List<TbaadmRtl> filtered = tbaadmRtlRepository.findFilteredForDateAndRtlist(oracleDate, maxRtlist, ratecodes); // filter data on today and max rlist num
        log.info("Found {} tbaadm_rtl rows to process for rtlist_num {}", filtered.size(), maxRtlist);
        log.info("Filterd Full RTL Table : ");
        
    
        return filtered;
    }

    private void processSingleRow(RateItem t) {
        String fxd = t.getFixCurrencyCode();
        String rateCode = t.getRateCode() != null ? t.getRateCode().trim().toUpperCase() : null;
        Double units = t.getVarCurnyUnit();

        log.info(" ");
        log.info("fxd : {}, ratecode : {}, units: {}", fxd,rateCode,units);

        if (fxd == null || rateCode == null || units == null) {
            log.info("Skipping row because missing fxd/rateCode/units");
            return;
        }

        log.info("<<<<<<<<<< CRMSAPN.CRMS_EX_RATES table - RE_RATE_TYPE = EXRT started processing... >>>>>>>>>>");
        // Try find existing record(s) for currency
        // We will first try to find a matching row by RE_CUR_CODE and then update depending on RE_RATE_TYPE
        Optional<CrmsExRate> exRateOpt = crmsExRateRepository.findFirstByCurCodeAndRateType(fxd, "EXRT");
        if (exRateOpt.isPresent()) {
            log.info("EXRT in");
            updateExrtRow(exRateOpt.get(), rateCode, units);
        }

        log.info("<<<<<<<<<< CRMSAPN.CRMS_EX_RATES table - RE_RATE_TYPE = EXRT processing completed. >>>>>>>>>>");
        log.info(" ");
        log.info("<<<<<<<<<< CRMSAPN.CRMS_EX_RATES table - RE_RATE_TYPE = CURRT started processing... >>>>>>>>>>");

        Optional<CrmsExRate> curRateOpt = null;

        
        if(fxd.equalsIgnoreCase("USD")){
            log.info("in if fxd currency USD");
            if(rateCode.equalsIgnoreCase("CBCL")){
                log.info("in USD(L) in curreny rates");
                curRateOpt = crmsExRateRepository.findFirstByCurCodeAndRateType("USD(L)", rateTypeLitereal);
                log.info("CURRT in USD(L) out");
                log.info("updating CBCL of USD");
                updateCurrtRowUSD(curRateOpt.get(), rateCode, units);

            }else if(rateCode.equalsIgnoreCase("CBCS")){
                curRateOpt = crmsExRateRepository.findFirstByCurCodeAndRateType("USD(S)", rateTypeLitereal);
                log.info("in USD(S) in curreny rates");
                log.info("updating CBCS of USD");
                updateCurrtRowUSD(curRateOpt.get(), rateCode, units);
                
            }     
        }else{
            log.info("in fxd currency is not USD");
            // If not found EXRT row, look for CURRT row (or both), depending on the mapping rules
            curRateOpt = crmsExRateRepository.findFirstByCurCodeAndRateType(fxd, rateTypeLitereal);
            log.info("CURRT in except USD "+ curRateOpt );
            log.info("Updating CBCL except USD");
                updateCurrtRow(curRateOpt.get(), rateCode, units);
        }

        log.info("<<<<<<<<<< CRMSAPN.CRMS_EX_RATES table - RE_RATE_TYPE = CURRT processing completed. >>>>>>>>>>");
        log.info(" ");
        log.info("<<<<<<<<<< TELADMIN.GEN_INFO table started processing... >>>>>>>>>>");

        Optional<GenInfo> genInfo = genInfoRepository.findByGenCurrType(fxd.toUpperCase());
        log.info(genInfo.toString());
        log.info("  " + genInfo.isPresent());
        if (genInfo.isPresent()) {
            log.info("Gen Info in");
            updateGenInfoRow(genInfo.get(), rateCode, units);
        }

        log.info("<<<<<<<<<< TELADMIN.GEN_INFO table processing completed >>>>>>>>>>");


        // If neither exists, create a new row with default rateType depending on likely mapping
        // We'll create an EXRT entry by default if rate codes are TTBY/ODBY/TTSL etc, otherwise CURRT for BYOVC mapping
    } 

    private void updateGenInfoRow(GenInfo row, String rateCode, Double units) {
        log.info("GEN Info in {},  {}",rateCode,units.toString() );
        boolean changed = false;
        switch (rateCode.toUpperCase()) {
            case "TTBY":
                row.setGenEbuyRate(units);
                changed = true;
                break;
            case "CBCL":
                row.setGenCnbuyRate(units);
                changed = true;
                break;
            case "TTSL":
                row.setGenEsellRate(units);
                changed = true;
                break;
            case "CNSR":
                row.setGenCnsellRate(units);
                changed = true;
                break;
            // add other mappings if required
            default:
                log.info("Gen Info Table mapping missing for rateCode {}", rateCode);
        }
        log.info("Gen Info changed : {}", changed);

        if (changed) {
            genInfoRepository.save(row);
            log.info("Updated Gen Info row curCode={} column for rateCode={} with value={}", row.getGenCurrType(), rateCode, units);
        }

    }

    private void updateCurrtRow(CrmsExRate row, String rateCode, Double units) {
        log.info("CURRT in {},  {}",rateCode,units.toString() );
        boolean changed = false;
        switch (rateCode.toUpperCase()) {
            case "CBCL":
                log.info("var cur Units : " + units);
                log.info("Rate Code : " + rateCode);
                log.info("Cur Code : " + row.getCurCode());
                row.setReByOvc(units);
                changed = true;
                break;
    
            // add other CURRT mappings if required
            default:
                log.info("CURRT mapping missing for rateCode {}", rateCode);
        }
        log.info("CURRT changed : {}", changed);
        if (changed) {
            row.setRecWefTime(LocalDateTime.now());
            crmsExRateRepository.save(row);
            log.info("Updated CURRT row id={} curCode={} column for rateCode={} with value={}", row.getId(), row.getCurCode(), rateCode, units);
        }
    }

    private void updateCurrtRowUSD(CrmsExRate row, String rateCode, Double units) {
        log.info("CURRT in {},  {}",rateCode,units.toString() );
        boolean changed = false;
        switch (rateCode.toUpperCase()) {
            case "CBCL",
                 "CBCS":
                log.info("var cur Units : " + units);
                log.info("Rate Code : " + rateCode);
                log.info("Cur Code : " + row.getCurCode());
                row.setReByOvc(units);
                changed = true;
                break;
    
            // add other CURRT mappings if required
            default:
                log.info("CURRT mapping missing for rateCode {}", rateCode);
        }
        log.info("CURRT changed : {}", changed);
        if (changed) {
            row.setRecWefTime(LocalDateTime.now());
            crmsExRateRepository.save(row);
            log.info("Updated CURRT row id={} curCode={} column for rateCode={} with value={}", row.getId(), row.getCurCode(), rateCode, units);
        }
    }

    private void updateExrtRow(CrmsExRate row, String rateCode, Double units) {
        log.info("EXRT in {},  {}",rateCode,units.toString() );
        boolean changed = false;
        switch (rateCode.toUpperCase()) {
            case "TTBY":
                row.setReTtBuy(units);
                changed = true;
                break;
            case "ODBY":
                row.setReOdBuy(units);
                changed = true;
                break;
            case "TTSL":
                row.setReTtSel(units);
                changed = true;
                break;
            // add other mappings if required
            default:
                log.info("EXRT mapping missing for rateCode {}", rateCode);
        }
         log.info("EXRT changed : {}", changed);
         if (changed) {
            row.setRecWefTime(LocalDateTime.now());
            crmsExRateRepository.save(row);
            log.info("Updated EXRT row id={} curCode={} column for rateCode={} with value={}", row.getId(), row.getCurCode(), rateCode, units);
        }
    }
    
    //Save RTL Data To Table
    public void fetchAndSaveRates(String date) {
        
        log.info("Fetch and Save Rates IN");

        RateListRequest rateRequestY = new RateListRequest();
        rateRequestY.setReqId("21ba2220-91ff-5f3e-44c6-3c17805fx234");
        rateRequestY.setRtlDate(date);
        rateRequestY.setIsCurrency("Y");

        RateListRequest rateRequestN = new RateListRequest();
        rateRequestN.setReqId("21ba2220-91ff-5f3e-44c6-3c17805fx432");
        rateRequestN.setRtlDate(date);
        rateRequestN.setIsCurrency("N");

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        // Prepare request
        HttpEntity<RateListRequest> requestY = new HttpEntity<>(rateRequestY, headers);
        HttpEntity<RateListRequest> requestN = new HttpEntity<>(rateRequestN, headers);


        // Call API and map response into same DTO
        RateListResponse responseY = restTemplate.postForObject(rtlApi, requestY, RateListResponse.class);
        RateListResponse responseN = restTemplate.postForObject(rtlApi, requestN, RateListResponse.class);

        List<Rate> combinedList = new ArrayList<Rate>();

        combinedList.addAll(responseY.getRtlist());
        combinedList.addAll(responseN.getRtlist());


        log.info("Respnse : >>> ");
        log.info(combinedList.toString());



        if (combinedList.size() > 0) {
            log.info("Response IN.......");


            combinedList.forEach(item -> {
                log.info("Response getRtlist loop IN.......");
                try {
                    // --- Build composite key ---
                    TbaadmRtlId id = TbaadmRtlId.builder()
                            .fxdCrncyCode(item.getFxdCrncyCode())
                            .varCrncyCode(item.getVarCrncyCode())
                            .rtlistNum(item.getRtListNum())
                            .rtlistDate(LocalDate.parse(item.getRtListDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                            .rateCode(item.getRateCode())
                            .build();
                    log.info("TbaadmRtlId out.......");
                    // --- Map fields ---
                    TbaadmRtl entityObj = TbaadmRtl.builder()
                            .id(id)
                            .fxdCrncyUnit(new BigDecimal(item.getFxdCrncyUnits()))
                            .varCrncyUnit(new BigDecimal(item.getVarCrncyUnits()))
                            .custVarCrncyUnit(new BigDecimal(item.getCustVarCrncyUnits()))
                            .userId(item.getLchgUserId())
                            .lchgTime(LocalDateTime.parse(item.getLchgTime(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .creUserId(item.getRcreUserId())
                            .rcreTime(LocalDateTime.parse(item.getRcreTime(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .varCrncyTolpcntPlus(new BigDecimal(item.getVarCrncyTolPcntPlus()))
                            .varCrncyTolpcntMinus(new BigDecimal(item.getVarCrncyTolPcntMinus()))
                            .srlNum(item.getSrlNum())
                            .bankId(item.getBankId())
                            .entityCreFlag(item.getEntityCreFlg().charAt(0))
                            .delFlag(item.getDelFlg().charAt(0))
                            .build();

                    log.info("TbaadmRtlId out.......");
                    tbaadmRtlRepository.save(entityObj);
                    
                    log.info("Successfully Inserted to RDL");

                } catch (Exception e) {
                    log.error("Error saving record: >>>" + e.getMessage());
                }
            });

        }else{
            log.info("Data Inserted to RTL failed>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        
    }

    @GetMapping("/delete/rlisttable")
    public void deleteAllRlistTable() {
        tbaadmRtlRepository.deleteAll();
    }

    public List<RateRequest> getRtListByDate(LocalDate date) {
        log.info("Search date : " + date);
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        return rateRequestRepository.findByDate(sqlDate); // use injected instance
    }

}

