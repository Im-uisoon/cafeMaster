package com.project.summer.security;

import com.project.summer.domain.user.UserEntity;
import com.project.summer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // 사용자 정보를 DB에서 가져오기 위한 Repository
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String storeCode) throws UsernameNotFoundException {
        return userRepository.findByStoreCode(storeCode)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(storeCode + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // UserEntity 엔티티를 Spring Security의 UserDetails 객체로 변환
    private UserDetails createUserDetails(UserEntity userEntity) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole().getKey()));

        // Spring Security의 User 객체 생성 시 7개의 인자를 모두 제공합니다.
        // FQCN (Fully Qualified Class Name)을 명확히 사용
        return new org.springframework.security.core.userdetails.User(
                userEntity.getStoreCode(),     // username
                userEntity.getPassword(),      // password
                authorities                   // authorities
        );
    }
}
