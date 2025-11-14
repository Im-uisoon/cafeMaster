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

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MasterDashboardController implements Controllable {

    @FXML
    private Label storeCodeLabel;
    @FXML
    private VBox contentArea;

    private final ConfigurableApplicationContext context;
    private String jwtToken;

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            this.jwtToken = token;
            // TODO: Parse token to get storeCode
            storeCodeLabel.setText("MASTER"); // Placeholder
            showStoreManagement();
        }
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
    private void showStoreManagement() {
        loadContent("/com/project/summer/view/store_management.fxml");
    }

    @FXML
    private void showStockManagement() {
        loadContent("/com/project/summer/view/stock_management.fxml");
    }

    @FXML
    private void showRequestManagement() {
        loadContent("/com/project/summer/view/request_management.fxml");
    }

    @FXML
    private void showProductManagement() {
        loadContent("/com/project/summer/view/product_management.fxml");
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