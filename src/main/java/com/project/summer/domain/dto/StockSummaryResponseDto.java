package com.project.summer.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryResponseDto {
    private LocalDate date;
    private long dailyIncoming;
    private long dailyDiscard;
    private long totalStock;
}
