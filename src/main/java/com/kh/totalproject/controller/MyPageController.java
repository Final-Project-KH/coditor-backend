/* 회원정보 수정, 탈퇴 및 내 작성글, 댓글, 요청 전달 받는 컨트롤러
*  관리자 전용 페이지는 따로 컨트롤러 분리 할 예정 */
package com.kh.totalproject.controller;


import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.BoardResponse;
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
public class MyPageController {
    private final MyPageService myPageService;

    // 내 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> myProfile(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("헤더를 통해 들어온 토큰 값 {}", authorizationHeader);
        return ResponseEntity.ok(myPageService.listMyProfile(authorizationHeader));
    }

    // 내 정보 수정, 아마 버튼 클릭시 조회에서 바로 수정 입력칸 받을 수 있게
    @PutMapping("/profile-modify")
    public ResponseEntity<Boolean> modifyMember(@RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(myPageService.modifyMember(userRequest));
    }

    // 내 정보에서 비밀번호 수정
    @PutMapping("/profile-changePw")
    public ResponseEntity<Boolean> changePw(@RequestHeader Long id, @RequestParam String inputPw, @RequestParam String newPw) {
        return ResponseEntity.ok(myPageService.changePw(id, inputPw, newPw));
    }

    // 내 정보에서 내 글 보기
    @GetMapping("/post/page")
    public ResponseEntity<List<BoardResponse>> listMyPosts(@RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(myPageService.myPostList(size));
    }
}
