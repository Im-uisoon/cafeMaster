package com.project.summer.controller;

import com.project.summer.domain.storestock.StoreStockService;
import com.project.summer.domain.storestock.dto.StoreStockCreateRequestDto;
import com.project.summer.domain.storestock.dto.StoreStockResponseDto;
import com.project.summer.domain.storestock.dto.StoreStockUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/stocks")
public class StoreStockController {

    private final StoreStockService storeStockService;

    // 지점 재고 조회 (MASTER는 전체, STORE는 자기 것만)
    @GetMapping
    public ResponseEntity<List<StoreStockResponseDto>> getStoreStocks(Authentication authentication) {
        String storeCode = authentication.getName();
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        List<StoreStockResponseDto> responseDtoList = storeStockService.getStoreStocks(storeCode, authority);
        return ResponseEntity.ok(responseDtoList);
    }

    // 지점 재고 수정
    @PutMapping("/{stockId}")
    public ResponseEntity<StoreStockResponseDto> updateStoreStock(@PathVariable Long stockId,
                                                                  @Valid @RequestBody StoreStockUpdateRequestDto requestDto,
                                                                  Authentication authentication) {
        String storeCode = authentication.getName();
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        StoreStockResponseDto responseDto = storeStockService.updateStoreStock(stockId, requestDto, storeCode, authority);
        return ResponseEntity.ok(responseDto);
    }

    // 지점 재고 삭제
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deleteStoreStock(@PathVariable Long stockId, Authentication authentication) {
        String storeCode = authentication.getName();
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);

        storeStockService.deleteStoreStock(stockId, storeCode, authority);
        return ResponseEntity.noContent().build();
    }
}
