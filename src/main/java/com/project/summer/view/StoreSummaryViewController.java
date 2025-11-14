package com.project.summer.view;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.project.summer.service.StockRequestService;
import lombok.RequiredArgsConstructor;
import com.project.summer.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import com.project.summer.service.DiscardService;
import com.project.summer.domain.storestock.StoreStockService;
import com.project.summer.repository.UserRepository;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.domain.dailystock.DailyStockRecord;

@Component
@RequiredArgsConstructor
public class StoreSummaryViewController implements Controllable {

    private final StockRequestService stockRequestService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscardService discardService;
    private final StoreStockService storeStockService;
    private final UserRepository userRepository;

    @FXML
    private LineChart<String, Number> stockTrendChart;

    private String storeCode;

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            this.storeCode = auth.getName();
            loadChartData(this.storeCode);
        }
    }

    @FXML
    public void initialize() {
        // 그래프의 기본 설정만 담당합니다.
        stockTrendChart.getData().clear(); // 기존 데이터 초기화

        XYChart.Series<String, Number> incomingSeries = new XYChart.Series<>();
        incomingSeries.setName("일별 입고량");

        XYChart.Series<String, Number> discardSeries = new XYChart.Series<>();
        discardSeries.setName("일별 폐기량");

        XYChart.Series<String, Number> totalStockSeries = new XYChart.Series<>();
        totalStockSeries.setName("현재 보유 재고");

        stockTrendChart.getData().add(incomingSeries);
        stockTrendChart.getData().add(discardSeries);
        stockTrendChart.getData().add(totalStockSeries);
    }

    public void loadChartData(String storeCode) {
        // 오늘 날짜를 기준으로 최근 5일의 날짜를 생성합니다.
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 기존 데이터 초기화 (initialize에서 이미 추가했으므로 clear만)
        ((XYChart.Series<String, Number>) stockTrendChart.getData().get(0)).getData().clear();
        ((XYChart.Series<String, Number>) stockTrendChart.getData().get(1)).getData().clear();
        ((XYChart.Series<String, Number>) stockTrendChart.getData().get(2)).getData().clear();

        Map<String, Integer> completedIncomingQuantities = new HashMap<>();
        Map<String, Integer> dailyDiscardQuantities = new HashMap<>();
        Map<String, Integer> dailyStockQuantities = new HashMap<>();
        if (storeCode != null) {
            completedIncomingQuantities = stockRequestService.getCompletedIncomingQuantitiesByDate(storeCode);
            dailyDiscardQuantities = discardService.getDailyDiscardQuantitiesByDate(storeCode);

            // 과거 5일치 재고 기록 가져오기
            LocalDate fiveDaysAgo = today.minusDays(4);
            UserEntity currentUser = userRepository.findByStoreCode(storeCode)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            List<DailyStockRecord> records = storeStockService.getDailyStockRecords(currentUser, fiveDaysAgo, today.minusDays(1));
            for (DailyStockRecord record : records) {
                dailyStockQuantities.put(record.getRecordDate().format(formatter), record.getTotalQuantity());
            }

            // 오늘자 실시간 재고량 가져오기
            int currentTotalStock = storeStockService.calculateTotalStockQuantity(currentUser);
            dailyStockQuantities.put(today.format(formatter), currentTotalStock);
        }

        for (int i = 4; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String formattedDate = date.format(formatter);

            // 일별 입고량 데이터 설정 (데이터가 없으면 0으로 처리)
            int incomingQuantity = completedIncomingQuantities.getOrDefault(formattedDate, 0);
            ((XYChart.Series<String, Number>) stockTrendChart.getData().get(0)).getData().add(new XYChart.Data<>(formattedDate, incomingQuantity));

            // 일별 폐기량 데이터 설정 (데이터가 없으면 0으로 처리)
            int discardQuantity = dailyDiscardQuantities.getOrDefault(formattedDate, 0);
            ((XYChart.Series<String, Number>) stockTrendChart.getData().get(1)).getData().add(new XYChart.Data<>(formattedDate, discardQuantity));

            // 일별 보유 재고 데이터 설정 (데이터가 없으면 0으로 처리)
            int totalStockQuantity = dailyStockQuantities.getOrDefault(formattedDate, 0);
            ((XYChart.Series<String, Number>) stockTrendChart.getData().get(2)).getData().add(new XYChart.Data<>(formattedDate, totalStockQuantity));
        }
    }
}
