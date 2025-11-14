package com.project.summer.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequestDto {
    @NotBlank(message = "상품 코드는 필수 입력 항목입니다.")
    private String productCode;

    @NotBlank(message = "상품명은 필수 입력 항목입니다.")
    private String productName;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private int price;
}
