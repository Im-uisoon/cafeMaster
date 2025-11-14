package com.project.summer.domain.stockrequest;

import com.project.summer.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AllocatedStockDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_request_item_id")
    private StockRequestItem stockRequestItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 어떤 상품의 할당인지

    @Column(nullable = false)
    private int quantity; // 할당된 수량

    @Column(nullable = false)
    private LocalDate expirationDate; // 할당된 재고의 유통기한

    public AllocatedStockDetail(StockRequestItem stockRequestItem, Product product, int quantity, LocalDate expirationDate) {
        this.stockRequestItem = stockRequestItem;
        this.product = product;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
    }

    // 연관관계 설정
    public void setStockRequestItem(StockRequestItem stockRequestItem) {
        this.stockRequestItem = stockRequestItem;
    }
}
