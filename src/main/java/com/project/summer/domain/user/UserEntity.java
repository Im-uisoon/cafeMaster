package com.project.summer.domain.user;

import com.project.summer.domain.stockrequest.StockRequest;
import com.project.summer.domain.storestock.StoreStock;
import com.project.summer.domain.discard.DiscardRecord;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매장 코드
    @Column(nullable = false, unique = true)
    private String storeCode;

    // 매장 이름
    @Column(nullable = false, unique = true)
    private String storeName;

    // 비밀번호(해시)
    @Column(nullable = false)
    private String password;

    // 역할 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // User가 삭제될 때 StoreStock도 함께 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreStock> storeStocks = new ArrayList<>();

    // User가 삭제될 때 StockRequest도 함께 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockRequest> stockRequests = new ArrayList<>();

    // User가 삭제될 때 DiscardRecord도 함께 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.project.summer.domain.discard.DiscardRecord> discardRecords = new ArrayList<>();
}
