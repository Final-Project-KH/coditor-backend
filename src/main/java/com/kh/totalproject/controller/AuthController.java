/* 로그인에 필요한 요청과 응답을 담당하는 Auth Controller, JWT 의 인증 범위 내에 해당 */
package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.dto.request.AdminRequest;
import com.kh.totalproject.dto.request.TokenRequest;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.TokenResponse;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.exception.HiJackingException;
import com.kh.totalproject.service.AuthService;
import com.kh.totalproject.service.GoogleService;
import com.kh.totalproject.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
// /auth 경로는 인증 요구 해제
// 회원가입 또한 /auth 경로로 이동해야함
public class AuthController {
    private final AuthService authService;

    // 로그인 요청, 응답 (Access, Refresh Token 전달)
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> signIn(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.logIn(loginRequest));
    }

    // 엑세스 토큰 만료시 요청, 응답
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestBody TokenRequest tokenRequest) {
        try {
            TokenResponse tokenResponseDto = authService.reissueToken(tokenRequest);
            log.info("서비스로직 시작 : {}", tokenResponseDto);
            return ResponseEntity.ok(tokenResponseDto);
        } catch (HiJackingException e) {
            // 반환 타입을 맞추기 위해 ResponseEntity<?>로 설정
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("해당 사용자의 Refresh Token 이 일치하지 않습니다.");
        }
    }

    // 회원가입 (관리자)
    @PostMapping("/admin")
    public ResponseEntity<UserResponse> handleSignUpAdmin(@RequestBody AdminRequest adminRequest) {
        UserResponse responseDataDto = authService.saveAdmin(adminRequest);
        return ResponseEntity.ok(responseDataDto);
    }

    // 회원가입에 필요한 정보를 요청, 응답
    @PostMapping("/join")
    public ResponseEntity<Boolean> join(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(authService.signUp(userRequest));
    }

    // 회원가입시 존재여부 유효성 검사
    @PostMapping("/join/validate")
    public ResponseEntity<Boolean> validateForInfo(@RequestParam String key, @RequestParam String value) {
        return ResponseEntity.ok(authService.validationForInfo(key, value));
    }

    // 회원가입중 이메일 유효성 검사 요청 및 OTP 응답 *재전송시 해당 컨트롤러 재요청*
    @PostMapping("/join/verify")
    public ResponseEntity<Boolean> sendOtpForJoin(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendOtpForJoin(email));
    }

    // 회원가입중 OTP 유효성 검사 요청, 응답
    @PostMapping("/join/{otp}/{email}")
    public ResponseEntity<Boolean> otpValidationForJoin(@PathVariable Integer otp, @PathVariable String email) {
        return ResponseEntity.ok(authService.validateOtpForJoin(otp, email));
    }

    // 아이디 찾기에 필요한 정보를 요청, 응답
    @PostMapping("/forgotid")
    public ResponseEntity<UserResponse> findId(@RequestParam String email) {
        return ResponseEntity.ok(authService.getIdByEmail(email));
    }

    // 비밀번호 찾기시 이메일 유효성 검사 요청 및 OTP 응답
    @PostMapping("/forgotpw/{email}")
    public ResponseEntity<Boolean> sendOtpForPwFind(@PathVariable String email) {
        return ResponseEntity.ok(authService.sendOtpForPasswordReset(email));
    }

    // 비밀번호 찾기시 전달 받은 OTP 유효성 체크
    @PostMapping("/forgotpw/{otp}/{email}")
    public ResponseEntity<Boolean> otpValidationForPwFind(@PathVariable Integer otp, @PathVariable String email) {
        return ResponseEntity.ok(authService.validateOtpForPw(otp, email));
    }

    // 비밀번호 찾기 첫번째 페이지에서 OTP 인증 후 비밀번호 재설정에 필요한 정보를 요청, 응답 컨트롤러
    @PutMapping("/resetpw/{email}")
    public ResponseEntity<Boolean> resetPw(@PathVariable String email, @RequestParam String newPw) {
        return ResponseEntity.ok(authService.resetPassword(email, newPw));
    }

    private final GoogleService googleService;
    private final KakaoService kakaoService;

    @PostMapping("/google")
    public TokenResponse googleLogin(@RequestBody String idToken) {
        return googleService.login(idToken);
    }

    @PostMapping("/kakao")
    public TokenResponse kakaoLogin(@RequestBody String accessToken) {
        return kakaoService.login(accessToken);
    }
}
