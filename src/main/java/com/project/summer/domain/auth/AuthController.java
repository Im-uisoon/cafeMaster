package com.project.summer.domain.auth;

import com.project.summer.domain.auth.dto.LoginRequestDto;
import com.project.summer.domain.auth.dto.RegisterRequestDto;
import com.project.summer.domain.auth.dto.TokenResponseDto;
import com.project.summer.domain.user.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService; // userService 주입

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        UserEntity newUser = userService.registerUser(registerRequestDto);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto token = userService.login(loginRequestDto);
        return ResponseEntity.ok(token);
    }

    // 테스트용 인증된 사용자 정보 확인 엔드포인트
    @GetMapping("/profile")
    public ResponseEntity<String> getUserInfo(java.security.Principal principal) {
        // principal.getName()은 이제 storeCode를 반환합니다.
        return ResponseEntity.ok("안녕하세요, " + principal.getName() + "님! (인증 성공)");
    }

    // 에러코드 모음집
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    // 에러코드 처리기 (회원가입 에러)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request 반환
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage()); // 예외 메시지를 그대로 반환
        return ResponseEntity.badRequest().body(error);
    }

    // 에러코드 처리기 (로그인 에러)
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401 Unauthorized 반환
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        // 매장 코드가 존재하지 않거나 비밀번호가 틀렸을 때 모두 동일한 메시지를 반환하여 보안 강화
        error.put("message", "매장 코드 또는 비밀번호가 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}