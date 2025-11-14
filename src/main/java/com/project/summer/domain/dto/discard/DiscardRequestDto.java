package com.project.summer.domain.dto.discard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscardRequestDto {
    private Long storeStockId;
    private int quantity;
    private String discardReason;
}
