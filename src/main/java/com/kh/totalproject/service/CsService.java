package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.SuggestionCommentRequest;
import com.kh.totalproject.dto.response.ReportCommentResponse;
import com.kh.totalproject.dto.response.SuggestionCommentResponse;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
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
    private final ReportCommentRepository reportCommentRepository;
    private final SuggestionCommentRepository suggestionCommentRepository;
    private final JwtUtil jwtUtil;

    // 신고 게시글 생성 서비스
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
            reportRequest.setName(user.getNickname());  // 여기서 굳이 Nickname 으로 저장 할 필요 없이 userKey 로 저장 - 추후에 수정 필요
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

    // 건의사항 게시글 생성 서비스
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
            suggestRequest.setName(user.getNickname()); // 여기서 굳이 Nickname 으로 저장 할 필요 없이 userKey 로 저장 - 추후에 수정 필요
            // 엔티티화 한후 저장
            // 생성된 게시글 저장 return true
            suggestionRepository.save(suggestRequest.toCreateSuggestionPost(user));
            return true;
        } catch (BadCredentialsException e) {
            return false;
        }
    }

    public Page<ReportCommentResponse> listReportComment(String authorizationHeader, Long reportId, int page, int size, String sortBy, String order) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";  // 기본적으로 최신순
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";  // 기본적으로 내림차순
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<ReportComment> reportComments = reportCommentRepository.findByReportBoard_Id(reportId, pageable);
            return reportComments.map(ReportCommentResponse::ofAllComment);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Page<SuggestionCommentResponse> listSuggestionComment(String authorizationHeader, Long suggestionId, int page, int size, String sortBy, String order) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";  // 기본적으로 최신순
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";  // 기본적으로 내림차순
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<SuggestionComment> suggestionComments = suggestionCommentRepository.findBySuggestionBoard_Id(suggestionId, pageable);
            return suggestionComments.map(SuggestionCommentResponse::ofAllComment);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Boolean AddReportComment(String authorizationHeader, ReportCommentRequest reportCommentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            ReportBoard reportBoard = reportRepository.findById(reportCommentRequest.getReportId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));
            ReportComment reportComment = reportCommentRequest.toAddComment(user, reportBoard);
            reportCommentRepository.save(reportComment);
            return true;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Boolean AddSuggestionComment(String authorizationHeader, SuggestionCommentRequest suggestionCommentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionCommentRequest.getSuggestionId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            SuggestionComment suggestionComment = suggestionCommentRequest.toAddComment(user, suggestionBoard);
            suggestionCommentRepository.save(suggestionComment);
            return true;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Boolean ModifyReportComment(String authorizationHeader, ReportCommentRequest reportCommentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            ReportBoard reportBoard = reportRepository.findById(reportCommentRequest.getReportId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            ReportComment existingComment = reportCommentRepository.findById(reportCommentRequest.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
            ReportComment reportComment = reportCommentRequest.toModifyComment(user, reportBoard, existingComment);
            reportCommentRepository.save(reportComment);
            return true;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Boolean ModifySuggestionComment(String authorizationHeader, SuggestionCommentRequest suggestionCommentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionCommentRequest.getSuggestionId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
            SuggestionComment existingComment = suggestionCommentRepository.findById(suggestionCommentRequest.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
            SuggestionComment suggestionComment = suggestionCommentRequest.toModifyComment(user, suggestionBoard, existingComment);
            suggestionCommentRepository.save(suggestionComment);
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
        return null;
    }

    public Boolean DeleteReportPost(String authorizationHeader, Long commentId) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            ReportComment reportComment = reportCommentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
            if (!reportComment.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("해당 댓글을 삭제 할 권한이 없습니다.");
            }
            reportCommentRepository.deleteById(commentId);
            return true;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }

    public Boolean DeleteSuggestionPost(String authorizationHeader, Long commentId) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
            SuggestionComment suggestionComment = suggestionCommentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
            if (!suggestionComment.getUser().getUserKey().equals(user.getUserKey())) {
                throw new AccessDeniedException("해당 댓글을 삭제 할 권한이 없습니다.");
            }
            suggestionCommentRepository.deleteById(commentId);
            return true;
        } catch (AccessDeniedException e) {
            throw new AccessDeniedException("해당글의 댓글을 열람 할 권한이 없습니다.");
        }
    }
}
