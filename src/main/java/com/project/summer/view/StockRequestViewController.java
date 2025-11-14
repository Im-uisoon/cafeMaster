package com.project.summer.view;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.summer.domain.product.Product;
import com.project.summer.domain.stockrequest.RequestStatus;
import com.project.summer.domain.dto.stockrequest.StockRequestCreateDto;
import com.project.summer.domain.dto.stockrequest.StockRequestItemDto;
import com.project.summer.domain.dto.stockrequest.StockRequestResponseDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockRequestViewController implements Controllable {

    @FXML
    private TableView<StockRequestResponseDto> requestStatusTableView;
    @FXML
    private TableColumn<StockRequestResponseDto, Long> requestIdColumn;
    @FXML
    private TableColumn<StockRequestResponseDto, String> requestTimeColumn;
    @FXML
    private TableColumn<StockRequestResponseDto, RequestStatus> requestStatusColumn;
    @FXML
    private TableColumn<StockRequestResponseDto, String> requestRejectionReasonColumn;

    @FXML
    private TextField productCodeField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField quantityField;

    @FXML
    private TableView<StockRequestItemDto> requestItemsTableView;
    @FXML
    private TableColumn<StockRequestItemDto, String> reqProductCodeColumn;
    @FXML
    private TableColumn<StockRequestItemDto, String> reqProductNameColumn;
    @FXML
    private TableColumn<StockRequestItemDto, Integer> reqQuantityColumn;

    @FXML
    private TextField detailStoreNameField;
    @FXML
    private TextField detailRequestedDateField;
    @FXML
    private TextField detailProductNameField;
    @FXML
    private TextField detailQuantityField;
    @FXML
    private TextField detailStatusField;
    @FXML
    private TextArea detailRejectionReasonArea;

    private final ApiClient apiClient;
    private final ObservableList<StockRequestResponseDto> requestStatusList = FXCollections.observableArrayList();
    private final ObservableList<StockRequestItemDto> itemsToRequest = FXCollections.observableArrayList();
    private final ObservableList<Product> headquartersProductList = FXCollections.observableArrayList();

    @FXML
    private TableView<Product> headquartersProductTableView;
    @FXML
    private TableColumn<Product, Long> hqProductIdColumn;
    @FXML
    private TableColumn<Product, String> hqProductCodeColumn;
    @FXML
    private TableColumn<Product, String> hqProductNameColumn;
    @FXML
    private TableColumn<Product, Integer> hqPriceColumn;

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadRequestStatus();
            loadHeadquartersProductList();
        }
    }

    @FXML
    private void initialize() {
        // 요청 현황 테이블 초기화
        requestIdColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        requestTimeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        requestStatusColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getStatus()));
        requestRejectionReasonColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRejectionReason()));

        requestStatusTableView.setItems(requestStatusList);
        requestStatusTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showRequestDetails(newValue));

        // 본사 메뉴얼 테이블 초기화
        hqProductIdColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        hqProductCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        hqProductNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        hqPriceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));

        headquartersProductTableView.setItems(headquartersProductList);
        headquartersProductTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        productCodeField.setText(newValue.getProductCode());
                        productNameField.setText(newValue.getProductName());
                    }
                });

        // 요청할 상품 목록 테이블 초기화
        reqProductCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        reqProductNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        reqQuantityColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        requestItemsTableView.setItems(itemsToRequest);

        // 상품 코드 또는 상품명 입력 시 자동 채우기
        productCodeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !productCodeField.getText().isEmpty()) { // 포커스를 잃었을 때
                fillProductDetailsByCode(productCodeField.getText());
            }
        });
        productNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !productNameField.getText().isEmpty()) { // 포커스를 잃었을 때
                fillProductDetailsByName(productNameField.getText());
            }
        });

        // 상세 정보 필드들은 읽기 전용
        // storeNameField.setEditable(false);
        // requestedDateField.setEditable(false);
        // statusField.setEditable(false);
        // rejectionReasonArea.setEditable(false);
    }

    private void fillProductDetailsByCode(String productCode) {
        try {
            Product[] products = apiClient.get("http://localhost:8080/api/master/products", Product[].class);
            for (Product product : products) {
                if (product.getProductCode().equals(productCode)) {
                    productNameField.setText(product.getProductName());
                    return;
                }
            }
            showAlert("알림", "해당 상품 코드를 가진 상품을 찾을 수 없습니다.");
            productNameField.clear();
        } catch (ApiException | IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "상품 정보를 불러오는 데 실패했습니다.");
        }
    }

    private void fillProductDetailsByName(String productName) {
        try {
            Product[] products = apiClient.get("http://localhost:8080/api/master/products", Product[].class);
            for (Product product : products) {
                if (product.getProductName().equals(productName)) {
                    productCodeField.setText(product.getProductCode());
                    return;
                }
            }
            showAlert("알림", "해당 상품명을 가진 상품을 찾을 수 없습니다.");
            productCodeField.clear();
        } catch (ApiException | IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "상품 정보를 불러오는 데 실패했습니다.");
        }
    }

    private void loadRequestStatus() {
        try {
            List<StockRequestResponseDto> requests = apiClient.get("http://localhost:8080/api/store/stock-requests", new TypeReference<List<StockRequestResponseDto>>() {});
            requestStatusList.setAll(requests);
        } catch (ApiException e) {
            System.err.println("API Exception: " + e.getErrorMessage());
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error loading stock requests: " + e.getMessage());
            showAlert("오류", "요청 현황을 불러오는 데 실패했습니다.");
        }
    }

    private void showRequestDetails(StockRequestResponseDto request) {
        if (request != null) {
            detailStoreNameField.setText(request.getStoreName());
            detailRequestedDateField.setText(request.getRequestedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            detailStatusField.setText(request.getStatus().toString());
            detailRejectionReasonArea.setText(request.getRejectionReason() != null ? request.getRejectionReason() : "");

            // 요청 상세 아이템 표시 (상품명 목록)
            String itemsText = request.getItems().stream()
                    .map(item -> item.getProductName() + " (" + item.getQuantity() + "개)")
                    .collect(Collectors.joining(", "));
            detailProductNameField.setText(itemsText);

            // APPROVED 상태면 요청 아이템을 itemsToRequest에 자동 추가
            if (request.getStatus() == RequestStatus.APPROVED) {
                itemsToRequest.clear();
                for (com.project.summer.domain.dto.stockrequest.StockRequestItemResponseDto item : request.getItems()) {
                    itemsToRequest.add(new StockRequestItemDto(item.getProductCode(), item.getProductName(), item.getQuantity()));
                }
            } else {
                itemsToRequest.clear();
            }

        } else {
            resetForm();
        }
    }


    @FXML
    private void resetForm() {
        requestStatusTableView.getSelectionModel().clearSelection();
        productCodeField.clear();
        productNameField.clear();
        quantityField.clear();
        itemsToRequest.clear();

        detailStoreNameField.clear();
        detailRequestedDateField.clear();
        detailStatusField.clear();
        detailRejectionReasonArea.clear();
    }

    @FXML
    private void addItemToRequest() {
        String productCode = productCodeField.getText();
        String productName = productNameField.getText(); // 검색용
        String quantityText = quantityField.getText();

        if (productCode.isEmpty() || quantityText.isEmpty()) {
            showAlert("입력 오류", "상품 코드와 개수를 입력해주세요.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("입력 오류", "개수는 1개 이상이어야 합니다.");
                return;
            }
            itemsToRequest.add(new StockRequestItemDto(productCode, productName, quantity));
            productCodeField.clear();
            productNameField.clear();
            quantityField.clear();
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "개수는 숫자로 입력해주세요.");
        }
    }

    @FXML
    private void sendStockRequest() {
        if (itemsToRequest.isEmpty()) {
            showAlert("알림", "요청할 상품을 추가해주세요.");
            return;
        }

        try {
            StockRequestCreateDto requestDto = new StockRequestCreateDto(new ArrayList<>(itemsToRequest));
            apiClient.post("http://localhost:8080/api/store/stock-requests", requestDto, Void.class);
            loadRequestStatus();
            resetForm();
            showAlert("성공", "발주 요청이 성공적으로 전송되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "발주 요청 전송에 실패했습니다.");
        }
    }

    @FXML
    private void confirmStockRequest() {
        StockRequestResponseDto selectedRequest = requestStatusTableView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("알림", "입고 처리할 요청을 선택해주세요.");
            return;
        }
        if (selectedRequest.getStatus() != RequestStatus.APPROVED) {
            showAlert("알림", "승인된 요청만 입고 처리할 수 있습니다.");
            return;
        }

        try {
            apiClient.post("http://localhost:8080/api/store/stock-requests/" + selectedRequest.getId() + "/confirm", null, Void.class);
            loadRequestStatus();
            resetForm();
            showAlert("성공", "재고 입고가 성공적으로 처리되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 입고 처리에 실패했습니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadHeadquartersProductList() {
        try {
            Product[] products = apiClient.get("http://localhost:8080/api/master/products", Product[].class);
            headquartersProductList.setAll(products);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "본사 물품 목록을 불러오는 데 실패했습니다.");
        }
    }
}