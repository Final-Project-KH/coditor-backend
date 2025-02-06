/* 관리자 전용 페이지
*  모든 Controller 작용에 Role.Admin 인지 체크
*  유저, 게시글 에 관한 CRUD 중 Read 과 Delete 를 가능하게 하며
*  신고 글 과 건의사항 글에 대한 답변 가능 Create */

package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.request.SuggestionCommentRequest;
import com.kh.totalproject.service.AdminService;
import com.kh.totalproject.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    // 모든 유저를 페이지네이션 목록 요청 / 응답
    @GetMapping("/list/users")
    public ResponseEntity<?> listAllUserInfo(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "registeredAt") String sortBy,
                                             @RequestParam(defaultValue = "DESC") String order,
                                             @RequestParam(required = false) String search) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.listAllUserInfo(page, size, sortBy, order, search));
    }

    // 유저가 작성한 신고 글 목록 요청 / 응답
    @GetMapping("/list/report")
    public ResponseEntity<?> listAllReport(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "createdAt") String sortBy,
                                           @RequestParam(defaultValue = "DESC") String order,
                                           @RequestParam(defaultValue = "ACTIVE") String status) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.listReportPost(page, size, sortBy, order, status));
    }

    // 유저가 작성한 건의사항 글 목록 요청 / 응답
    @GetMapping("/list/suggestion")
    public ResponseEntity<?> listAllSuggestion(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(defaultValue = "createdAt") String sortBy,
                                               @RequestParam(defaultValue = "DESC") String order,
                                               @RequestParam(defaultValue = "ACTIVE")String status) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.listSuggestionPost(page, size, sortBy, order, status));
    }

    // 유저가 작성한 신고 글 삭제 요청 / 응답 (관리자 권한)
    @DeleteMapping("delete/report")
    public ResponseEntity<?> deleteReport(@RequestParam Long reportId) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.deleteReportPost(reportId));
    }

    // 유저가 작성한 건의사항 글 삭제 요청 / 응답 (관리자 권한)
    @DeleteMapping("delete/suggestion")
    public ResponseEntity<?> deleteSuggestion(@RequestParam Long suggestionId) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.deleteSuggestionPost(suggestionId));
    }

    // 유저가 작성한 신고 글 관리자 답변 보기 요청 / 응답
    @GetMapping("/list/report/comment")
    public ResponseEntity<?> listReportReply(@RequestParam Long reportId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "createdAt") String sortBy,
                                             @RequestParam(defaultValue = "DESC") String order) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.listReportReply(reportId, page, size, sortBy, order));
    }

    // 유저가 작성한 건의사항 글 관리자 답변 보기 요청 / 응답
    @GetMapping("/list/suggestion/comment")
    public ResponseEntity<?> listSuggestionReply(@RequestParam Long suggestionId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                                 @RequestParam(defaultValue = "DESC") String order) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.listSuggestionReply(suggestionId, page, size, sortBy, order));
    }

    // 신고 글에 대한 답변 요청 / 응답
    @PostMapping("/reply/report")
    public ResponseEntity<?> replyReport(ReportCommentRequest reportCommentRequest) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.replyReport(reportCommentRequest));
    }

    // 건의사항 글에 대한 답변 요청 / 응답
    @PostMapping("/reply/suggestion")
    public ResponseEntity<?> replySuggestion(SuggestionCommentRequest suggestionCommentRequest) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.replySuggestion(suggestionCommentRequest));
    }

    // 신고글에 대한 처리 글 삭제 기능
    @DeleteMapping("/delete/post")
    public ResponseEntity<?> deletePost(@RequestParam Long boardId) {
        if (!SecurityUtil.isAdminUser()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("관리자 권한이 필요 합니다.");
        }
        return ResponseEntity.ok(adminService.deletePost(boardId));
    }
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