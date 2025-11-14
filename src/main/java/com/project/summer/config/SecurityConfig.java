package com.project.summer.config;

import com.project.summer.security.CustomAccessDeniedHandler;
import com.project.summer.security.CustomAuthenticationEntryPoint;
import com.project.summer.security.JwtAuthenticationFilter;
import com.project.summer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean // 비밀번호 암호화 Encoder 등록
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // Spring Security 필터 체인 설정
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 사용 시 필요 없는 기능 끄기
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // 세션 사용 안함 (JWT는 stateless 사용)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 권한 관리 옵션
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 및 기본 페이지 접근 허용
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico").permitAll()
                        // 로그인은 권한 없이 모두 허용
                        .requestMatchers("/api/auth/login").permitAll()
                        // 회원가입은 MASTER만 가능
                        .requestMatchers("/api/auth/register").hasRole("MASTER")
                        // MASTER만 접근 가능한 API
                        .requestMatchers("/api/master/stores/**").hasRole("MASTER")
                        .requestMatchers(HttpMethod.GET, "/api/master/products").hasAnyRole("MASTER", "STORE")
                        .requestMatchers("/api/master/products/**").hasRole("MASTER")
                        .requestMatchers("/api/master/stocks/**").hasRole("MASTER")
                        // 기존 지점 재고 관련 API 접근 제어
                        .requestMatchers(HttpMethod.POST, "/api/store/stocks").denyAll() // 직접 등록 비활성화
                        .requestMatchers("/api/store/stocks/**").hasAnyRole("MASTER", "STORE")

                        // 새로운 재고 요청 API 권한 설정
                        .requestMatchers("/api/store/stock-requests/**").hasRole("STORE")
                        .requestMatchers("/api/master/stock-requests/**").hasRole("MASTER")

                        // 폐기 관련 API 권한 설정
                        .requestMatchers("/api/store/discard").hasRole("STORE") // 수동 폐기
                        .requestMatchers("/api/discard/daily").hasAnyRole("MASTER", "STORE") // 일일 폐기 내역 조회
                        .requestMatchers("/api/discard/monthly").hasAnyRole("MASTER", "STORE") // 월별 폐기 요약 조회

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 처리 설정 (접근 거부 핸들러 추가)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .accessDeniedHandler(customAccessDeniedHandler) // << 403 Error
                                .authenticationEntryPoint(customAuthenticationEntryPoint) // << 401 Error
                )

                // JWT 필터 등록: UsernamePasswordAuthenticationFilter 이전에 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}