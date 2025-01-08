package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.GoogleLoginRequest;
import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.request.SaveAdminRequest;
import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.service.AuthService;
import com.kh.totalproject.service.GoogleService;
import com.kh.totalproject.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j  // 로깅을 위한 Lombok 어노테이션, 클래스에서 로그를 쉽게 사용할 수 있도록 함
@CrossOrigin(origins = "http://localhost:3000")  // React 개발 서버에서 오는 요청을 허용 (CORS 설정)
@RestController  // RESTful 웹 서비스를 위한 컨트롤러 어노테이션, JSON 형식의 응답을 반환
@RequestMapping("/auth")  // 이 컨트롤러의 모든 메서드는 "/auth" 경로 하에 매핑됨
@RequiredArgsConstructor  // Lombok 어노테이션, final 필드나 @NonNull 필드를 자동으로 생성자에 주입
public class GoogleController {

    private final GoogleService googleService;  // GoogleService 의존성 주입 (구글 로그인 관련 서비스)

    // 구글 로그인 API 처리 메서드
    @PostMapping("/googlelogin")  // POST 요청 "/auth/googlelogin"에 매핑
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> data) {
        String googleToken = data.get("token");  // 요청 데이터에서 구글 토큰을 추출

        if (googleToken == null) {  // 구글 토큰이 없으면 에러 응답
            return ResponseEntity.badRequest().body(Map.of("error", "구글 토큰이 누락되었습니다."));
        }
        try {
            // GoogleService에서 구글 로그인 처리
            TokenResponse tokenResponse = googleService.loginWithGoogle(googleToken);

            // 로그인 성공 시 응답 반환
            Map<String, String> result = new HashMap<>();
            result.put("grantType", "Bearer");  // 인증 타입은 "Bearer"
            result.put("accessToken", tokenResponse.getAccessToken());  // 액세스 토큰 반환
            result.put("refreshToken", tokenResponse.getRefreshToken());  // 리프레시 토큰 반환

            return ResponseEntity.ok(result);  // 성공적인 응답 반환
        } catch (Exception e) {  // 구글 인증 실패 시
            log.error("구글 로그인 처리 중 오류 발생: {}", e.getMessage());  // 오류 로깅
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "구글 인증 실패"));  // 인증 실패 응답
        }
    }

    // 구글 로그인 API 처리 메서드 (DTO 사용)
    @PostMapping("/google")  // POST 요청 "/auth/google"에 매핑
    public TokenResponse googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {
        String googleToken = googleLoginRequest.getToken();  // GoogleLoginRequest에서 구글 토큰 추출
        return googleService.loginWithGoogle(googleToken);  // 구글 로그인 처리 후 TokenResponse 반환
    }

    // 공통 에러 핸들링 로직 (HttpClientErrorException 예외 처리)
    @ExceptionHandler(HttpClientErrorException.class)  // HttpClientErrorException 예외가 발생하면 호출됨
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException e) {
        log.error("HttpClientErrorException 발생: {}", e.getMessage());  // 예외 메시지 로깅
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());  // 예외 상태 코드와 메시지 반환
    }

    // 공통 에러 핸들링 로직 (IllegalArgumentException 예외 처리)
    @ExceptionHandler(IllegalArgumentException.class)  // IllegalArgumentException 예외가 발생하면 호출됨
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException 발생: {}", e.getMessage());  // 예외 메시지 로깅
        return ResponseEntity.badRequest().body(e.getMessage());  // 잘못된 요청에 대한 응답 반환
    }
}