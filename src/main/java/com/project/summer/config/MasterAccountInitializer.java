package com.project.summer.config;

import com.project.summer.domain.user.Role;
import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override // 본사 계정 생성
    public void run(String... args) throws Exception {
        final String masterStoreCode = "M1";
        final String masterStoreName = "본사";
        final String masterRawPassword = "1";

        // M1 계정이 이미 존재하는지 확인
        if (userRepository.existsByStoreCode(masterStoreCode)) {
            return; // 이미 존재하면 추가하지 않고 종료
        }

        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(masterRawPassword);

        // MASTER 계정 생성
        UserEntity masterAccount = UserEntity.builder()
                .storeCode(masterStoreCode)
                .storeName(masterStoreName)
                .password(encodedPassword) // 해시된 비밀번호 저장
                .role(Role.MASTER) // MASTER 역할 부여
                .build();

        userRepository.save(masterAccount);
    }
}
