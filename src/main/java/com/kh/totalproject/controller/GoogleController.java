package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.GoogleLoginRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.service.GoogleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")  // React 개발 서버에서 오는 요청을 허용 (CORS 설정)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;  // GoogleService 의존성 주입

    // 구글 로그인 처리 (DTO 사용)
    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {
        String googleToken = googleLoginRequest.getToken();  // DTO에서 구글 토큰 추출
        if (googleToken == null || googleToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "구글 토큰이 누락되었습니다."));
        }

        try {
            TokenResponse tokenResponse = googleService.login(googleToken);  // 구글 로그인 처리
            Map<String, String> result = new HashMap<>();
            result.put("grantType", "Bearer");
            result.put("accessToken", tokenResponse.getAccessToken());
            result.put("refreshToken", tokenResponse.getRefreshToken());
            result.put("isNewUser", String.valueOf(tokenResponse.isNewUser())); // isNewUser 값 추가
            return ResponseEntity.ok(result);  // 성공적인 응답 반환
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "구글 인증 실패"));
        }
    }

    // 공통 에러 핸들링 로직 (HttpClientErrorException 예외 처리)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException e) {
        log.error("HttpClientErrorException 발생: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }

    // 공통 에러 핸들링 로직 (IllegalArgumentException 예외 처리)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException 발생: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
