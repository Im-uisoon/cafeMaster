package com.project.summer.domain.dto.stockrequest;

import com.project.summer.domain.stockrequest.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestUpdateStatusDto {

    @NotNull(message = "상태는 필수입니다.")
    private RequestStatus status;

    private String rejectionReason; // 거절 시에만 사용
}
