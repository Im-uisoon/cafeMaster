package com.project.summer.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.summer.domain.dto.stockrequest.StockRequestItemResponseDto;
import com.project.summer.domain.dto.stockrequest.StockRequestResponseDto;
import com.project.summer.domain.dto.stockrequest.StockRequestUpdateStatusDto;
import com.project.summer.domain.stockrequest.RequestStatus;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestManagementController implements Controllable {

    @FXML
    private TableView<StockRequestResponseDto> requestTableView;
    @FXML
    private TableColumn<StockRequestResponseDto, Long> idColumn;
    @FXML
    private TableColumn<StockRequestResponseDto, String> storeNameColumn;
    @FXML
    private TableColumn<StockRequestResponseDto, RequestStatus> statusColumn;

    @FXML
    private TextField storeNameField;
    @FXML
    private TextField requestedDateField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField statusField;
    @FXML
    private ComboBox<String> rejectionReasonComboBox;
    @FXML
    private TextArea customRejectionReasonArea;

    private final ApiClient apiClient;
    private final ObservableList<StockRequestResponseDto> requestList = FXCollections.observableArrayList();

    private final String[] REJECTION_REASONS = {"재고 부족", "판매 중단 상품", "기타"};

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadRequests("PENDING"); // 초기에는 PENDING 요청만 로드
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        storeNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStoreName()));
        statusColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStatus()));

        requestTableView.setItems(requestList);
        requestTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showRequestDetails(newValue));

        // 거절 사유 ComboBox 초기화
        rejectionReasonComboBox.setItems(FXCollections.observableArrayList(REJECTION_REASONS));
        rejectionReasonComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            customRejectionReasonArea.setVisible("기타".equals(newVal));
            customRejectionReasonArea.setManaged("기타".equals(newVal));
        });

        // 거절 사유 입력 필드 초기 비활성화
        rejectionReasonComboBox.setDisable(true);
        customRejectionReasonArea.setDisable(true);
    }

    private void loadRequests(String status) {
        try {
            String url = "http://localhost:8080/api/master/stock-requests";
            if (status != null && !status.isEmpty()) {
                url += "?status=" + status;
            }
            List<StockRequestResponseDto> requests = apiClient.get(url, new TypeReference<List<StockRequestResponseDto>>() {});
            requestList.setAll(requests);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "요청 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void showRequestDetails(StockRequestResponseDto request) {
        if (request != null) {
            storeNameField.setText(request.getStoreName());
            requestedDateField.setText(request.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            // 요청 상품 목록을 문자열로 변환하여 표시
            String itemsText = request.getItems().stream()
                    .map(item -> item.getProductName() + " (" + item.getQuantity() + "개)")
                    .collect(Collectors.joining(", "));
            productNameField.setText(itemsText);
            quantityField.setText(String.valueOf(request.getItems().stream().mapToInt(StockRequestItemResponseDto::getQuantity).sum())); // 총 개수
            statusField.setText(request.getStatus().toString());
            if (request.getRejectionReason() != null && !request.getRejectionReason().isEmpty()) {
                // 기존 거절 사유가 미리 정의된 사유 중 하나인지 확인
                boolean foundPredefined = false;
                for (String reason : REJECTION_REASONS) {
                    if (reason.equals(request.getRejectionReason())) {
                        rejectionReasonComboBox.setValue(reason);
                        customRejectionReasonArea.clear();
                        foundPredefined = true;
                        break;
                    }
                }
                if (!foundPredefined) {
                    rejectionReasonComboBox.setValue("기타");
                    customRejectionReasonArea.setText(request.getRejectionReason());
                }
            } else {
                rejectionReasonComboBox.setValue(null);
                customRejectionReasonArea.clear();
            }

            // 거절 상태일 때만 거절 사유 필드 활성화
            rejectionReasonComboBox.setDisable(request.getStatus() != RequestStatus.PENDING);
            customRejectionReasonArea.setDisable(request.getStatus() != RequestStatus.PENDING || !"기타".equals(rejectionReasonComboBox.getValue()));
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        requestTableView.getSelectionModel().clearSelection();
        storeNameField.clear();
        requestedDateField.clear();
        productNameField.clear();
        quantityField.clear();
        statusField.clear();
        rejectionReasonComboBox.setValue(null);
        customRejectionReasonArea.clear();
        rejectionReasonComboBox.setDisable(true);
        customRejectionReasonArea.setDisable(true);
    }

    @FXML
    private void showPendingRequests() {
        loadRequests("PENDING");
        resetForm();
    }

    @FXML
    private void approveRequest() {
        StockRequestResponseDto selectedRequest = requestTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("알림", "승인할 요청을 선택해주세요.");
            return;
        }
        if (selectedRequest.getStatus() != RequestStatus.PENDING) {
            showAlert("알림", "대기 중인 요청만 승인할 수 있습니다.");
            return;
        }
        try {
            StockRequestUpdateStatusDto updateDto = new StockRequestUpdateStatusDto(RequestStatus.APPROVED, null);
            apiClient.put("http://localhost:8080/api/master/stock-requests/" + selectedRequest.getId() + "/status", updateDto, Void.class);
            loadRequests("PENDING");
            resetForm();
            showAlert("성공", "요청이 성공적으로 승인되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "요청 승인에 실패했습니다.");
        }
    }

    @FXML
    private void rejectRequest() {
        StockRequestResponseDto selectedRequest = requestTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("알림", "거절할 요청을 선택해주세요.");
            return;
        }
        if (selectedRequest.getStatus() != RequestStatus.PENDING) {
            showAlert("알림", "대기 중인 요청만 거절할 수 있습니다.");
            return;
        }

        String rejectionReason = rejectionReasonComboBox.getValue();
        if (rejectionReason == null) {
            showAlert("입력 오류", "거절 사유를 선택해주세요.");
            return;
        }

        if ("기타".equals(rejectionReason)) {
            rejectionReason = customRejectionReasonArea.getText();
            if (rejectionReason.trim().isEmpty()) {
                showAlert("입력 오류", "기타 사유를 입력해주세요.");
                return;
            }
        }

        try {
            StockRequestUpdateStatusDto updateDto = new StockRequestUpdateStatusDto(RequestStatus.REJECTED, rejectionReason);
            apiClient.put("http://localhost:8080/api/master/stock-requests/" + selectedRequest.getId() + "/status", updateDto, Void.class);
            loadRequests("PENDING");
            resetForm();
            showAlert("성공", "요청이 성공적으로 거절되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "요청 거절에 실패했습니다.");
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
