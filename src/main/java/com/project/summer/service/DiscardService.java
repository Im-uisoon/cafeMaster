package com.project.summer.service;

import com.project.summer.domain.discard.DiscardItem;
import com.project.summer.domain.discard.DiscardRecord;
import com.project.summer.domain.discard.DiscardType;
import com.project.summer.domain.dto.discard.DailyDiscardRecordResponseDto;
import com.project.summer.domain.dto.discard.DiscardItemResponseDto;
import com.project.summer.domain.dto.discard.ManualDiscardRequestDto;
import com.project.summer.domain.dto.discard.MonthlyDiscardSummaryResponseDto;
import com.project.summer.domain.storestock.StoreStock;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.DiscardItemRepository;
import com.project.summer.repository.DiscardRecordRepository;
import com.project.summer.repository.StoreStockRepository;
import com.project.summer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscardService {

    private final DiscardRecordRepository discardRecordRepository;
    private final DiscardItemRepository discardItemRepository;
    private final StoreStockRepository storeStockRepository;
    private final UserRepository userRepository;

    // (스케줄러) 유통기한 만료 재고 자동 폐기
    @Transactional
    public void discardExpiredStocks() {
        LocalDate today = LocalDate.now();
        List<StoreStock> expiredStocks = storeStockRepository.findByExpirationDateBefore(today.plusDays(1));

        if (expiredStocks.isEmpty()) {
            System.out.println(today + " 날짜로 만료된 지점 재고가 없습니다.");
            return;
        }

        // 매장별로 그룹화하여 처리
        expiredStocks.stream()
                .collect(Collectors.groupingBy(StoreStock::getUser))
                .forEach((user, stocks) -> {
                    processDiscard(user, stocks, DiscardType.AUTOMATIC, "기한만료");
                });

        System.out.println(today + " 날짜로 만료된 지점 재고 " + expiredStocks.size() + "개가 폐기되었습니다.");
    }

    // (STORE) 수동 폐기
    @Transactional
    public void manualDiscard(ManualDiscardRequestDto requestDto, String username) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        StoreStock storeStock = storeStockRepository.findById(requestDto.getStoreStockId())
                .orElseThrow(() -> new IllegalArgumentException("해당 재고를 찾을 수 없습니다."));

        // 자신의 매장 재고인지 확인
        if (!storeStock.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("자신의 매장 재고만 폐기할 수 있습니다.");
        }

        // 요청 수량이 현재 재고 수량보다 많은지 확인
        if (requestDto.getQuantity() > storeStock.getQuantity()) {
            throw new IllegalArgumentException("폐기 수량이 현재 재고 수량보다 많습니다.");
        }

        // 폐기 처리
        processDiscard(user, List.of(storeStock), DiscardType.MANUAL, requestDto.getDiscardReason(), requestDto.getQuantity());
    }

    // 폐기 처리 공통 로직
    private void processDiscard(UserEntity user, List<StoreStock> stocksToDiscard, DiscardType discardType, String discardReason) {
        processDiscard(user, stocksToDiscard, discardType, discardReason, -1); // -1은 전체 수량 폐기 의미
    }

    private void processDiscard(UserEntity user, List<StoreStock> stocksToDiscard, DiscardType discardType, String discardReason, int manualDiscardQuantity) {
        LocalDate today = LocalDate.now();

        // 해당 매장의 오늘 날짜 폐기 기록을 찾거나 새로 생성
        DiscardRecord discardRecord = discardRecordRepository.findByUserAndDiscardDate(user, today)
                .orElseGet(() -> DiscardRecord.builder()
                        .user(user)
                        .discardDate(today)
                        .totalDiscardAmount(0)
                        .totalDiscardProductCount(0)
                        .build());

        Set<Long> discardedProductIds = discardRecord.getDiscardItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        for (StoreStock stock : stocksToDiscard) {
            int actualDiscardQuantity = (manualDiscardQuantity != -1) ? manualDiscardQuantity : stock.getQuantity();

            // DiscardItem 생성
            DiscardItem discardItem = DiscardItem.builder()
                    .discardRecord(discardRecord)
                    .product(stock.getProduct())
                    .quantity(actualDiscardQuantity)
                    .expirationDate(stock.getExpirationDate())
                    .discardType(discardType)
                    .discardReason(discardReason)
                    .storeStockId(stock.getId())
                    .build();
            discardRecord.addDiscardItem(discardItem);

            // 총 폐기 금액 업데이트
            discardRecord.updateTotalDiscardAmount(stock.getProduct().getPrice() * actualDiscardQuantity);

            // 총 폐기 상품 종류 개수 업데이트
            if (!discardedProductIds.contains(stock.getProduct().getId())) {
                discardRecord.updateTotalDiscardProductCount(1);
                discardedProductIds.add(stock.getProduct().getId());
            }

            // StoreStock에서 수량 차감 또는 삭제
            if (manualDiscardQuantity != -1) { // 수동 폐기 시 부분 폐기 가능
                stock.update(stock.getQuantity() - actualDiscardQuantity);
                if (stock.getQuantity() <= 0) {
                    storeStockRepository.delete(stock);
                } else {
                    storeStockRepository.save(stock);
                }
            } else { // 자동 폐기 시 전체 폐기
                storeStockRepository.delete(stock);
            }
        }
        discardRecordRepository.save(discardRecord);
    }

    // (STORE/MASTER) 일일 폐기 내역 조회
    public DailyDiscardRecordResponseDto getDailyDiscardRecords(LocalDate date, String username, String authority) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DiscardRecord discardRecord;
        if (authority.equals("ROLE_MASTER")) {
            // MASTER는 모든 매장의 해당 날짜 기록 중 하나를 선택 (여기서는 첫 번째)
            discardRecord = discardRecordRepository.findByDiscardDate(date).stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(date + " 날짜의 폐기 기록을 찾을 수 없습니다."));
        } else {
            // STORE는 자신의 매장 기록만 조회
            discardRecord = discardRecordRepository.findByUserAndDiscardDate(user, date)
                    .orElseThrow(() -> new IllegalArgumentException(date + " 날짜의 폐기 기록을 찾을 수 없습니다."));
        }
        return new DailyDiscardRecordResponseDto(discardRecord);
    }

    // (STORE/MASTER) 월별 폐기 요약 조회
    public List<MonthlyDiscardSummaryResponseDto> getMonthlyDiscardSummary(int year, int month, String username, String authority) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<DiscardRecord> records;
        if (authority.equals("ROLE_MASTER")) {
            // MASTER는 모든 매장의 해당 월 기록 조회
            records = discardRecordRepository.findByDiscardDateBetweenOrderByDiscardDateAsc(startDate, endDate);
        } else {
            // STORE는 자신의 매장 기록만 조회
            records = discardRecordRepository.findByUserAndDiscardDateBetweenOrderByDiscardDateAsc(user, startDate, endDate);
        }

        return records.stream()
                .map(MonthlyDiscardSummaryResponseDto::new)
                .collect(Collectors.toList());
    }

    // (STORE/MASTER) 월별 폐기 항목 조회
    public List<DiscardItemResponseDto> getMonthlyDiscardItems(int year, int month, String username, String authority) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.of(year, month, startDate.lengthOfMonth());

        List<DiscardRecord> records;
        if (authority.equals("ROLE_MASTER")) {
            records = discardRecordRepository.findByDiscardDateBetweenOrderByDiscardDateAsc(startDate, endDate);
        } else {
            records = discardRecordRepository.findByUserAndDiscardDateBetweenOrderByDiscardDateAsc(user, startDate, endDate);
        }

        return records.stream()
                .flatMap(record -> record.getDiscardItems().stream())
                .map(DiscardItemResponseDto::new)
                .collect(Collectors.toList());
    }

    // (STORE) 일별 폐기량 조회
    public Map<String, Integer> getDailyDiscardQuantitiesByDate(String username) {
        UserEntity user = userRepository.findByStoreCode(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<DiscardRecord> discardRecords = discardRecordRepository.findByUserOrderByDiscardDateAsc(user);

        Map<String, Integer> dailyDiscardQuantities = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (DiscardRecord record : discardRecords) {
            String date = record.getDiscardDate().format(formatter);
            int totalQuantity = record.getDiscardItems().stream()
                    .mapToInt(DiscardItem::getQuantity)
                    .sum();
            dailyDiscardQuantities.merge(date, totalQuantity, Integer::sum);
        }
        return dailyDiscardQuantities;
    }
}
