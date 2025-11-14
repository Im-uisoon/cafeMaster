package com.project.summer.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StoreDashboardViewController {

    @FXML
    private AnchorPane contentPane;

    private final ApplicationContext applicationContext;

    @FXML
    public void initialize() {
        // 초기 화면으로 매장 요약 뷰를 로드합니다.
        showSummaryView(null);
    }

    @FXML
    void showSummaryView(ActionEvent event) {
        loadView("/com/project/summer/view/store_summary_view.fxml");
    }

    @FXML
    void showStockView(ActionEvent event) {
        loadView("/com/project/summer/view/store_stock_view.fxml");
    }

    @FXML
    void showStockRequestView(ActionEvent event) {
        loadView("/com/project/summer/view/stock_request_view.fxml");
    }

    @FXML
    void showDiscardManagementView(ActionEvent event) {
        loadView("/com/project/summer/view/store_discard_management.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
