package com.project.summer.domain.stock.dto;

import com.project.summer.domain.stock.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponseDto {
    private Long id;
    private String productCode;
    private String productName;
    private int productPrice; // Product에서 가져온 가격 정보 추가
    private LocalDate expirationDate;
    private int quantity;

    // Stock 엔티티를 받아서 DTO로 변환하는 정적 팩토리 메서드
    public static StockResponseDto from(Stock stock) {
        // LAZY 로딩된 product 엔티티를 실제로 사용하기 전에 로딩되도록 강제
        // stock.getProduct().getProductCode() 등의 호출이 트랜잭션 내에서 이루어져야 합니다.
        return StockResponseDto.builder()
                .id(stock.getId())
                .productCode(stock.getProduct().getProductCode())
                .productName(stock.getProduct().getProductName())
                .productPrice(stock.getProduct().getPrice())
                .expirationDate(stock.getExpirationDate())
                .quantity(stock.getQuantity())
                .build();
    }
}
