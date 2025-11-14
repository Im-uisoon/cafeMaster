package com.project.summer.repository;

import com.project.summer.domain.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    // 오늘까지 만료된 재고를 찾음
    List<Stock> findByExpirationDateBefore(LocalDate date);

    // 특정 상품의 재고를 유통기한이 빠른 순으로 조회
    List<Stock> findByProduct_ProductCodeOrderByExpirationDateAsc(String productCode);

    // 특정 상품의 총 재고 수량 조회
    @org.springframework.data.jpa.repository.Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.product.productCode = :productCode")
    Optional<Integer> sumQuantityByProduct_ProductCode(@org.springframework.data.repository.query.Param("productCode") String productCode);
}
