package com.project.summer.domain.stockrequest;

import com.project.summer.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // 요청한 매장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // 요청 상태

    @Column(nullable = false)
    private LocalDateTime requestedAt; // 요청 시간

    private String rejectionReason; // 거절 사유

    @OneToMany(mappedBy = "stockRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockRequestItem> items = new ArrayList<>();

    // 생성자
    public StockRequest(UserEntity user) {
        this.user = user;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    // 연관관계 편의 메서드
    public void addItem(StockRequestItem item) {
        items.add(item);
        item.setStockRequest(this);
    }

    // 상태 변경 메서드
    public void approve() {
        this.status = RequestStatus.APPROVED;
    }

    public void reject(String reason) {
        this.status = RequestStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void complete() {
        this.status = RequestStatus.COMPLETED;
    }
}
