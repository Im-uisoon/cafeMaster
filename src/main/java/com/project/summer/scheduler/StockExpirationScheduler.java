package com.project.summer.scheduler;

import com.project.summer.domain.stock.StockService;
import com.project.summer.service.DiscardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockExpirationScheduler {

    private final StockService stockService;
    private final DiscardService discardService;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void deleteExpiredStocksDaily() {
        System.out.println("본사 만료된 재고 삭제 스케줄러 실행...");
        stockService.deleteExpiredStocks();

        System.out.println("지점 만료된 재고 폐기 스케줄러 실행...");
        discardService.discardExpiredStocks();
    }

//    @Scheduled(fixedRate = 60000) // 1분 (60000 밀리초)마다 실행
//    public void deleteExpiredStocksTest() {
//         System.out.println("테스트: 본사 만료된 재고 삭제 스케줄러 실행 (1분 주기)...");
//         stockService.deleteExpiredStocks();
//         System.out.println("테스트: 지점 만료된 재고 폐기 스케줄러 실행 (1분 주기)...");
//         discardService.discardExpiredStocks();
//    }
}

