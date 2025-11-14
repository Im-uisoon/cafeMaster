package com.project.summer.domain.auth;

import com.project.summer.domain.auth.dto.LoginRequestDto;
import com.project.summer.domain.auth.dto.RegisterRequestDto;
import com.project.summer.domain.auth.dto.StoreUpdateRequestDto;
import com.project.summer.domain.auth.dto.TokenResponseDto;
import com.project.summer.domain.user.Role;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.UserRepository;
import com.project.summer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("userService")
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션, 쓰기 필요 시 @Transactional 추가
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional // 로그인 로직은 데이터 변경(토큰 발급 등)이 포함될 수 있으므로 @Transactional
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        // 1. Username + Password 를 기반으로 Authentication 객체 생성
        // 이때 Authentication 객체는 인증 여부를 확인하는 authenticated = false 상태
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getStoreCode(), loginRequestDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(authentication);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public UserEntity registerUser(RegisterRequestDto registerRequestDto) { // RegisterRequestDto 사용
        // 매장 코드 중복
        if (userRepository.existsByStoreCode(registerRequestDto.getStoreCode())) {
            throw new IllegalArgumentException("이미 사용 중인 매장 코드입니다.");
        }
        // 매장 이름 중복
        if (userRepository.existsByStoreName(registerRequestDto.getStoreName())) {
            throw new IllegalArgumentException("이미 사용 중인 매장 이름입니다.");
        }

        UserEntity newUserEntity = UserEntity.builder()
                .storeCode(registerRequestDto.getStoreCode())
                .storeName(registerRequestDto.getStoreName()) // 매장 이름 설정
                .password(passwordEncoder.encode(registerRequestDto.getPassword())) // 비밀번호 암호화
                .role(Role.STORE) // 기본적으로 STORE 역할 부여
                .build();
        return userRepository.save(newUserEntity);
    }

    // 매장 관리 기능 추가

    // 1. 모든 매장 리스트 조회 (MASTER만)
    public List<com.project.summer.domain.auth.dto.UserResponseDto> getAllStores() {
        return userRepository.findAll().stream()
                .map(com.project.summer.domain.auth.dto.UserResponseDto::from)
                .collect(java.util.stream.Collectors.toList());
    }

    // 2. 특정 매장 정보 조회 (MASTER만)
    public UserEntity getStoreById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 매장을 찾을 수 없습니다."));
    }

    // 3. 매장 정보 수정 (MASTER만)
    @Transactional
    public UserEntity updateStore(Long id, StoreUpdateRequestDto updateRequestDto) {
        UserEntity existingStore = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 매장을 찾을 수 없습니다."));

        // 매장 코드 중복 확인 (단, 본인의 코드가 아닌 경우에만 검사)
        if (!existingStore.getStoreCode().equals(updateRequestDto.getStoreCode()) &&
                userRepository.existsByStoreCode(updateRequestDto.getStoreCode())) {
            throw new IllegalArgumentException("이미 사용 중인 매장 코드입니다.");
        }

        // 매장 이름 중복 확인 (단, 본인의 이름이 아닌 경우에만 검사)
        if (!existingStore.getStoreName().equals(updateRequestDto.getStoreName()) &&
                userRepository.existsByStoreName(updateRequestDto.getStoreName())) {
            throw new IllegalArgumentException("이미 사용 중인 매장 이름입니다.");
        }

        // 비밀번호 업데이트 로직 개선: 비밀번호가 비어있지 않은 경우에만 업데이트
        String updatedPassword = existingStore.getPassword(); // 기본적으로 기존 비밀번호 유지
        if (updateRequestDto.getPassword() != null && !updateRequestDto.getPassword().trim().isEmpty()) {
            updatedPassword = passwordEncoder.encode(updateRequestDto.getPassword()); // 새 비밀번호 해싱
        }

        // 정보 업데이트
        existingStore = UserEntity.builder()
                .id(existingStore.getId())
                .storeCode(updateRequestDto.getStoreCode())
                .storeName(updateRequestDto.getStoreName())
                .password(updatedPassword) // <<< 업데이트된 비밀번호 사용
                .role(existingStore.getRole())
                .build();

        // save()는 ID가 있으면 update, 없으면 insert
        return userRepository.save(existingStore);
    }

    // 4. 매장 정보 삭제 (MASTER만)
    @Transactional
    public void deleteStore(Long id) {
        // 삭제할 UserEntity를 조회합니다. 이 과정에서 해당 엔티티는 영속성 컨텍스트에 포함됩니다.
        UserEntity userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 매장을 찾을 수 없습니다."));

        // 영속성 컨텍스트에 포함된 엔티티를 삭제하면, Cascade 옵션에 따라
        // 관련된 모든 자식 엔티티(StoreStock, StockRequest)가 함께 삭제됩니다.
        userRepository.delete(userToDelete);
    }
}