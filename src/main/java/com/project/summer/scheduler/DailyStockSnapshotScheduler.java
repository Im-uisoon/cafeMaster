package com.project.summer.scheduler;

import com.project.summer.domain.storestock.StoreStockService;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyStockSnapshotScheduler {

    private final UserRepository userRepository;
    private final StoreStockService storeStockService;

    // 매일 자정 (0시 0분 0초)에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void recordDailyStockSnapshots() {
        System.out.println("Daily stock snapshot recording started.");
        List<UserEntity> allStores = userRepository.findAllByRole("ROLE_STORE"); // 모든 STORE 권한 사용자 조회

        for (UserEntity user : allStores) {
            try {
                storeStockService.recordDailyStockSnapshot(user);
                System.out.println("Recorded daily stock snapshot for store: " + user.getStoreCode());
            } catch (Exception e) {
                System.err.println("Error recording daily stock snapshot for store " + user.getStoreCode() + ": " + e.getMessage());
            }
        }
        System.out.println("Daily stock snapshot recording finished.");
    }
}
