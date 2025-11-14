package com.project.summer.domain.dto.stockrequest;

import com.project.summer.domain.stockrequest.RequestStatus;
import com.project.summer.domain.stockrequest.StockRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestResponseDto {

    private Long id;
    private String storeName; // 요청한 매장 이름
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private String rejectionReason;
    private List<StockRequestItemResponseDto> items;

    public StockRequestResponseDto(StockRequest stockRequest) {
        this.id = stockRequest.getId();
        this.storeName = stockRequest.getUser().getStoreName();
        this.status = stockRequest.getStatus();
        this.requestedAt = stockRequest.getRequestedAt();
        this.rejectionReason = stockRequest.getRejectionReason();
        this.items = stockRequest.getItems().stream()
                .map(StockRequestItemResponseDto::new)
                .collect(Collectors.toList());
    }
}
