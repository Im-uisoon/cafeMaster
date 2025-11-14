package com.project.summer.domain.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockCreateRequestDto {
    @NotBlank(message = "상품 코드는 필수 입력 항목입니다.")
    private String productCode; // 어떤 상품에 대한 재고인지 식별하기 위한 상품 코드

    @NotNull(message = "유통기한은 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // "2025-12-31" 형식으로 받기 위함
    private LocalDate expirationDate;

    @Min(value = 1, message = "개수는 1개 이상이어야 합니다.")
    private int quantity;
}
