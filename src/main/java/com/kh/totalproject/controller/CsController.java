package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.SuggestionCommentRequest;
import com.kh.totalproject.dto.response.ReportCommentResponse;
import com.kh.totalproject.dto.response.SuggestionCommentResponse;
import com.kh.totalproject.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/customerService")
public class CsController {
    private final CsService csService;

    // 신고 게시글 작성 요청 / 응답
    @PostMapping("/new/reportPost")
    ResponseEntity<Boolean> createReportPost(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody ReportRequest reportRequest) {
        return ResponseEntity.ok(csService.createReportPost(authorizationHeader, reportRequest));
    }

    // 건의사항 게시글 작성 요청 / 응답
    @PostMapping("/new/suggestionPost")
    ResponseEntity<Boolean> createSuggestionPost(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody SuggestRequest suggestRequest) {
        return ResponseEntity.ok(csService.createSuggestionPost(authorizationHeader, suggestRequest));
    }

    // CS 댓글 조회 (Admin 페이지에서도 똑같이 구현 필요)
    // 신고 글에 댓글 조회  (Page 타입이 아닌 그냥 하나만 볼 수있게 변경 필요)
    @GetMapping("/list/reportComment")
    ResponseEntity<Page<ReportCommentResponse>> listReportComment(@RequestHeader("Authorization") String authorizationHeader,
                                                                  @RequestParam Long reportId,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                  @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(csService.listReportComment(authorizationHeader, reportId, page, size, sortBy, order));
    }

    // CS 댓글 조회 (Admin 페이지에서도 똑같이 구현 필요)
    // 건의사항 글에 댓글 조회 (Page 타입이 아닌 그냥 하나만 볼 수있게 변경 필요)
    @GetMapping("/list/suggestionComment")
    ResponseEntity<Page<SuggestionCommentResponse>> listSuggestionComment(@RequestHeader("Authorization") String authorizationHeader,
                                                                          @RequestParam Long suggestionId,
                                                                          @RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                          @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(csService.listSuggestionComment(authorizationHeader, suggestionId, page, size, sortBy, order));
    }

//    // CS 댓글 작성 (Admin 페이지에서도 똑같이 구현 필요)
//    // 신고 글에 댓글 작성 (답변 형식으로 바뀐다면 필요 없어지므로 정리)
//    @PostMapping("/new/reportComment")
//    ResponseEntity<Boolean> addReportComment(@RequestHeader("Authorization") String authorizationHeader,
//                                             @RequestBody ReportCommentRequest reportCommentRequest) {
//        return ResponseEntity.ok(csService.AddReportComment(authorizationHeader, reportCommentRequest));
//    }
//
//    // CS 댓글 작성 (Admin 페이지에서도 똑같이 구현 필요)
//    // 건의사항 글에 댓글 작성 (답변 형식으로 바뀐다면 필요 없어지므로 정리)
//    @PostMapping("/new/suggestionComment")
//    ResponseEntity<Boolean> addSuggestionComment(@RequestHeader("Authorization") String authorizationHeader,
//                                             @RequestBody SuggestionCommentRequest suggestionCommentRequest) {
//        return ResponseEntity.ok(csService.AddSuggestionComment(authorizationHeader, suggestionCommentRequest));
//    }
//
//    // CS 댓글 수정 (Admin 페이지에서도 똑같이 구현 필요)
//    // 신고 글에 댓글 수정
//    @PutMapping("/modify/reportComment")
//    ResponseEntity<Boolean> modifyReportComment(@RequestHeader("Authorization") String authorizationHeader,
//                                                @RequestBody ReportCommentRequest reportCommentRequest) {
//        return ResponseEntity.ok(csService.ModifyReportComment(authorizationHeader, reportCommentRequest));
//    }
//
//    // CS 댓글 수정 (Admin 페이지에서도 똑같이 구현 필요)
//    // 건의사항 글에 댓글 수정
//    @PutMapping("/modify/suggestionComment")
//    ResponseEntity<Boolean> modifySuggestionComment(@RequestHeader("Authorization") String authorizationHeader,
//                                                    @RequestBody SuggestionCommentRequest suggestionCommentRequest) {
//        return ResponseEntity.ok(csService.ModifySuggestionComment(authorizationHeader, suggestionCommentRequest));
//    }
//
//    // CS 댓글 삭제 (Admin 페이지에서도 똑같이 구현 필요)
//    // 신고 글에 댓글 삭제
//    @DeleteMapping("/delete/reportComment")
//    ResponseEntity<Boolean> deleteReportComment(@RequestHeader("Authorization") String authorizationHeader,
//                                                @RequestParam Long commentId) {
//        return ResponseEntity.ok(csService.DeleteReportPost(authorizationHeader, commentId));
//    }
//
//    // CS 댓글 삭제 (Admin 페이지에서도 똑같이 구현 필요)
//    // 건의사항 글에 댓글 삭제
//    @DeleteMapping("/delete/suggestionComment")
//    ResponseEntity<Boolean> deleteSuggestionComment(@RequestHeader("Authorization") String authorizationHeader,
//                                                    @RequestParam Long commentId) {
//        return ResponseEntity.ok(csService.DeleteSuggestionPost(authorizationHeader, commentId));
//    }
}
