package com.project.summer.domain.auth.dto;

import com.project.summer.domain.user.UserEntity;
import com.project.summer.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String storeCode;
    private String storeName;
    private Role role;

    public static UserResponseDto from(UserEntity userEntity) {
        return UserResponseDto.builder()
                .id(userEntity.getId())
                .storeCode(userEntity.getStoreCode())
                .storeName(userEntity.getStoreName())
                .role(userEntity.getRole())
                .build();
    }
}
