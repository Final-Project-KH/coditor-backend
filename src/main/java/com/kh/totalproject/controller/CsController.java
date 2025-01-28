package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/customerService")
public class CsController {
    private final CsService csService;

    // 신고 게시글 작성 요청 / 응답
    @PostMapping("/report/new")
    ResponseEntity<Boolean> CreateReportPost(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody ReportRequest reportRequest) {
        return ResponseEntity.ok(csService.createReportPost(authorizationHeader, reportRequest));
    }

    // 건의사항 게시글 작성 요청 / 응답
    @PostMapping("/suggestion/new")
    ResponseEntity<Boolean> CreateSuggestionPost(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody SuggestRequest suggestRequest) {
        return ResponseEntity.ok(csService.createSuggestionPost(authorizationHeader, suggestRequest));
    }

    // CS 댓글 작성 (Admin 페이지에서도 똑같이 구현 필요)
    // CS 댓글 조회 (Admin 페이지에서도 똑같이 구현 필요)
    // CS 댓글 수정 (Admin 페이지에서도 똑같이 구현 필요)
    // CS 댓글 삭제 (Admin 페이지에서도 똑같이 구현 필요)
}
