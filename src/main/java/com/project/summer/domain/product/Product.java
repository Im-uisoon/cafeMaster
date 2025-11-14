package com.project.summer.domain.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품의 고유 ID (Primary Key)

    @Column(unique = true, nullable = false)
    private String productCode; // 상품 코드 (고유하며 필수)

    @Column(nullable = false)
    private String productName; // 상품명 (필수)

    @Column(nullable = false)
    private int price; // 가격 (필수)
}
