package com.project.summer.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.summer.domain.dto.discard.DailyDiscardRecordResponseDto;
import com.project.summer.domain.dto.discard.DiscardItemResponseDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StoreDiscardManagementController implements Controllable {

    @FXML
    private TableView<DiscardItemResponseDto> discardTableView;
    @FXML
    private TableColumn<DiscardItemResponseDto, Long> idColumn;
    @FXML
    private TableColumn<DiscardItemResponseDto, String> productCodeColumn;
    @FXML
    private TableColumn<DiscardItemResponseDto, String> productNameColumn;
    @FXML
    private TableColumn<DiscardItemResponseDto, Integer> quantityColumn;
    @FXML
    private TableColumn<DiscardItemResponseDto, String> discardReasonColumn;
    @FXML
    private TableColumn<DiscardItemResponseDto, LocalDate> discardDateColumn;

    @FXML
    private TextField productCodeField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField expirationDateField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField discardReasonField;
    @FXML
    private TextField amountField;

    private final ApiClient apiClient;
    private final ObservableList<DiscardItemResponseDto> discardList = FXCollections.observableArrayList();

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadDiscardListDaily(); // 기본적으로 일별 폐기 리스트 로드
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        productCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        productNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        quantityColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));
        discardReasonColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDiscardReason()));
        discardDateColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getDiscardDate()));

        discardTableView.setItems(discardList);
        discardTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDiscardDetails(newValue));
    }

    private void loadDiscardListDaily() {
        LocalDate today = LocalDate.now();
        String dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            DailyDiscardRecordResponseDto response = apiClient.get("http://localhost:8080/api/discard/daily?date=" + dateString, DailyDiscardRecordResponseDto.class);
            discardList.setAll(response.getDiscardItems());
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "일별 폐기 기록을 불러오는 데 실패했습니다.");
        }
    }

    private void loadDiscardListMonthly() {
        LocalDate today = LocalDate.now();
        String year = String.valueOf(today.getYear());
        String month = String.valueOf(today.getMonthValue());
        try {
            List<DiscardItemResponseDto> records = apiClient.get("http://localhost:8080/api/discard/monthly?year=" + year + "&month=" + month, new TypeReference<List<DiscardItemResponseDto>>() {});
            discardList.setAll(records);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "월별 폐기 기록을 불러오는 데 실패했습니다.");
        }
    }

    private void showDiscardDetails(DiscardItemResponseDto record) {
        if (record != null) {
            productCodeField.setText(record.getProductCode());
            productNameField.setText(record.getProductName());
            expirationDateField.setText(record.getExpirationDate() != null ? record.getExpirationDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
            quantityField.setText(String.valueOf(record.getQuantity()));
            discardReasonField.setText(record.getDiscardReason());
            amountField.setText(String.valueOf(record.getAmount()));
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        discardTableView.getSelectionModel().clearSelection();
        productCodeField.clear();
        productNameField.clear();
        expirationDateField.clear();
        quantityField.clear();
        discardReasonField.clear();
        amountField.clear();
    }

    @FXML
    private void viewDaily() {
        loadDiscardListDaily();
    }

    @FXML
    private void viewMonthly() {
        loadDiscardListMonthly();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
