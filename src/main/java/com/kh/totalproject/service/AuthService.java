package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.request.SaveAdminRequest;
import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManagerBuilder managerBuilder;
    private final PasswordEncoder passwordEncoder;

    // Login 시 토큰 반환
    public TokenResponse login(LoginRequest loginRequest){
        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 아이디가 없습니다."));
//        log.info("PasswordEncoder : {}", passwordEncoder.getClass()); // 디버그용
//        log.info("입력된 비밀번호 : {}", loginRequest.getPassword());
//        log.info("저장된 암호화된 비밀번호 : {}", user.getPassword());
//        boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
//        log.info("비밀번호 일치 여부: {}", isPasswordMatch);
        // 입력 받은 password 와 Database password 일치 시 다음 기능 수행
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            // 로그인 입력 받은 아이디, 패스워드 기반 Spring Security Token 생성
            UsernamePasswordAuthenticationToken authenticationToken = loginRequest.toAuthentication(); 
            Authentication authentication = managerBuilder.getObject().authenticate(authenticationToken);
            return jwtUtil.generateToken(authentication);
        }
        else log.warn("비밀번호가 일치하지 않습니다.");
        return null;
    }

    // 회원 가입 (반환 타입 - UserInfoResponse)
    public UserInfoResponse saveUser(SaveUserRequest requestDto){
        User user = requestDto.toEntity(passwordEncoder);
        return UserInfoResponse.of(userRepository.save(user));
    }

    // 관리자 회원 가입 (반환 타입 - UserInfoResponse)
    public UserInfoResponse saveAdmin(SaveAdminRequest requestDto){
        User user = requestDto.toEntity(passwordEncoder);
        return UserInfoResponse.of(userRepository.save(user));
    }
}
