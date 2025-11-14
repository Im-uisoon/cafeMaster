package com.project.summer.repository;

import com.project.summer.domain.stockrequest.RequestStatus;
import com.project.summer.domain.stockrequest.StockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {

    // 특정 매장의 모든 재고 요청을 조회 (최신순)
    List<StockRequest> findByUser_IdOrderByRequestedAtDesc(Long userId);

    // 특정 상태의 모든 재고 요청을 조회 (요청 시간순)
    List<StockRequest> findByStatusOrderByRequestedAtAsc(RequestStatus status);

    // 특정 매장의 특정 상태 재고 요청을 조회 (요청 시간순)
    List<StockRequest> findByUser_IdAndStatusOrderByRequestedAtAsc(Long userId, RequestStatus status);
}
