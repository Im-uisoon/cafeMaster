package com.project.summer.controller;

import com.project.summer.domain.dto.stockrequest.StockRequestCreateDto;
import com.project.summer.domain.dto.stockrequest.StockRequestResponseDto;
import com.project.summer.domain.dto.stockrequest.StockRequestUpdateStatusDto;
import com.project.summer.domain.stockrequest.RequestStatus;
import com.project.summer.domain.stockrequest.StockRequest;
import com.project.summer.service.StockRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StockRequestController {

    private final StockRequestService stockRequestService;

    // (STORE) 재고 요청 생성
    @PostMapping("/store/stock-requests")
    public ResponseEntity<StockRequestResponseDto> createStockRequest(
            @Valid @RequestBody StockRequestCreateDto createDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        StockRequest stockRequest = stockRequestService.createStockRequest(createDto, userDetails.getUsername());
        return new ResponseEntity<>(new StockRequestResponseDto(stockRequest), HttpStatus.CREATED);
    }

    // (STORE) 자신의 재고 요청 목록 조회
    @GetMapping("/store/stock-requests")
    public ResponseEntity<List<StockRequestResponseDto>> getStoreStockRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StockRequest> requests = stockRequestService.getStoreStockRequests(userDetails.getUsername());
        List<StockRequestResponseDto> responseDtos = requests.stream()
                .map(StockRequestResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // (MASTER) 모든 재고 요청 목록 조회 (상태별 필터링)
    @GetMapping("/master/stock-requests")
    public ResponseEntity<List<StockRequestResponseDto>> getAllStockRequests(
            @RequestParam(required = false) RequestStatus status) {
        List<StockRequest> requests = stockRequestService.getAllStockRequests(status);
        List<StockRequestResponseDto> responseDtos = requests.stream()
                .map(StockRequestResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // (MASTER) 재고 요청 상태 변경 (승인/거절)
    @PutMapping("/master/stock-requests/{requestId}/status")
    public ResponseEntity<StockRequestResponseDto> updateStockRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody StockRequestUpdateStatusDto updateDto) {
        StockRequest updatedRequest = stockRequestService.updateStockRequestStatus(requestId, updateDto);
        return ResponseEntity.ok(new StockRequestResponseDto(updatedRequest));
    }

    // (STORE) 승인된 요청을 확인하고 실제 재고로 등록
    @PostMapping("/store/stock-requests/{requestId}/confirm")
    public ResponseEntity<Void> confirmStockRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        stockRequestService.confirmStockRequest(requestId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
