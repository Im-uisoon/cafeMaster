package com.project.summer.domain.dto.discard;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManualDiscardRequestDto {

    @NotNull(message = "폐기할 재고 ID는 필수입니다.")
    private Long storeStockId;

    @Min(value = 1, message = "폐기 수량은 1개 이상이어야 합니다.")
    private int quantity;

    @NotBlank(message = "폐기 사유는 필수입니다.")
    private String discardReason;
}
