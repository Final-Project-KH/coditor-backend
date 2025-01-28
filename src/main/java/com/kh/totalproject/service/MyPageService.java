package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.repository.BoardRepository;
import com.kh.totalproject.repository.ReportRepository;
import com.kh.totalproject.repository.SuggestionRepository;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
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
    private final SuggestionRepository suggestionRepository;
    private final JwtUtil jwtUtil;

    // 내 정보 보기
    public UserResponse listMyProfile(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return UserResponse.ofAll(user);
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
    public Page<BoardResponse> myPost(String authorizationHeader, int page, int size, String sortBy, String order) {
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
        Page<Board> boards = boardRepository.findByUserKey(user.getUserKey(), pageable);
        return boards.map(BoardResponse::ofMyPost);
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

    public Boolean modifyMyReport(String authorizationHeader, ReportRequest reportRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
            ReportBoard reportBoard = reportRepository.findById(reportRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재 하지 않습니다."));
            if (!reportBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("당신은 이 글에 수정 권한이 없습니다.");
            }
            ReportBoard updatedBoard = reportRequest.toModifyReportPost(reportBoard);
            reportRepository.save(updatedBoard);
        } catch (AccessDeniedException e) {
            return false;
        }
        return true;
    }

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
}