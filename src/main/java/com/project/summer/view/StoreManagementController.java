package com.project.summer.view;

import com.project.summer.domain.auth.dto.RegisterRequestDto;
import com.project.summer.domain.auth.dto.StoreUpdateRequestDto;
import com.project.summer.domain.user.UserEntity;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreManagementController implements Controllable {

    @FXML
    private TableView<UserEntity> storeTableView;
    @FXML
    private TableColumn<UserEntity, Long> idColumn;
    @FXML
    private TableColumn<UserEntity, String> storeCodeColumn;
    @FXML
    private TableColumn<UserEntity, String> storeNameColumn;
    @FXML
    private TextField storeCodeField;
    @FXML
    private TextField storeNameField;
    @FXML
    private PasswordField passwordField;

    private final ApiClient apiClient;
    private final ObservableList<UserEntity> storeList = FXCollections.observableArrayList();

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadStoreList();
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        storeCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStoreCode()));
        storeNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStoreName()));

        storeTableView.setItems(storeList);
        storeTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showStoreDetails(newValue));
    }

    private void loadStoreList() {
        try {
            UserEntity[] stores = apiClient.get("http://localhost:8080/api/master/stores", UserEntity[].class);
            storeList.setAll(stores);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "매장 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void showStoreDetails(UserEntity user) {
        if (user != null) {
            storeCodeField.setText(user.getStoreCode());
            storeNameField.setText(user.getStoreName());
            passwordField.clear();
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        storeTableView.getSelectionModel().clearSelection();
        storeCodeField.clear();
        storeNameField.clear();
        passwordField.clear();
    }

    @FXML
    private void addStore() {
        try {
            RegisterRequestDto newStore = new RegisterRequestDto(
                    storeCodeField.getText(),
                    storeNameField.getText(),
                    passwordField.getText()
            );
            apiClient.post("http://localhost:8080/api/auth/register", newStore, UserEntity.class);
            loadStoreList();
            resetForm();
            showAlert("성공", "매장이 성공적으로 추가되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "매장 추가에 실패했습니다.");
        }
    }

    @FXML
    private void updateStore() {
        UserEntity selectedStore = storeTableView.getSelectionModel().getSelectedItem();
        if (selectedStore == null) {
            showAlert("알림", "수정할 매장을 선택해주세요.");
            return;
        }
        try {
            StoreUpdateRequestDto updatedStore = new StoreUpdateRequestDto(
                    storeCodeField.getText(),
                    storeNameField.getText(),
                    passwordField.getText()
            );
            apiClient.put("http://localhost:8080/api/master/stores/" + selectedStore.getId(), updatedStore, UserEntity.class);
            loadStoreList();
            resetForm();
            showAlert("성공", "매장이 성공적으로 수정되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "매장 수정에 실패했습니다.");
        }
    }

    @FXML
    private void deleteStore() {
        UserEntity selectedStore = storeTableView.getSelectionModel().getSelectedItem();
        if (selectedStore == null) {
            showAlert("알림", "삭제할 매장을 선택해주세요.");
            return;
        }
        try {
            apiClient.delete("http://localhost:8080/api/master/stores/" + selectedStore.getId());
            loadStoreList();
            resetForm();
            showAlert("성공", "매장이 성공적으로 삭제되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "매장 삭제에 실패했습니다.");
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
