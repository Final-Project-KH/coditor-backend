package com.kh.totalproject.controller;


import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.request.SaveAdminRequest;
import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.service.AuthService;
import com.kh.totalproject.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
// /auth 경로는 인증 요구 해제
// 회원가입 또한 /auth 경로로 이동해야함
public class AuthController {
    private final AuthService authService;
    
    // login 진행 시 Token 반환
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> signIn(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    // 회원가입 (유저)
    @PostMapping("/signup")
    public ResponseEntity<UserInfoResponse> handleSignUp(@RequestBody SaveUserRequest requestDto) {
        UserInfoResponse responseDataDto = authService.saveUser(requestDto);
        return ResponseEntity.ok(responseDataDto);
    }

    // 회원가입 (관리자)
    @PostMapping("/admin")
    public ResponseEntity<UserInfoResponse> handleSignUpAdmin(@RequestBody SaveAdminRequest requestDto) {
        UserInfoResponse responseDataDto = authService.saveAdmin(requestDto);
        return ResponseEntity.ok(responseDataDto);
    }
}
