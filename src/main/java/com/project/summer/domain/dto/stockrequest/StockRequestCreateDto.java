package com.project.summer.domain.dto.stockrequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestCreateDto {

    @NotEmpty(message = "요청 항목이 하나 이상 있어야 합니다.")
    @Valid // 내부 DTO의 유효성 검사도 함께 수행
    private List<StockRequestItemDto> items;
}
