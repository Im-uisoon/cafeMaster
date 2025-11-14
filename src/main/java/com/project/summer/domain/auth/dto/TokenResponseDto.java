package com.project.summer.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // Lombok 빌더 패턴으로 객체 생성
public class TokenResponseDto {
    private String accessToken;
    private String tokenType = "Bearer"; // 토큰 타입 명시 (JWT 표준)
    // 리프레시 토큰도 여기에 추가할 수 있습니다.
}