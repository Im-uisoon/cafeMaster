package com.project.summer.view;

import com.project.summer.domain.auth.UserService;
import com.project.summer.domain.auth.dto.LoginRequestDto;
import com.project.summer.domain.auth.dto.TokenResponseDto;
import com.project.summer.security.JwtTokenProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import javafx.application.Platform;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginController {

    @FXML
    private TextField storeCodeField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;

    private final UserService userService;
    private final SceneManager sceneManager;
    private final JwtTokenProvider jwtTokenProvider;

    @FXML
    public void login() {
        String storeCode = storeCodeField.getText();
        String password = passwordField.getText();

        if (storeCode.isEmpty() || password.isEmpty()) {
            showAlert("로그인 오류", "매장 코드와 비밀번호를 모두 입력해주세요.");
            return;
        }

        try {
            TokenResponseDto tokenResponse = userService.login(new LoginRequestDto(storeCode, password));
            String token = tokenResponse.getAccessToken();

            Authentication auth = jwtTokenProvider.getAuthentication(token);
            boolean isMaster = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_MASTER"));

            if (isMaster) {
                Platform.runLater(() -> {
                    try {
                        // 현재 Stage를 가져와서 SceneManager에 전달
                        Stage currentStage = (Stage) loginBtn.getScene().getWindow();
                        sceneManager.switchScene(currentStage, "/com/project/summer/view/master_dashboard.fxml", token);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("오류", "화면을 전환하는 데 실패했습니다.");
                    }
                });
            } else {
                Platform.runLater(() -> {
                    try {
                        Stage currentStage = (Stage) loginBtn.getScene().getWindow();
                        sceneManager.switchScene(currentStage, "/com/project/summer/view/store_dashboard.fxml", token);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("오류", "화면을 전환하는 데 실패했습니다.");
                    }
                });
            }

        } catch (Exception e) {
            showAlert("로그인 실패", "매장 코드 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
