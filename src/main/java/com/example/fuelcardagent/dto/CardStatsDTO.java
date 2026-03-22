package com.example.fuelcardagent.dto;

import java.math.BigDecimal;

public record CardStatsDTO(
        long totalGasStationCards,
        BigDecimal averageCreditLimit,
        BigDecimal totalCreditLimit
) {}
