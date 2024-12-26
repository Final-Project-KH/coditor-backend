package com.kh.totalproject.controller;


import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.ApiResponse;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 회원 정보 보기 등은 나중에 url 나눠야함
// 로그인 창, 회원가입 창 url 분리 (마이페이지 또한)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<UserInfoResponse>> handleSignUp(@RequestBody SaveUserRequest requestDto) {
        UserInfoResponse responseDataDto = userService.saveUser(requestDto);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>(true, "회원가입 성공", responseDataDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // User 회원정보 보기
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> handleGetAllUsers() {
        List<UserInfoResponse> responseDataDtoList = userService.getUserInfoAll();
        ApiResponse<List<UserInfoResponse>> response = new ApiResponse<>(true, "모든 회원정보 조회 결과", responseDataDtoList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // exists 정보는 프론트에서 HEAD로 보내서 ok(200) not found(404) 인지를 확인하여 처리가 가능합니다.
    // HEAD 메서드로 요청하는 경우 백엔드는 GET으로 매핑하여 처리합니다.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> handleGetUserById(@PathVariable Long id) {
        UserInfoResponse responseDataDto = userService.getUserInfo(id);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>(true, "회원정보 조회 결과", responseDataDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    // 회원 정보 수정
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> handleUpdateUser(@PathVariable Long id, @RequestBody SaveUserRequest requestDto) {
        UserInfoResponse responseDataDto = userService.update(id, requestDto);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>(true, "회원 정보 수정 성공", responseDataDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    // 회원 삭제
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> handleDeleteUser(@PathVariable Long id) {
        UserInfoResponse responseDataDto = userService.delete(id);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>(true, "회원 삭제 성공", responseDataDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
