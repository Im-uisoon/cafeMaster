package com.project.summer.domain.discard.dto;

import com.project.summer.domain.discard.DiscardItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscardRecordResponseDto {
    private Long id;
    private String productCode;
    private String productName;
    private LocalDate expirationDate;
    private int quantity;
    private String discardReason;
    private LocalDate discardDate;
    private double amount;

    public DiscardRecordResponseDto(DiscardItem item) {
        this.id = item.getId();
        this.productCode = item.getProduct().getProductCode();
        this.productName = item.getProduct().getProductName();
        this.expirationDate = item.getExpirationDate();
        this.quantity = item.getQuantity();
        this.discardReason = item.getDiscardReason();
        this.discardDate = item.getDiscardRecord().getDiscardDate(); // DiscardRecord에서 폐기일자 가져옴
        // 금액은 DiscardItem에 직접 없으므로, Product의 단가와 수량을 곱하여 계산하거나 백엔드에서 제공해야 함
        // 현재는 임시로 0으로 설정하거나, Product에서 단가를 가져와 계산
        this.amount = item.getProduct().getPrice() * item.getQuantity(); // Product에 price 필드가 있다고 가정
    }
}
