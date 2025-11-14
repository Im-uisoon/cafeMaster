package com.project.summer.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import javafx.scene.image.Image;
import com.project.summer.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StoreDashboardController implements Controllable {

    @FXML
    private Label storeCodeLabel;
    @FXML
    private VBox contentArea;

    private final ConfigurableApplicationContext context;
    private final JwtTokenProvider jwtTokenProvider;
    private String jwtToken;

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            this.jwtToken = token;
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            storeCodeLabel.setText(auth.getName()); // Display storeCode from token
            showSummaryView(); // Default view for STORE
        }
    }

    @FXML
    private void showSummaryView() {
        loadContent("/com/project/summer/view/store_summary_view.fxml");
    }

    @FXML
    private void logout() {
        // 현재 Stage를 닫고 로그인 화면으로 돌아갑니다.
        Stage stage = (Stage) storeCodeLabel.getScene().getWindow();
        stage.close();

        // 로그인 화면을 다시 띄웁니다.
        try {
            FXMLLoader loader = context.getBean(FXMLLoader.class);
            loader.setLocation(getClass().getResource("/com/project/summer/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1024, 768);
            Stage loginStage = new Stage();
            loginStage.setTitle("Coffee - Login");
            loginStage.getIcons().add(new Image(getClass().getResourceAsStream("/coffee.png")));
            loginStage.setScene(scene);
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStockView() {
        loadContent("/com/project/summer/view/store_stock_view.fxml");
    }

    @FXML
    private void showStockRequest() {
        loadContent("/com/project/summer/view/stock_request_view.fxml");
    }

    @FXML
    private void showDiscardManagement() {
        loadContent("/com/project/summer/view/store_discard_management.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = context.getBean(FXMLLoader.class);
            loader.setLocation(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof Controllable controllable) {
                controllable.initData(jwtToken);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
