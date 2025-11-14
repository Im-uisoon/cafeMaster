package com.project.summer.view;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.product.dto.ProductCreateRequestDto;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProductManagementController implements Controllable {

    @FXML
    private TableView<Product> productTableView;
    @FXML
    private TableColumn<Product, Long> idColumn;
    @FXML
    private TableColumn<Product, String> productCodeColumn;
    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    private TableColumn<Product, Integer> priceColumn;
    @FXML
    private TextField productCodeField;
    @FXML
    private TextField productNameField;
    @FXML
    private TextField priceField;

    private final ApiClient apiClient;
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @Override
    public void initData(Object data) {
        if (data instanceof String token) {
            apiClient.setJwtToken(token);
            loadProductList();
        }
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        productCodeColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductCode()));
        productNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProductName()));
        priceColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));

        productTableView.setItems(productList);
        productTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showProductDetails(newValue));
    }

    private void loadProductList() {
        try {
            Product[] products = apiClient.get("http://localhost:8080/api/master/products", Product[].class);
            productList.setAll(products);
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "물품 목록을 불러오는 데 실패했습니다.");
        }
    }

    private void showProductDetails(Product product) {
        if (product != null) {
            productCodeField.setText(product.getProductCode());
            productNameField.setText(product.getProductName());
            priceField.setText(String.valueOf(product.getPrice()));
        } else {
            resetForm();
        }
    }

    @FXML
    private void resetForm() {
        productTableView.getSelectionModel().clearSelection();
        productCodeField.clear();
        productNameField.clear();
        priceField.clear();
    }

    @FXML
    private void addProduct() {
        try {
            ProductCreateRequestDto newProduct = new ProductCreateRequestDto(
                    productCodeField.getText(),
                    productNameField.getText(),
                    Integer.parseInt(priceField.getText())
            );
            apiClient.post("http://localhost:8080/api/master/products", newProduct, Product.class);
            loadProductList();
            resetForm();
            showAlert("성공", "물품이 성공적으로 추가되었습니다.");
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "가격은 숫자로 입력해주세요.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "물품 추가에 실패했습니다.");
        }
    }

    @FXML
    private void updateProduct() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("알림", "수정할 물품을 선택해주세요.");
            return;
        }
        try {
            ProductCreateRequestDto updatedProduct = new ProductCreateRequestDto(
                    productCodeField.getText(),
                    productNameField.getText(),
                    Integer.parseInt(priceField.getText())
            );
            apiClient.put("http://localhost:8080/api/master/products/" + selectedProduct.getId(), updatedProduct, Product.class);
            loadProductList();
            resetForm();
            showAlert("성공", "물품이 성공적으로 수정되었습니다.");
        } catch (NumberFormatException e) {
            showAlert("입력 오류", "가격은 숫자로 입력해주세요.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "물품 수정에 실패했습니다.");
        }
    }

    @FXML
    private void deleteProduct() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("알림", "삭제할 물품을 선택해주세요.");
            return;
        }
        try {
            apiClient.delete("http://localhost:8080/api/master/products/" + selectedProduct.getId());
            loadProductList();
            resetForm();
            showAlert("성공", "물품이 성공적으로 삭제되었습니다.");
        } catch (ApiException e) {
            showAlert("API 오류", e.getErrorMessage());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert("오류", "물품 삭제에 실패했습니다.");
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
