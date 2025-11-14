package com.project.summer.domain.stock.dto;

import jakarta.validation.constraints.Min;
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
public class StockUpdateRequestDto {
    @NotNull(message = "유통기한은 필수 입력 항목입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Min(value = 0, message = "개수는 0 이상이어야 합니다.") // 재고를 0으로 만들 수도 있으므로 0 이상으로 설정
    private int quantity;
}
