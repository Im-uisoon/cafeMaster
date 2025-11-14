package com.project.summer.domain.stock;

import com.project.summer.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 재고 항목의 고유 ID (Primary Key)

    @ManyToOne(fetch = FetchType.LAZY) // Product와 다대일 관계 (지연 로딩)
    @JoinColumn(name = "product_id", nullable = false) // 외래 키 컬럼 이름 설정 (필수)
    private Product product; // 이 재고 항목이 어떤 상품에 대한 것인지 참조

    @Column(nullable = false)
    private LocalDate expirationDate; // 유통기한 (필수)

    @Column(nullable = false)
    private int quantity; // 개수 (필수)

    public void update(LocalDate expirationDate, int quantity) {
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
