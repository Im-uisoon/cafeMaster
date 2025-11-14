package com.project.summer.repository;

import com.project.summer.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    // 상품명 중복 방지를 위해 추가
    boolean existsByProductName(String productName);
}
