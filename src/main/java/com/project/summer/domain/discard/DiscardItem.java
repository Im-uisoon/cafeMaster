package com.project.summer.domain.discard;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.storestock.StoreStock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "discard_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discard_record_id", nullable = false)
    private DiscardRecord discardRecord; // 이 폐기 항목이 속한 일별 폐기 기록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 폐기된 상품

    @Column(nullable = false)
    private int quantity; // 폐기된 수량

    @Column(nullable = false)
    private LocalDate expirationDate; // 폐기 당시 유통기한

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscardType discardType; // 폐기 유형 (자동/수동)

    private String discardReason; // 수동 폐기 시 사유

    // StoreStock 엔티티의 ID를 참조하여 어떤 재고 항목이 폐기되었는지 추적
    @Column(nullable = false)
    private Long storeStockId; 

    @Builder
    public DiscardItem(DiscardRecord discardRecord, Product product, int quantity, LocalDate expirationDate, DiscardType discardType, String discardReason, Long storeStockId) {
        this.discardRecord = discardRecord;
        this.product = product;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.discardType = discardType;
        this.discardReason = discardReason;
        this.storeStockId = storeStockId;
    }

    // 연관관계 설정 편의 메서드
    public void setDiscardRecord(DiscardRecord discardRecord) {
        this.discardRecord = discardRecord;
    }
}
