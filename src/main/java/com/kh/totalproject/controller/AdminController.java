/* 관리자 전용 페이지
*  유저, 게시글 에 관한 CRUD 중 Read 과 Delete 를 가능하게 하며
*  신고 글 과 건의사항 글에 대한 답변 가능 */

package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.response.ReportCommentResponse;
import com.kh.totalproject.dto.response.ReportResponse;
import com.kh.totalproject.dto.response.SuggestResponse;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/validate/admin")
    public ResponseEntity<Boolean> checkIfAdmin(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(adminService.adminChecker(authorizationHeader));
    }

    // 모든 유저를 페이지네이션 목록 요청 / 응답
    @GetMapping("/list/users")
    public ResponseEntity<Page<UserResponse>> listAllUserInfo(@RequestHeader("Authorization") String authorizationHeader,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "createdAt") String sortBy,
                                                              @RequestParam(defaultValue = "DESC") String order,
                                                              @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.listAllUserInfo(authorizationHeader, page, size, sortBy, order, search));
    }

    // 유저가 작성한 신고 글 목록 요청 / 응답
    @GetMapping("/list/report")
    public ResponseEntity<Page<ReportResponse>> listAllReport(@RequestHeader("Authorization") String authorizationHeader,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @RequestParam(defaultValue = "createdAt") String sortBy,
                                                              @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(adminService.listReportPost(authorizationHeader, page, size, sortBy, order));
    }

    // 유저가 작성한 건의사항 글 목록 요청 / 응답
    @GetMapping("/list/report")
    public ResponseEntity<Page<SuggestResponse>> listAllSuggestion(@RequestHeader("Authorization") String authorizationHeader,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                   @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(adminService.listSuggestionPost(authorizationHeader, page, size, sortBy, order));
    }

//    // 유저가 작성한 신고 글 삭제
//    @DeleteMapping("delete/report")
//    public ResponseEntity<Boolean> deleteReport(@RequestHeader("Authorization") String authorizationHeader,
//                                                @RequestParam Long reportId) {
//        return ResponseEntity.ok(adminService.deleteReport(authorizationHeader, reportId));
//    }
//
//    // 유저가 작성한 건의사항 글 삭제
//    @DeleteMapping("delete/suggestion")
//    public ResponseEntity<Boolean> deleteSuggestion(@RequestHeader("Authorization") String authorizationHeader,
//                                                    @RequestParam Long suggestionId) {
//        return ResponseEntity.ok(adminService.deleteSuggestion(authorizationHeader, suggestionId));
//    }
//
//    // 유저가 작성한 신고 글 관리자 답변 보기 요청 / 응답
//    @GetMapping("/list/report/comment")
//    public ResponseEntity<ReportCommentResponse> listReportReply(@RequestHeader("Authorization") String authorizationHeader) {
//        return ResponseEntity.ok(adminService.listReportReply(authorizationHeader));
//    }
//
//    // 유저가 작성한 건의사항 글 관리자 답변 보기 요청 / 응답
//    @GetMapping("/list/suggestion/comment")
//    public ResponseEntity<ReportCommentResponse> listSuggestionReply(@RequestHeader("Authorization") String authorizationHeader) {
//        return ResponseEntity.ok(adminService.listSuggestionReply(authorizationHeader));
//    }
//
//    // 신고 글에 대한 답변 요청 / 응답
//    @PostMapping("/reply/report")
//    public ResponseEntity<ReportCommentRequest> replyReport(@RequestHeader("Authorization") String authorizationHeader,
//                                                            @RequestParam Long reportId) {
//        return ResponseEntity.ok(adminService.replyReport(authorizationHeader, reportId));
//    }
//
//    // 건의사항 글에 대한 답변 요청 / 응답
//    @PostMapping("/reply/suggestion")
//    public ResponseEntity<ReportCommentRequest> replySuggestion(@RequestHeader("Authorization") String authorizationHeader,
//                                                                @RequestParam Long suggestionId) {
//        return ResponseEntity.ok(adminService.replySuggestion(authorizationHeader, suggestionId));
//    }
//
//    // 신고글에 대한 처리 글 삭제 기능
//    @DeleteMapping("/delete/post")
//    public ResponseEntity<Boolean> deletePost(@RequestHeader("Authorization") String authorizationHeader,
//                                              @RequestParam Long boardId) {
//        return ResponseEntity.ok(adminService.deletePost(authorizationHeader, boardId));
//    }
//
//    // 공지사항 글 작성
//    @GetMapping("/list/announcement")
//    public ResponseEntity<Page<?>> listAnnouncement() {
//        return ResponseEntity.ok(adminService.listAnnouncement());
//    }
//
//    // 공지사항글 작성 (필요한지 필요 없는지 유무 정하면 엔티티 만들어야함)
//    @PostMapping("/new/announcement")
//    public ResponseEntity<?> createAnnouncement(@RequestHeader("Authorization") String authorizationHeader) {
//        return ResponseEntity.ok(adminService.createAnnouncement(authorizationHeader));
//    }
//
//    // 공지사항 글 수정
//    @PutMapping("/modify/announcement")
//    public ResponseEntity<?> modifyAnnouncement(@RequestHeader("Authorization") String authorizationHeader,
//                                                @RequestParam Long announcementId) {
//        return ResponseEntity.ok(adminService.modifyAnnouncement(authorizationHeader, announcementId));
//    }
//
//    // 공지사항 글 삭제
//    @DeleteMapping("/delete/announcement")
//    public ResponseEntity<?> deleteAnnouncement(@RequestHeader("Authorization") String authorizationHeader,
//                                                @RequestParam Long announcementId) {
//        return ResponseEntity.ok(adminService.deleteAnnouncement(authorizationHeader, announcementId));
//    }
}