package com.project.summer.domain.dto.discard;

import com.project.summer.domain.discard.DiscardRecord;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MonthlyDiscardSummaryResponseDto {

    private LocalDate discardDate;
    private int totalDiscardAmount;
    private int totalDiscardProductCount;

    public MonthlyDiscardSummaryResponseDto(DiscardRecord discardRecord) {
        this.discardDate = discardRecord.getDiscardDate();
        this.totalDiscardAmount = discardRecord.getTotalDiscardAmount();
        this.totalDiscardProductCount = discardRecord.getTotalDiscardProductCount();
    }
}
