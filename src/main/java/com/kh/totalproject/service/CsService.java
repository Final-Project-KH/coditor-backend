package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.entity.Board;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.BoardRepository;
import com.kh.totalproject.repository.ReportRepository;
import com.kh.totalproject.repository.SuggestionRepository;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CsService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;
    private final SuggestionRepository suggestionRepository;
    private final JwtUtil jwtUtil;

    // 게시글 생성 서비스
    public Boolean createReportPost(String authorizationHeader, ReportRequest reportRequest) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. "));

            Board board = boardRepository.findById(reportRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다"));

            boolean alreadyReported = reportRepository.existsByUserAndBoard(user, board);
            if (alreadyReported) {
                throw new IllegalArgumentException("이미 신고된 게시글입니다.");
            }
            // setName 으로 닉네임을 변수값으로 지정
            reportRequest.setName(user.getNickname());
            // 해당 신고하는 게시글 지정
            reportRequest.setBoardId(board.getId());
            // 엔티티화 한후 저장
            // 생성된 게시글 저장 return true
            reportRepository.save(reportRequest.toCreateReportPost(user, board));
            return true;
        } catch (BadCredentialsException e) {
            return false;
        }
    }

    public Boolean createSuggestionPost(String authorizationHeader, SuggestRequest suggestRequest) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

            // setName 으로 닉네임을 변수값으로 지정
            suggestRequest.setName(user.getNickname());
            // 엔티티화 한후 저장
            // 생성된 게시글 저장 return true
            suggestionRepository.save(suggestRequest.toCreateSuggestionPost(user));
            return true;
        } catch (BadCredentialsException e) {
            return false;
        }
    }
}
