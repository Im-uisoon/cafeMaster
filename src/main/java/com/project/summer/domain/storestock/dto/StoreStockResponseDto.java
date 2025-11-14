package com.project.summer.domain.storestock.dto;

import com.project.summer.domain.storestock.StoreStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStockResponseDto {

    private Long id;
    private String storeCode;
    private String storeName;
    private String productCode;
    private String productName;
    private int quantity;
    private LocalDate expirationDate;

    public static StoreStockResponseDto from(StoreStock storeStock) {
        return StoreStockResponseDto.builder()
                .id(storeStock.getId())
                .storeCode(storeStock.getUser().getStoreCode())
                .storeName(storeStock.getUser().getStoreName())
                .productCode(storeStock.getProduct().getProductCode())
                .productName(storeStock.getProduct().getProductName())
                .quantity(storeStock.getQuantity())
                .expirationDate(storeStock.getExpirationDate())
                .build();
    }
}
