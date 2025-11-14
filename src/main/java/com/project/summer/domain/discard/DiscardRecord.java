package com.project.summer.domain.discard;

import com.project.summer.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discard_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 폐기 기록이 속한 매장

    @Column(nullable = false)
    private LocalDate discardDate; // 폐기 날짜

    @Column(nullable = false)
    private int totalDiscardAmount; // 총 폐기 금액 (상품 단가 * 개수)

    @Column(nullable = false)
    private int totalDiscardProductCount; // 총 폐기 상품 종류 개수 (고유 상품명 기준)

    @OneToMany(mappedBy = "discardRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiscardItem> discardItems = new ArrayList<>();

    @Builder
    public DiscardRecord(UserEntity user, LocalDate discardDate, int totalDiscardAmount, int totalDiscardProductCount) {
        this.user = user;
        this.discardDate = discardDate;
        this.totalDiscardAmount = totalDiscardAmount;
        this.totalDiscardProductCount = totalDiscardProductCount;
    }

    // 편의 메서드
    public void addDiscardItem(DiscardItem item) {
        this.discardItems.add(item);
        item.setDiscardRecord(this);
    }

    public void updateTotalDiscardAmount(int amount) {
        this.totalDiscardAmount += amount;
    }

    public void updateTotalDiscardProductCount(int count) {
        this.totalDiscardProductCount += count;
    }
}
