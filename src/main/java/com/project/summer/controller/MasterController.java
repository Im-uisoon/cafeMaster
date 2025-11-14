package com.project.summer.controller;

import com.project.summer.domain.auth.UserService;
import com.project.summer.domain.auth.dto.StoreUpdateRequestDto;
import com.project.summer.domain.auth.dto.UserResponseDto;
import com.project.summer.domain.user.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master/stores")
public class MasterController {

    private final UserService userService;

    // 모든 매장 리스트 조회
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllStores() {
        List<UserResponseDto> stores = userService.getAllStores();
        // 실제 운영에서는 비밀번호 등 민감 정보를 제외한 DTO로 변환하여 반환해야 합니다.
        return ResponseEntity.ok(stores);
    }

    // 특정 매장 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getStoreById(@PathVariable Long id) {
        UserEntity store = userService.getStoreById(id);
        return ResponseEntity.ok(store);
    }

    // 매장 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateStore(@PathVariable Long id, @Valid @RequestBody StoreUpdateRequestDto updateRequestDto) {
        UserEntity updatedStore = userService.updateStore(id, updateRequestDto);
        return ResponseEntity.ok(updatedStore);
    }

    // 매장 정보 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        userService.deleteStore(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
