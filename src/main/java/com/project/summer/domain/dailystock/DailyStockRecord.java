package com.project.summer.domain.dailystock;

import com.project.summer.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private int totalQuantity;

    @Builder
    public DailyStockRecord(UserEntity user, LocalDate recordDate, int totalQuantity) {
        this.user = user;
        this.recordDate = recordDate;
        this.totalQuantity = totalQuantity;
    }
}
