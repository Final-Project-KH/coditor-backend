/* 각각 커뮤니티에 해당하는 게시글 보기, 게시글 작성, 게시글 수정 등등 요청과 응답에 필요한 컨트롤러 */
package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.BoardRequest;
import com.kh.totalproject.dto.response.BoardResponse;
import com.kh.totalproject.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/community")
public class CommunityController {
    private final CommunityService communityService;

    // 게시판별 단일 글 작성시 게시판 type 을 전달 받아 서비스에서 해당 로직으로 연결
    @PostMapping("/new/post")
    ResponseEntity<Boolean> createPost(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody BoardRequest boardRequest,
                                       @RequestParam String boardType) {
        return ResponseEntity.ok(communityService.createPost(authorizationHeader, boardRequest, boardType));
    }

    @PutMapping("/modify/post")
    ResponseEntity<Boolean> modifyPost(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestBody BoardRequest boardRequest,
                                       @RequestParam String boardType) {
        return ResponseEntity.ok(communityService.modifyPost(authorizationHeader, boardRequest, boardType));
    }

    // 게시판별 전체 게시글 조회시 게시판 type 을 전달 받아 서비스에서 해당 로직으로 연결
    @GetMapping("/list/all")
    ResponseEntity<Page<BoardResponse>> listAll(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam String boardType,
                                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                                @RequestParam(defaultValue = "DESC") String order) {
        return ResponseEntity.ok(communityService.listAllByBoardTypeWithSort(page, size, boardType, sortBy, order));
    }

    // 게시판별 단일 게시글 조회시 게시판 type 을 전달 받아 서비스에서 해당 로직으로 연결
    @GetMapping("/list/one")
    ResponseEntity<BoardResponse> listOne(@RequestParam long id, @RequestParam String boardType) {
        return ResponseEntity.ok(communityService.listOneByBoardType(id, boardType));
    }
}
