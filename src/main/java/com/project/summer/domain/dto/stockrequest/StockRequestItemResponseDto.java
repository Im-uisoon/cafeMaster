package com.project.summer.domain.dto.stockrequest;

import com.project.summer.domain.stockrequest.StockRequestItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestItemResponseDto {

    private String productCode; // 추가된 필드
    private String productName;
    private int quantity;

    public StockRequestItemResponseDto(StockRequestItem item) {
        this.productCode = item.getProduct().getProductCode(); // productCode 설정
        this.productName = item.getProduct().getProductName();
        this.quantity = item.getQuantity();
    }
}
