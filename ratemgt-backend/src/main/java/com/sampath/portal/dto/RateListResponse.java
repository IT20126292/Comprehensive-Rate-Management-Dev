package com.sampath.portal.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sampath.portal.entity.Rate;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateListResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("rtlList")
    private List<Rate> rtlist;
}