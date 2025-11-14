package com.project.summer.view;

import com.project.summer.domain.storestock.dto.StoreStockResponseDto;
import com.project.summer.domain.storestock.dto.StoreStockUpdateRequestDto;
import com.project.summer.domain.dto.discard.DiscardRequestDto;
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

@Component
@RequiredArgsConstructor
public class StoreStockViewController implements Controllable {

    @FXML
    private TableView<StoreStockResponseDto> storeStockTableView;
    @FXML
    private TableColumn<StoreStockResponseDto, Long> idColumn;
    @FXML
    private TableColumn<StoreStockResponseDto, String> productCodeColumn;
    @FXML
    private TableColumn<StoreStockResponseDto, String> productNameColumn;
    @FXML
    private TableColumn<StoreStockResponseDto, LocalDate> expirationDateColumn;
    @FXML
    private TableColumn<StoreStockResponseDto, Integer> quantityColumn;

    @FXML
    private TextField productCodeField;
    @FXML
    private TextField productNameField;
    @FXML
    private DatePicker expirationDateField;
    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> discardReasonField;

    private final ApiClient apiClient;
    private final ObservableList<StoreStockResponseDto> storeStockList = FXCollections.observableArrayList();

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadStoreStockList();
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        productCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        productNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        expirationDateColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getExpirationDate()));
        quantityColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        storeStockTableView.setItems(storeStockList);
        storeStockTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showStoreStockDetails(newValue));

        // 상품 코드, 상품명, 유통기한은 수정 불가능하도록 설정
        productCodeField.setEditable(false);
        productNameField.setEditable(false);
        expirationDateField.setEditable(false);

        discardReasonField.setItems(FXCollections.observableArrayList("파손", "사용불가", "기타"));
    }

    private void loadStoreStockList() {
        try {
            StoreStockResponseDto[] stocks = apiClient.get("http://localhost:8080/api/store/stocks", StoreStockResponseDto[].class);
            storeStockList.setAll(stocks);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "지점 재고 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void showStoreStockDetails(StoreStockResponseDto stock) {
        if (stock != null) {
            productCodeField.setText(stock.getProductCode());
            productNameField.setText(stock.getProductName());
            expirationDateField.setValue(stock.getExpirationDate());
            quantityField.setText(String.valueOf(stock.getQuantity()));
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        storeStockTableView.getSelectionModel().clearSelection();
        productCodeField.clear();
        productNameField.clear();
        expirationDateField.setValue(null);
        quantityField.clear();
        discardReasonField.setValue(null);
    }

    @FXML
    private void updateStock() {
        StoreStockResponseDto selectedStock = storeStockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("알림", "수정할 재고를 선택해주세요.");
            return;
        }
        try {
            StoreStockUpdateRequestDto updatedStock = new StoreStockUpdateRequestDto(
                    Integer.parseInt(quantityField.getText())
            );
            apiClient.put("http://localhost:8080/api/store/stocks/" + selectedStock.getId(), updatedStock, Void.class);
            loadStoreStockList();
            resetForm();
            showAlert("성공", "재고가 성공적으로 수정되었습니다.");
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "개수는 숫자로 입력해주세요.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 수정에 실패했습니다.");
        }
    }

    @FXML
    private void discardStock() {
        StoreStockResponseDto selectedStock = storeStockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("알림", "폐기할 재고를 선택해주세요.");
            return;
        }

        String quantityText = quantityField.getText();
        String discardReason = discardReasonField.getValue();

        if (quantityText.isEmpty() || discardReason == null || discardReason.isEmpty()) {
            showAlert("입력 오류", "폐기할 개수와 폐기 사유를 선택해주세요.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0 || quantity > selectedStock.getQuantity()) {
                showAlert("입력 오류", "폐기할 개수는 1개 이상이어야 하며, 현재 재고 수량보다 많을 수 없습니다.");
                return;
            }

            DiscardRequestDto discardRequest = new DiscardRequestDto(
                    selectedStock.getId(),
                    quantity,
                    discardReason
            );

            apiClient.post("http://localhost:8080/api/store/discard", discardRequest, Void.class);
            loadStoreStockList();
            resetForm();
            showAlert("성공", "재고가 성공적으로 폐기되었습니다.");
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "개수는 숫자로 입력해주세요.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 폐기에 실패했습니다.");
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
