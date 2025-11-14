package com.project.summer.repository;

import com.project.summer.domain.discard.DiscardRecord;
import com.project.summer.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscardRecordRepository extends JpaRepository<DiscardRecord, Long> {

    // 특정 매장의 특정 날짜 폐기 기록 조회
    Optional<DiscardRecord> findByUserAndDiscardDate(UserEntity user, LocalDate discardDate);

    // 특정 매장의 특정 기간 폐기 기록 조회 (월별 요약용)
    List<DiscardRecord> findByUserAndDiscardDateBetweenOrderByDiscardDateAsc(UserEntity user, LocalDate startDate, LocalDate endDate);

    // 모든 매장의 특정 기간 폐기 기록 조회 (월별 요약용)
    List<DiscardRecord> findByDiscardDateBetweenOrderByDiscardDateAsc(LocalDate startDate, LocalDate endDate);

    // 모든 매장의 특정 날짜 폐기 기록 조회
    List<DiscardRecord> findByDiscardDate(LocalDate discardDate);

    // 특정 매장의 모든 폐기 기록을 날짜순으로 조회
    List<DiscardRecord> findByUserOrderByDiscardDateAsc(UserEntity user);
}
