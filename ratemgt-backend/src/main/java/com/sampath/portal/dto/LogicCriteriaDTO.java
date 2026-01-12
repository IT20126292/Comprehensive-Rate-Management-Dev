package com.sampath.portal.dto;

import java.math.BigDecimal;

public record LogicCriteriaDTO(
        BigDecimal logicCode,
        String shortCode,
        String codeDescription
) {}