package com.project.summer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 해당 파일은 403 권한 부족으로 인한 문제를 처리합니다.

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // HTTP 응답 상태 코드를 403 Forbidden으로 설정
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 응답 Content-Type을 JSON으로 설정 (프론트엔드에서 파싱하기 위함)
        response.setContentType("application/json;charset=UTF-8");

        String errorMessage;
        String requestURI = request.getRequestURI(); // 요청 URI 경로를 가져옴

        // 403 Forbidden : URI 오류 메시지 설정
        errorMessage = "{\"message\": \"관리자 권한이 필요합니다.\"}";

        // 클라이언트에게 JSON 형태의 오류 메시지 전송
        response.getWriter().write(errorMessage);
        response.getWriter().flush();
    }
}