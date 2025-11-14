package com.project.summer.view;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.stock.dto.StockCreateRequestDto;
import com.project.summer.domain.stock.dto.StockResponseDto;
import com.project.summer.domain.stock.dto.StockUpdateRequestDto;
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
public class StockManagementController implements Controllable {

    @FXML
    private TableView<StockResponseDto> stockTableView;
    @FXML
    private TableColumn<StockResponseDto, Long> idColumn;
    @FXML
    private TableColumn<StockResponseDto, String> productCodeColumn;
    @FXML
    private TableColumn<StockResponseDto, String> productNameColumn;
    @FXML
    private TableColumn<StockResponseDto, LocalDate> expirationDateColumn;
    @FXML
    private TableColumn<StockResponseDto, Integer> quantityColumn;

    @FXML
    private TableView<Product> productListTableView;
    @FXML
    private TableColumn<Product, Long> productIdColumnForProductList;
    @FXML
    private TableColumn<Product, String> productCodeColumnForProductList;
    @FXML
    private TableColumn<Product, String> productNameColumnForProductList;
    @FXML
    private TableColumn<Product, Integer> priceColumnForProductList;

    @FXML
    private TextField productCodeField;
    @FXML
    private TextField productNameField;
    @FXML
    private DatePicker expirationDateField;
    @FXML
    private TextField quantityField;

    @FXML
    private ListView<String> recommendationListView;

    private final ApiClient apiClient;
    private final ObservableList<StockResponseDto> stockList = FXCollections.observableArrayList();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<String> recommendationList = FXCollections.observableArrayList();

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadStockList();
            loadProductList();
            updateRecommendationList();
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        productCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        productNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        expirationDateColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getExpirationDate()));
        quantityColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantity()));

        stockTableView.setItems(stockList);
        stockTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showStockDetails(newValue));

        productIdColumnForProductList.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        productCodeColumnForProductList.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        productNameColumnForProductList.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        priceColumnForProductList.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));

        productListTableView.setItems(productList);
        productListTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        productCodeField.setText(newValue.getProductCode());
                        productNameField.setText(newValue.getProductName());
                    }
                });

        recommendationListView.setItems(recommendationList);
        recommendationListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        String[] parts = newValue.split(" - ");
                        productCodeField.setText(parts[0]);
                        productNameField.setText(parts[1]);
                    }
                });

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
    }

    private void updateRecommendationList() {
        recommendationList.clear();
        for (Product product : productList) {
            int totalQuantity = stockList.stream()
                    .filter(stock -> stock.getProductCode().equals(product.getProductCode()))
                    .mapToInt(StockResponseDto::getQuantity)
                    .sum();
            if (totalQuantity < 5) {
                recommendationList.add(product.getProductCode() + " - " + product.getProductName());
            }
        }
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

    private void loadStockList() {
        try {
            StockResponseDto[] stocks = apiClient.get("http://localhost:8080/api/master/stocks", StockResponseDto[].class);
            stockList.setAll(stocks);
            updateRecommendationList();
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void loadProductList() {
        try {
            Product[] products = apiClient.get("http://localhost:8080/api/master/products", Product[].class);
            productList.setAll(products);
            updateRecommendationList();
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "물품 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void showStockDetails(StockResponseDto stock) {
        if (stock != null) {
            productCodeField.setText(stock.getProductCode());
            productNameField.setText(stock.getProductName());
            expirationDateField.setValue(stock.getExpirationDate());
            quantityField.setText(String.valueOf(stock.getQuantity()));

            // 수정 불가능하도록 설정
            productCodeField.setEditable(false);
            productNameField.setEditable(false);
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        stockTableView.getSelectionModel().clearSelection();
        productCodeField.clear();
        productNameField.clear();
        expirationDateField.setValue(null);
        quantityField.clear();

        // 수정 가능하도록 설정
        productCodeField.setEditable(true);
        productNameField.setEditable(true);
    }

    @FXML
    private void addStock() {
        try {
            StockCreateRequestDto newStock = new StockCreateRequestDto(
                    productCodeField.getText(),
                    expirationDateField.getValue(),
                    Integer.parseInt(quantityField.getText())
            );
            apiClient.post("http://localhost:8080/api/master/stocks", newStock, StockResponseDto.class);
            loadStockList();
            resetForm();
            showAlert("성공", "재고가 성공적으로 추가되었습니다.");
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "개수는 숫자로 입력해주세요.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 추가에 실패했습니다.");
        }
    }

    @FXML
    private void updateStock() {
        StockResponseDto selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("알림", "수정할 재고를 선택해주세요.");
            return;
        }
        try {
            StockUpdateRequestDto updatedStock = new StockUpdateRequestDto(
                    expirationDateField.getValue(),
                    Integer.parseInt(quantityField.getText())
            );
            apiClient.put("http://localhost:8080/api/master/stocks/" + selectedStock.getId(), updatedStock, StockResponseDto.class);
            loadStockList();
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
    private void deleteStock() {
        StockResponseDto selectedStock = stockTableView.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("알림", "삭제할 재고를 선택해주세요.");
            return;
        }
        try {
            apiClient.delete("http://localhost:8080/api/master/stocks/" + selectedStock.getId());
            loadStockList();
            resetForm();
            showAlert("성공", "재고가 성공적으로 삭제되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "재고 삭제에 실패했습니다.");
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
