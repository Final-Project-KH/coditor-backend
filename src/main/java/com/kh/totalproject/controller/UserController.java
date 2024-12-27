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
    public ResponseEntity<UserInfoResponse> handleSignUp(@RequestBody SaveUserRequest requestDto) {
        UserInfoResponse responseDataDto = userService.saveUser(requestDto);
        return ResponseEntity.ok(responseDataDto);
    }
    
    // User 회원정보 보기
    @GetMapping
    public ResponseEntity<List<UserInfoResponse>> handleGetAllUsers() {
        List<UserInfoResponse> responseDataDtoList = userService.getUserInfoAll();
        return ResponseEntity.ok(responseDataDtoList);
    }

    // exists 정보는 프론트에서 HEAD로 보내서 ok(200) not found(404) 인지를 확인하여 처리가 가능합니다.
    // HEAD 메서드로 요청하는 경우 백엔드는 GET으로 매핑하여 처리합니다.
    @GetMapping("/{id}")
    public ResponseEntity<UserInfoResponse> handleGetUserById(@PathVariable("id") Long id) {
        UserInfoResponse responseDataDto = userService.getUserInfo(id);
        return ResponseEntity.ok(responseDataDto);
    }
    
    // 회원 정보 수정
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    @PutMapping("/{id}")
    public ResponseEntity<UserInfoResponse> handleUpdateUser(@PathVariable("id") Long id, @RequestBody SaveUserRequest requestDto) {
        UserInfoResponse responseDataDto = userService.update(id, requestDto);
        return ResponseEntity.ok(responseDataDto);
    }
    
    // 회원 삭제
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> handleDeleteUser(@PathVariable("id") Long id) {
        UserInfoResponse responseDataDto = userService.delete(id);
        return ResponseEntity.ok(responseDataDto);
    }
}
