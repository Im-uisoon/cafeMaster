package com.project.summer.service;

import com.project.summer.domain.dto.stockrequest.StockRequestCreateDto;
import com.project.summer.domain.dto.stockrequest.StockRequestUpdateStatusDto;
import com.project.summer.domain.product.Product;
import com.project.summer.domain.stockrequest.RequestStatus;
import com.project.summer.domain.stockrequest.StockRequest;
import com.project.summer.domain.stockrequest.StockRequestItem;
import com.project.summer.domain.storestock.StoreStock;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockRequestService {

    private final StockRequestRepository stockRequestRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StoreStockRepository storeStockRepository;
    private final StockRepository stockRepository;

    // (STORE) 재고 요청 생성
    @Transactional
    public StockRequest createStockRequest(StockRequestCreateDto createDto, String username) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        StockRequest stockRequest = new StockRequest(user);

        createDto.getItems().forEach(itemDto -> {
            Product product = productRepository.findByProductCode(itemDto.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemDto.getProductCode()));
            stockRequest.addItem(new StockRequestItem(product, itemDto.getQuantity()));
        });

        return stockRequestRepository.save(stockRequest);
    }

    // (STORE) 자신의 재고 요청 목록 조회
    public List<StockRequest> getStoreStockRequests(String username) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return stockRequestRepository.findByUser_IdOrderByRequestedAtDesc(user.getId());
    }

    // (MASTER) 모든 재고 요청 목록 조회 (상태별 필터링)
    public List<StockRequest> getAllStockRequests(RequestStatus status) {
        if (status != null) {
            return stockRequestRepository.findByStatusOrderByRequestedAtAsc(status);
        } else {
            return stockRequestRepository.findAll();
        }
    }

    // (MASTER) 재고 요청 상태 변경 (승인/거절)
    @Transactional
    public StockRequest updateStockRequestStatus(Long requestId, StockRequestUpdateStatusDto updateDto) {
        StockRequest stockRequest = stockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        if (updateDto.getStatus() == RequestStatus.APPROVED) {
            // 승인 시 본사 재고 수량 확인
            for (StockRequestItem item : stockRequest.getItems()) {
                String productCode = item.getProduct().getProductCode();
                int requestedQuantity = item.getQuantity();

                Integer totalAvailableStock = stockRepository.sumQuantityByProduct_ProductCode(productCode)
                        .orElse(0); // 재고가 없으면 0

                if (totalAvailableStock < requestedQuantity) {
                    throw new IllegalArgumentException(
                            "상품 [" + item.getProduct().getProductName() + "]의 본사 재고가 부족합니다. 요청 수량: " + requestedQuantity + ", 현재 재고: " + totalAvailableStock
                    );
                }

                // 본사 재고 차감
                int remainingQuantityToDeduct = requestedQuantity;
                List<com.project.summer.domain.stock.Stock> centralStocks = stockRepository.findByProduct_ProductCodeOrderByExpirationDateAsc(productCode);

                for (com.project.summer.domain.stock.Stock centralStock : centralStocks) {
                    if (remainingQuantityToDeduct <= 0) break;

                    int quantityToDeductFromThisStock = Math.min(remainingQuantityToDeduct, centralStock.getQuantity());

                    centralStock.updateQuantity(centralStock.getQuantity() - quantityToDeductFromThisStock);
                    remainingQuantityToDeduct -= quantityToDeductFromThisStock;

                    // 할당된 재고 상세 정보 기록
                    item.addAllocatedStockDetail(new com.project.summer.domain.stockrequest.AllocatedStockDetail(
                            item, item.getProduct(), quantityToDeductFromThisStock, centralStock.getExpirationDate()
                    ));

                    if (centralStock.getQuantity() <= 0) {
                        stockRepository.delete(centralStock);
                    } else {
                        stockRepository.save(centralStock);
                    }
                }

                if (remainingQuantityToDeduct > 0) {
                    throw new IllegalStateException("본사 재고가 부족하여 요청을 처리할 수 없습니다. (승인 후 재고 부족 발생)");
                }
            }
            stockRequest.approve();
        } else if (updateDto.getStatus() == RequestStatus.REJECTED) {
            if (updateDto.getRejectionReason() == null || updateDto.getRejectionReason().isBlank()) {
                throw new IllegalArgumentException("거절 사유는 필수입니다.");
            }
            stockRequest.reject(updateDto.getRejectionReason());
        }
        return stockRequest;
    }

    // (STORE) 승인된 요청을 확인하고 실제 재고로 등록
    @Transactional
    public void confirmStockRequest(Long requestId, String username) {
        StockRequest stockRequest = stockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));

        // 요청한 사용자와 현재 로그인한 사용자가 같은지 확인
        if (!stockRequest.getUser().getStoreCode().equals(username)) {
            throw new AccessDeniedException("자신의 요청만 처리할 수 있습니다.");
        }

        // 상태가 '승인'된 요청인지 확인
        if (stockRequest.getStatus() != RequestStatus.APPROVED) {
            throw new IllegalStateException("승인된 요청만 처리할 수 있습니다. 현재 상태: " + stockRequest.getStatus());
        }

        // 각 요청 항목을 매장 재고(StoreStock)로 변환하여 저장
        stockRequest.getItems().forEach(item -> {
            // 할당된 재고 상세 정보를 바탕으로 지점 재고 생성
            item.getAllocatedStockDetails().forEach(allocatedDetail -> {
                StoreStock storeStock = StoreStock.builder()
                        .user(stockRequest.getUser())
                        .product(allocatedDetail.getProduct())
                        .quantity(allocatedDetail.getQuantity())
                        .expirationDate(allocatedDetail.getExpirationDate())
                        .build();
                storeStockRepository.save(storeStock);
            });
        });

        // 요청 상태를 '완료'로 변경
        stockRequest.complete();
    }

    // (STORE) 완료된 발주 요청의 일별 입고량 조회
    public Map<String, Integer> getCompletedIncomingQuantitiesByDate(String username) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<StockRequest> completedRequests = stockRequestRepository.findByUser_IdAndStatusOrderByRequestedAtAsc(user.getId(), RequestStatus.COMPLETED);

        Map<String, Integer> dailyIncomingQuantities = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (StockRequest request : completedRequests) {
            String date = request.getRequestedAt().toLocalDate().format(formatter);
            int totalQuantity = request.getItems().stream()
                    .mapToInt(StockRequestItem::getQuantity)
                    .sum();
            dailyIncomingQuantities.merge(date, totalQuantity, Integer::sum);
        }
        return dailyIncomingQuantities;
    }
}
