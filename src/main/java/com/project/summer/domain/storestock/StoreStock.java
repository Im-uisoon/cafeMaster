package com.project.summer.domain.storestock;

import com.project.summer.domain.product.Product;
import com.project.summer.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "store_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 재고가 속한 매장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 어떤 상품에 대한 재고인지

    @Column(nullable = false)
    private int quantity; // 수량

    @Column(nullable = false)
    private LocalDate expirationDate; // 유통기한

    // 수량 업데이트를 위한 메소드
    public void update(int quantity) {
        this.quantity = quantity;
    }
}
