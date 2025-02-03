package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import com.kh.totalproject.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final BoardReactionRepository boardReactionRepository;
    private final SuggestionRepository suggestionRepository;
    private final JwtUtil jwtUtil;

    // 내 정보 보기
    public UserResponse listMyProfile(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        int postCntByUser = (int) boardRepository.countByUserUserKey(user.getUserKey());
        return UserResponse.ofMyProfile(user, postCntByUser);
    }

    // 내 정보 수정
    public boolean modifyMyProfile(String authorizationHeader, UserRequest userRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 기존에 가지고 있던 정보로 유저 검색
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);
            User existingData = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
            User updatedData = userRequest.toModifyProfile(existingData);
            userRepository.save(updatedData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 내 비밀번호 변경
    public boolean changePw(String authorizationHeader, String inputPw, String newPw) {
        String token = authorizationHeader.replace("Bearer ", "");
        // 토큰에서 인증 정보 확인
        jwtUtil.getAuthentication(token);
        // Access 토큰에서 id 추출
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
        if (!passwordEncoder.matches(inputPw, user.getPassword())) {
            return false;
        } else {
            String newHashedPw = passwordEncoder.encode(newPw);
            user.setPassword(newHashedPw);
            userRepository.save(user);
            return true;
        }
    }

    //     내 작성글 보기,
    //     내정보에서 열람을 할 수 있는 페이지 네이션으로 설정
    //     BoardId 값을 반환하기 때문에 단일 게시글에 접근을 할 때는 CommunityService 에서 listOne 에 해당하는 메서드를 호출 해야함
    public Page<BoardResponse> myPost(int page, int size, String sortBy, String order) {
        Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userKey)
                .orElseThrow(() -> new ForbiddenException("해당 유저를 찾을 수 없습니다."));

        // 정렬시 기본값 설정, 페이지에 처음 접근할때
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";  // 기본적으로 최신순
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";  // 기본적으로 내림차순
        }

        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Board> boards = boardRepository.findByUserKey(user.getUserKey(), pageable);

        // 게시글 목록을 BoardResponse로 변환하여 반환
        return boards.map(board -> {

            // 각 게시글에 대한 댓글, 좋아요, 싫어요 수 가져오기
            int commentCnt = commentRepository.countCommentsByBoardId(board.getId()); // 수정
            int likeCnt = boardReactionRepository.countLikesByBoardId(board.getId()); // 수정
            int dislikeCnt = boardReactionRepository.countDislikesByBoardId(board.getId()); // 수정

            // BoardResponse로 변환하여 반환
            return BoardResponse.ofPost(board, commentCnt, likeCnt, dislikeCnt);
        });
    }

    public Page<ReportResponse> myReportList(String authorizationHeader, int page, int size, String sortBy, String order) {
        String token = authorizationHeader.replace("Bearer ", "");
        // 정렬시 기본값 설정, 페이지에 처음 접근할때
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";  // 기본적으로 최신순
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";  // 기본적으로 내림차순
        }
        // 토큰에서 인증 정보 확인
        jwtUtil.getAuthentication(token);
        // Access 토큰에서 id 추출
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<ReportBoard> reportBoards = reportRepository.findByUserKey(user.getUserKey(), pageable);
        return reportBoards.map(ReportResponse::ofReportPostList);
    }

    public Page<SuggestResponse> mySuggestionList(String authorizationHeader, int page, int size, String sortBy, String order) {
        String token = authorizationHeader.replace("Bearer ", "");
        // 정렬시 기본값 설정, 페이지에 처음 접근할때
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";  // 기본적으로 최신순
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";  // 기본적으로 내림차순
        }
        // 토큰에서 인증 정보 확인
        jwtUtil.getAuthentication(token);
        // Access 토큰에서 id 추출
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<SuggestionBoard> suggestionBoards = suggestionRepository.findByUserKey(user.getUserKey(), pageable);
        return suggestionBoards.map(SuggestResponse::ofSuggestionPostList);
    }

    public ReportResponse myReportPost(String authorizationHeader, long id) {
        String token = authorizationHeader.replace("Bearer ", "");
        // 토큰에서 인증 정보 확인
        jwtUtil.getAuthentication(token);
        // Access 토큰에서 id 추출
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
        ReportBoard reportBoard = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글은 유효하지 않습니다."));
        if (!Objects.equals(user.getUserKey(), reportBoard.getUser().getUserKey())) {
            throw new AccessDeniedException("당신은 이 글에 대한 열람 권한이 없습니다.");
        }
        return ReportResponse.ofOneReportPost(reportBoard);
    }

    public SuggestResponse mySuggestionPost(String authorizationHeader, long id) {
        String token = authorizationHeader.replace("Bearer ", "");
        // 토큰에서 인증 정보 확인
        jwtUtil.getAuthentication(token);
        // Access 토큰에서 id 추출
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
        SuggestionBoard suggestionBoard = suggestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글은 유효하지 않습니다."));
        if (!Objects.equals(user.getUserKey(), suggestionBoard.getUser().getUserKey())) {
            throw new AccessDeniedException("당신은 이 글에 대한 열람 권한이 없습니다.");
        }
        return SuggestResponse.ofOneSuggestionPost(suggestionBoard);
    }

    // 내 신고 글 수정 메서드
    public Boolean modifyMyReport(String authorizationHeader, ReportRequest reportRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다"));

            // reportId와 boardId로 기존 레코드를 찾아 수정
            ReportBoard reportBoard = reportRepository.findByBoardIdAndReportId(reportRequest.getBoardId(), reportRequest.getReportId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

            // 수정 권한 체크
            if (!reportBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("당신은 이 글에 수정 권한이 없습니다.");
            }

            // 수정할 데이터를 기존 데이터에 반영하여 업데이트
            ReportBoard updatedBoard = reportRequest.toModifyReportPost(reportBoard);

            // 기존 레코드를 수정하기 전에 업데이트된 report 의 ID가 올바른지 확인
            if (updatedBoard.getId() == null) {
                updatedBoard.setId(reportBoard.getId()); // 기존 ID를 설정
            }

            // 기존 레코드를 수정
            reportRepository.save(updatedBoard);

            return true;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    // 내 건의사항 글 수정
    public Boolean modifyMySuggestion(String authorizationHeader, SuggestRequest suggestRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestRequest.getSuggestionId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재 하지 않습니다."));
            if (!suggestionBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("당신은 이 글에 수정 권한이 없습니다.");
            }
            SuggestionBoard updatedBoard = suggestRequest.toModifySuggestionPost(suggestionBoard);
            suggestionRepository.save(updatedBoard);
        } catch (AccessDeniedException e) {
            return false;
        }
        return true;
    }

    // 내 신고 글 삭제
    public Boolean deleteMyReportPost(String authorizationHeader, Long reportId) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
            ReportBoard reportBoard = reportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            if (!reportBoard.getUser().getUserKey().equals(user.getUserKey())){
                throw new AccessDeniedException("당신은 이 글을 삭제 할 권한이 없습니다.");
            }
            reportRepository.deleteById(reportId);
        } catch (AccessDeniedException e) {
            return false;
        }
        return true;
    }

    // 내 건의사항 글 삭제
    public Boolean deleteMySuggestionPost(String authorizationHeader, Long suggestionId) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            if (!suggestionBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("당신은 이 글을 삭제 할 권한이 없습니다.");
            }
            suggestionRepository.deleteById(suggestionId);
        } catch (AccessDeniedException e) {
            return false;
        }
        return true;
    }
}