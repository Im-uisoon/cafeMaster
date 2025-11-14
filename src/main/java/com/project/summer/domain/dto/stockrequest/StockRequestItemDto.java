package com.project.summer.domain.dto.stockrequest;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestItemDto {

    @NotBlank(message = "상품 코드는 필수입니다.")
    private String productCode;

    private String productName; // 추가된 필드

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private int quantity;

    public StockRequestItemDto(String productCode, int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }
}
