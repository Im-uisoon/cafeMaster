package com.project.summer.controller;

import com.project.summer.domain.stock.Stock;
import com.project.summer.domain.stock.StockService;
import com.project.summer.domain.stock.dto.StockCreateRequestDto;
import com.project.summer.domain.stock.dto.StockUpdateRequestDto;
import com.project.summer.domain.stock.dto.StockResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master/stocks")
public class StockController {
    private final StockService stockService;

    // 재고 항목 추가
    @PostMapping
    public ResponseEntity<StockResponseDto> createStock(@Valid @RequestBody StockCreateRequestDto requestDto) {
        StockResponseDto stock = stockService.createStock(requestDto); // 서비스에서 DTO 반환
        return new ResponseEntity<>(stock, HttpStatus.CREATED);
    }

    // 모든 재고 항목 조회
    @GetMapping
    public ResponseEntity<List<StockResponseDto>> getAllStocks() {
        List<StockResponseDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    // 특정 재고 항목 조회
    @GetMapping("/{id}")
    public ResponseEntity<StockResponseDto> getStockById(@PathVariable Long id) {
        StockResponseDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    // 재고 항목 수정
    @PutMapping("/{id}")
    public ResponseEntity<StockResponseDto> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequestDto requestDto) {
        StockResponseDto updatedStock = stockService.updateStock(id, requestDto); // 서비스에서 DTO 반환
        return ResponseEntity.ok(updatedStock);
    }

    // 재고 항목 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    
}
