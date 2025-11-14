package com.project.summer.domain.dto.discard;

import com.project.summer.domain.discard.DiscardItem;
import com.project.summer.domain.discard.DiscardType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor // 추가
public class DiscardItemResponseDto {

    private Long id;
    private String productCode;
    private String productName;
    private int quantity;
    private LocalDate expirationDate;
    private DiscardType discardType;
    private String discardReason;
    private LocalDate discardDate; // 추가된 필드
    private double amount; // 추가된 필드

    public DiscardItemResponseDto(DiscardItem discardItem) {
        this.id = discardItem.getId();
        this.productCode = discardItem.getProduct().getProductCode(); // 추가
        this.productName = discardItem.getProduct().getProductName();
        this.quantity = discardItem.getQuantity();
        this.expirationDate = discardItem.getExpirationDate();
        this.discardType = discardItem.getDiscardType();
        this.discardReason = discardItem.getDiscardReason();
        this.discardDate = discardItem.getDiscardRecord().getDiscardDate(); // 추가
        this.amount = discardItem.getProduct().getPrice() * discardItem.getQuantity(); // 추가
    }
}
