/* 회원정보 수정, 탈퇴 및 내 작성글, 댓글, 요청 전달 받는 컨트롤러
*  관리자 전용 페이지는 따로 컨트롤러 분리 할 예정 */
package com.kh.totalproject.controller;


import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/my")
public class UserController {
    private final MyPageService myPageService;

    // User 회원정보 보기 (전체)
    @GetMapping
    public ResponseEntity<List<UserResponse>> handleGetAllUsers() {
        List<UserResponse> responseDataDtoList = myPageService.getUserInfoAll();
        return ResponseEntity.ok(responseDataDtoList);
    }

    // User 회원정보 보기 (단일) (마이페이지)
    // exists 정보는 프론트에서 HEAD로 보내서 ok(200) not found(404) 인지를 확인하여 처리가 가능합니다.
    // HEAD 메서드로 요청하는 경우 백엔드는 GET으로 매핑하여 처리합니다.
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> handleGetUserById(@PathVariable("id") Long id) {
        UserResponse responseDataDto = myPageService.getUserInfo(id);
        return ResponseEntity.ok(responseDataDto);
    }
    
    // 회원 정보 수정
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    // 2가지 방법 1) FrontEnd 에서 Token 을 Decoding 하여 Id 추출하여 BackEnd 로 요청
    // 2) Token 만 Header 에 실어서 BackEnd 로 요청 후 BackEnd 에서 Decoding 하여 처리
    // 현재는 1번 방법
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> handleUpdateUser(@PathVariable("id") Long id, @RequestBody UserRequest requestDto) {
        UserResponse responseDataDto = myPageService.update(id, requestDto);
        return ResponseEntity.ok(responseDataDto);
    }
    
    // 회원 삭제
    // 실제로는 토큰이 인증된 사용자로 제한해야 합니다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> handleDeleteUser(@PathVariable("id") Long id) {
        UserResponse responseDataDto = myPageService.delete(id);
        return ResponseEntity.ok(responseDataDto);
    }
}
