package com.project.summer.domain.storestock;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.storestock.dto.StoreStockCreateRequestDto;
import com.project.summer.domain.storestock.dto.StoreStockResponseDto;
import com.project.summer.domain.storestock.dto.StoreStockUpdateRequestDto;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.ProductRepository;
import com.project.summer.repository.StoreStockRepository;
import com.project.summer.repository.UserRepository;
import com.project.summer.repository.DailyStockRecordRepository;
import com.project.summer.domain.dailystock.DailyStockRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreStockService {

    private final StoreStockRepository storeStockRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DailyStockRecordRepository dailyStockRecordRepository;

    // 재고 생성
    @Transactional
    public StoreStockResponseDto createStoreStock(UserEntity user, Product product, int quantity, java.time.LocalDate expirationDate) {
        // 이미 해당 매장에 동일한 상품 재고가 있는지 확인 (이 로직은 StockRequestService에서 이미 처리되므로 여기서는 제거)
        // if (storeStockRepository.existsByUser_StoreCodeAndProduct_ProductCode(user.getStoreCode(), product.getProductCode())) {
        //     throw new IllegalArgumentException("이미 해당 상품의 재고가 존재합니다. 수정을 이용해주세요.");
        // }

        StoreStock storeStock = StoreStock.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .expirationDate(expirationDate)
                .build();

        StoreStock savedStock = storeStockRepository.save(storeStock);
        return StoreStockResponseDto.from(savedStock);
    }

    // 재고 조회 (권한에 따라 분기)
    public List<StoreStockResponseDto> getStoreStocks(String storeCode, String authority) {
        if (authority.equals("ROLE_MASTER")) {
            // MASTER는 모든 재고 조회
            return storeStockRepository.findAll().stream()
                    .map(StoreStockResponseDto::from)
                    .collect(Collectors.toList());
        } else {
            // STORE는 자기 매장 재고만 조회
            return storeStockRepository.findByUser_StoreCode(storeCode).stream()
                    .map(StoreStockResponseDto::from)
                    .collect(Collectors.toList());
        }
    }

    // 재고 수정
    @Transactional
    public StoreStockResponseDto updateStoreStock(Long stockId, StoreStockUpdateRequestDto requestDto, String storeCode, String authority) {
        StoreStock stock = storeStockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재고를 찾을 수 없습니다."));

        // STORE 권한일 경우, 자신의 재고가 맞는지 확인
        if (authority.equals("ROLE_STORE") && !stock.getUser().getStoreCode().equals(storeCode)) {
            throw new AccessDeniedException("자신의 매장 재고만 수정할 수 있습니다.");
        }

        stock.update(requestDto.getQuantity());
        return StoreStockResponseDto.from(stock);
    }

    // 재고 삭제
    @Transactional
    public void deleteStoreStock(Long stockId, String storeCode, String authority) {
        StoreStock stock = storeStockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("해당 재고를 찾을 수 없습니다."));

        // STORE 권한일 경우, 자신의 재고가 맞는지 확인
        if (authority.equals("ROLE_STORE") && !stock.getUser().getStoreCode().equals(storeCode)) {
            throw new AccessDeniedException("자신의 매장 재고만 삭제할 수 있습니다.");
        }

        storeStockRepository.delete(stock);
    }

    // 특정 매장의 현재 총 재고량 계산
    public int calculateTotalStockQuantity(UserEntity user) {
        return storeStockRepository.findByUser(user).stream()
                .mapToInt(StoreStock::getQuantity)
                .sum();
    }

    // 일일 재고 스냅샷 기록
    @Transactional
    public void recordDailyStockSnapshot(UserEntity user) {
        LocalDate today = LocalDate.now();
        // 이미 오늘 기록이 있는지 확인
        Optional<DailyStockRecord> existingRecord = dailyStockRecordRepository.findByUserAndRecordDate(user, today);

        int totalQuantity = calculateTotalStockQuantity(user);

        if (existingRecord.isPresent()) {
            // 기존 기록 업데이트
            DailyStockRecord record = existingRecord.get();
            // Lombok @Setter가 없으므로, 엔티티에 업데이트 메서드를 추가하거나 새로 생성해야 함
            // 여기서는 간단히 새로운 기록으로 대체하는 방식으로 처리 (실제 프로젝트에서는 업데이트 메서드 권장)
            dailyStockRecordRepository.delete(record);
            dailyStockRecordRepository.save(DailyStockRecord.builder()
                    .user(user)
                    .recordDate(today)
                    .totalQuantity(totalQuantity)
                    .build());
        } else {
            // 새 기록 생성
            dailyStockRecordRepository.save(DailyStockRecord.builder()
                    .user(user)
                    .recordDate(today)
                    .totalQuantity(totalQuantity)
                    .build());
        }
    }

    // 특정 매장의 특정 기간 동안의 일일 재고 기록 조회
    public List<DailyStockRecord> getDailyStockRecords(UserEntity user, LocalDate startDate, LocalDate endDate) {
        return dailyStockRecordRepository.findByUserAndRecordDateBetweenOrderByRecordDateAsc(user, startDate, endDate);
    }
}
