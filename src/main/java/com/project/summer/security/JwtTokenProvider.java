package com.project.summer.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 Secret Key
    private final Key key;
    private final long accessTokenValidityInMilliseconds;

    // application.properties에서 secret 값 주입
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.accessTokenValidityInMilliseconds}") long accessTokenValidityInMilliseconds) {
        // Base64로 인코딩된 secret key 디코딩
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // HMAC-SHA 알고리즘을 사용하여 Key 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
        // 토큰 만료 시간
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
    }

    // JWT 토큰 생성 메소드 (로그인 성공 시 호출)
    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // 권한 정보를 쉼표로 구분하여 문자열로 변환
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds); // 1일 (86400000 ms)

        return Jwts.builder()
                .setSubject(authentication.getName()) // 토큰 제목 (사용자 ID)
                .claim("auth", authorities) // "auth" 클레임에 권한 정보 저장
                .setExpiration(accessTokenExpiresIn) // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256) // 서명 (secret key와 알고리즘)
                .compact(); // 토큰 생성
    }

    // JWT 토큰을 복호화하여 인증 정보를 추출하는 메소드
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken); // 토큰에서 클레임(claims) 추출

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 생성하여 Authentication 객체 반환
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 토큰 유효성 검사 메소드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // 토큰 파싱 및 유효성 검사
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("로그인에 문제가 발생했습니다", e); // 유효하지 않은 JWT 토큰
        } catch (ExpiredJwtException e) {
            log.info("로그인을 다시 해주세요", e); // 만료된 JWT 토큰
        } catch (UnsupportedJwtException e) {
            log.info("로그인을 다시 해주세요", e); // 지원되지 않는 JWT 토큰
        } catch (IllegalArgumentException e) {
            log.info("로그인을 다시 해주세요", e); // JWT 클레임 문자열이 비어있음
        }
        return false;
    }

    // JWT 토큰에서 클레임(claims) 정보만 추출
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서도 클레임 정보는 가져올 수 있도록 처리
            return e.getClaims();
        }
    }
}
