package com.project.summer.repository;

import com.project.summer.domain.stockrequest.StockRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRequestItemRepository extends JpaRepository<StockRequestItem, Long> {
}
