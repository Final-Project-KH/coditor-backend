/* 회원정보 수정, 탈퇴 및 내 작성글, 댓글, 요청 전달 받는 컨트롤러
*  관리자 전용 페이지는 따로 컨트롤러 분리 할 예정 */
package com.kh.totalproject.controller;


import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Boolean> modifyUserInfo(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(myPageService.modifyMyProfile(authorizationHeader, userRequest));
    }

    // 내 정보에서 비밀번호 수정
    @PutMapping("/profile-changePw")
    public ResponseEntity<Boolean> changePw(@RequestHeader("Authorization") String authorizationHeader,
                                            @RequestParam String inputPw,
                                            @RequestParam String newPw) {
        return ResponseEntity.ok(myPageService.changePw(authorizationHeader, inputPw, newPw));
    }

    // 내 정보에서 내 글 보기
    @GetMapping("/post/list")
    public ResponseEntity<Page<BoardResponse>> listMyPost(@RequestHeader("Authorization") String authorizationHeader,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String sortBy,
                                                          @RequestParam(required = false) String order) {
        return ResponseEntity.ok(myPageService.myPost(authorizationHeader, page, size, sortBy, order));
    }

    @GetMapping("/report/list")
    public ResponseEntity<Page<ReportResponse>> listMyReportPost(@RequestHeader("Authorization") String authorizationHeader,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(required = false) String sortBy,
                                                                 @RequestParam(required = false) String order) {
        return ResponseEntity.ok(myPageService.myReportList(authorizationHeader, page, size, sortBy, order));
    }

    @GetMapping("/suggestion/list")
    public ResponseEntity<Page<SuggestResponse>> listMySuggestionPost(@RequestHeader("Authorization") String authorizationHeader,
                                                                      @RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "10") int size,
                                                                      @RequestParam(required = false) String sortBy,
                                                                      @RequestParam(required = false) String order) {
        return ResponseEntity.ok(myPageService.mySuggestionList(authorizationHeader, page, size, sortBy, order));
    }

    @GetMapping("/listOne/reportPost")
    public ResponseEntity<ReportResponse> MyReportPost(@RequestHeader("Authorization") String authorizationHeader,
                                                              @RequestParam long id) {
        return ResponseEntity.ok(myPageService.myReportPost(authorizationHeader, id));
    }

    @GetMapping("/listOne/suggestionPost")
    public ResponseEntity<SuggestResponse> MySuggestionPost(@RequestHeader("Authorization") String authorizationHeader,
                                                              @RequestParam long id) {
        return ResponseEntity.ok(myPageService.mySuggestionPost(authorizationHeader, id));
    }

    @GetMapping("/modify/reportPort")
    public ResponseEntity<Boolean> modifyMyReport(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestBody ReportRequest reportRequest) {
        return ResponseEntity.ok(myPageService.modifyMyReport(authorizationHeader, reportRequest));
    }

    @GetMapping("/modify/suggestionPort")
    public ResponseEntity<Boolean> modifyMySuggestion(@RequestHeader("Authorization") String authorizationHeader,
                                                      @RequestParam SuggestRequest suggestRequest) {
        return ResponseEntity.ok(myPageService.modifyMySuggestion(authorizationHeader, suggestRequest));
    }
}
