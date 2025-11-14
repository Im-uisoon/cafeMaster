package com.project.summer.repository;

import com.project.summer.domain.storestock.StoreStock;
import com.project.summer.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StoreStockRepository extends JpaRepository<StoreStock, Long> {

    // 특정 매장의 모든 재고를 조회
    List<StoreStock> findByUser_StoreCode(String storeCode);

    // 특정 매장의 모든 재고를 UserEntity로 조회
    List<StoreStock> findByUser(UserEntity user);

    // 특정 매장의 특정 상품 재고를 조회 (중복 추가 방지용)
    boolean existsByUser_StoreCodeAndProduct_ProductCode(String storeCode, String productCode);

    // 유통기한이 지난 재고 조회
    List<StoreStock> findByExpirationDateBefore(LocalDate date);
}
