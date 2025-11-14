package com.project.summer.domain.dto.discard;

import com.project.summer.domain.discard.DiscardRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor // 추가
public class DailyDiscardRecordResponseDto {

    private Long id;
    private String storeName;
    private LocalDate discardDate;
    private int totalDiscardAmount;
    private int totalDiscardProductCount;
    private List<DiscardItemResponseDto> discardItems;

    public DailyDiscardRecordResponseDto(DiscardRecord discardRecord) {
        this.id = discardRecord.getId();
        this.storeName = discardRecord.getUser().getStoreName();
        this.discardDate = discardRecord.getDiscardDate();
        this.totalDiscardAmount = discardRecord.getTotalDiscardAmount();
        this.totalDiscardProductCount = discardRecord.getTotalDiscardProductCount();
        this.discardItems = discardRecord.getDiscardItems().stream()
                .map(DiscardItemResponseDto::new)
                .collect(Collectors.toList());
    }
}
