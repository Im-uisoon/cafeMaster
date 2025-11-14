package com.project.summer.repository;

import com.project.summer.domain.dailystock.DailyStockRecord;
import com.project.summer.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStockRecordRepository extends JpaRepository<DailyStockRecord, Long> {
    Optional<DailyStockRecord> findByUserAndRecordDate(UserEntity user, LocalDate recordDate);
    List<DailyStockRecord> findByUserAndRecordDateBetweenOrderByRecordDateAsc(UserEntity user, LocalDate startDate, LocalDate endDate);
    List<DailyStockRecord> findByUserOrderByRecordDateAsc(UserEntity user);
}
