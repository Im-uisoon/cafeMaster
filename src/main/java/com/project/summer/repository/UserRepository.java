package com.project.summer.repository;

import com.project.summer.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // storeCode 가지고 사용자 조회
    Optional<UserEntity> findByStoreCode(String storeCode);
    // 역할(role)로 사용자 목록 조회
    List<UserEntity> findAllByRole(String role);
    // storeCode, storeName 중복 확인
    boolean existsByStoreCode(String storeCode);
    boolean existsByStoreName(String storeName);
}