package com.project.summer.domain.stock;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.stock.dto.StockCreateRequestDto;
import com.project.summer.domain.stock.dto.StockResponseDto;
import com.project.summer.domain.stock.dto.StockUpdateRequestDto;
import com.project.summer.repository.ProductRepository;
import com.project.summer.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockResponseDto createStock(StockCreateRequestDto requestDto) {
        Product product = productRepository.findByProductCode(requestDto.getProductCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품 코드를 가진 상품이 존재하지 않습니다."));

        Stock stock = Stock.builder()
                .product(product)
                .expirationDate(requestDto.getExpirationDate())
                .quantity(requestDto.getQuantity())
                .build();
        Stock savedStock = stockRepository.save(stock);
        return StockResponseDto.from(savedStock);
    }

    // DTO 리스트 반환으로 변경
    public List<StockResponseDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(StockResponseDto::from)
                .collect(Collectors.toList());
    }

    // DTO 반환으로 변경
    public StockResponseDto getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 재고 항목을 찾을 수 없습니다."));
        return StockResponseDto.from(stock);
    }

    @Transactional
    public StockResponseDto updateStock(Long id, StockUpdateRequestDto requestDto) {
        Stock existingStock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 재고 항목을 찾을 수 없습니다."));

        // 유통기한과 개수만 업데이트 (Product는 그대로 유지)
        existingStock.update(requestDto.getExpirationDate(), requestDto.getQuantity());
        return StockResponseDto.from(existingStock);
    }

    @Transactional
    public void deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 재고 항목을 찾을 수 없습니다.");
        }
        stockRepository.deleteById(id);
    }

    @Transactional
    public void deleteExpiredStocks() {
        LocalDate today = LocalDate.now(); // 오늘 날짜를 가져옵니다.
        // 오늘까지의 날짜 유통기한인건 자동 폐기
        List<Stock> expiredStocks = stockRepository.findByExpirationDateBefore(today.plusDays(1));
        if (!expiredStocks.isEmpty()) {
            stockRepository.deleteAll(expiredStocks);
            System.out.println(today + " 날짜로 만료된 재고 " + expiredStocks.size() + "개가 삭제되었습니다.");
        } else {
            System.out.println(today + " 날짜로 만료된 재고가 없습니다.");
        }
    }
}