package com.project.summer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 해당 파일은 401 로그인 실패 또는 토큰 만료로 인한 에러를 표현합니다

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // HTTP 응답 상태 코드를 401 Unauthorized로 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 응답 Content-Type을 JSON으로 설정
        response.setContentType("application/json;charset=UTF-8");
        // 클라이언트에게 보낼 메시지 작성
        response.getWriter().write("{\"message\": \"로그인 후 사용 가능합니다.\"}");
        response.getWriter().flush();
    }
}
