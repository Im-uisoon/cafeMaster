package com.project.summer.domain.stockrequest;

import com.project.summer.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_request_id")
    private StockRequest stockRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @OneToMany(mappedBy = "stockRequestItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AllocatedStockDetail> allocatedStockDetails = new ArrayList<>();

    // 생성자
    public StockRequestItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // 연관관계 설정
    public void setStockRequest(StockRequest stockRequest) {
        this.stockRequest = stockRequest;
    }

    // 할당된 재고 상세 정보 추가
    public void addAllocatedStockDetail(AllocatedStockDetail detail) {
        this.allocatedStockDetails.add(detail);
        detail.setStockRequestItem(this);
    }
}
