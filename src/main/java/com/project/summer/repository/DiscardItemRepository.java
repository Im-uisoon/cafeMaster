package com.project.summer.repository;

import com.project.summer.domain.discard.DiscardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscardItemRepository extends JpaRepository<DiscardItem, Long> {

    // 특정 DiscardRecord에 속한 모든 DiscardItem 조회
    List<DiscardItem> findByDiscardRecord_Id(Long discardRecordId);
}
