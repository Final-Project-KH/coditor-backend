package com.kh.totalproject.service;

import com.kh.totalproject.constant.Role;
import com.kh.totalproject.constant.Status;
import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.request.SuggestionCommentRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.exception.BadRequestException;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.exception.NotFoundException;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import com.kh.totalproject.util.SecurityUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReportRepository reportRepository;
    private final SuggestionRepository suggestionRepository;
    private final ReportCommentRepository reportCommentRepository;
    private final SuggestionCommentRepository suggestionCommentRepository;

    public Page<UserResponse> listAllUserInfo(int page, int size, String sortBy, String order, String search) {
        try {
            // 기본 정렬 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "registeredAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }

            // Sort 기본값이 아닌 이상한 값 들어 왔을때 대비한 예외처리 수정
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(order);
            } catch (IllegalArgumentException e) {
                direction = Sort.Direction.DESC;  // 기본값 설정
            }

            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page -1, size, sort);

            Specification<User> spec = createSpecification(search);
            Page<User> users = userRepository.findAll(spec, pageable);

            return users.map(UserResponse::ofAllUserInfo);
        } catch (BadRequestException e) {
            System.err.println("관리자 권한이 없습니다.");
            throw e;
        }
    }

    // 검색기능 user_id, nickname, email 로 like 검색 가능
    private Specification<User> createSpecification(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search + "%"; // 오타 수정

                predicates.add(cb.or(
                        cb.like(root.get("userId"), searchPattern),
                        cb.like(root.get("nickname"), searchPattern),
                        cb.like(root.get("email"), searchPattern)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Report 게시글 목록 서비스 구현 (해당 로직 요청시 status 값 없으면 ACTIVE 로 기본값)
    public Page<ReportResponse> listReportPost(int page, int size, String sortBy, String order, String status) {
        try {
            // 기본 정렬 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Status reportStatus = (status == null || status.isEmpty()) ? Status.ACTIVE : Status.valueOf(status);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Specification<ReportBoard> spec = createSpecificationReport(reportStatus);
            Page<ReportBoard> reportBoards = reportRepository.findAll(spec, pageable);
            return reportBoards.map(ReportResponse::ofReportPostList);
        } catch (BadRequestException e) {
            System.err.println("해당 메서드를 수행 할 수 없습니다.");
            throw e;
        }
    }

    // Status 별로 정렬을 지정 할 동적 Specification 설정 (Report)
    private static Specification<ReportBoard> createSpecificationReport(Status status) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            return predicate;
        };
    }

    // Suggestion 게시글 목록 구현 (해당 로직 요청시 status 값 없으면 ACTIVE 로 기본값)
    public Page<SuggestResponse> listSuggestionPost(int page, int size, String sortBy, String order, String status) {
        try {
            // 기본 정렬 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Status suggestionStatus = (status == null || status.isEmpty()) ? Status.ACTIVE : Status.valueOf(status);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Specification<SuggestionBoard> spec = createSpecificationSuggestion(suggestionStatus);
            Page<SuggestionBoard> suggestionBoards = suggestionRepository.findAll(spec, pageable);
            return suggestionBoards.map(SuggestResponse::ofSuggestionPostList);
        } catch (BadRequestException e) {
            System.err.println("해당 메서드를 수행 할 수 없습니다.");
            throw e;
        }
    }

    // Status 별로 정렬을 지정 할 동적 Specification 설정 (Suggestion)
    private static Specification<SuggestionBoard> createSpecificationSuggestion(Status status) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            return predicate;
        };
    }

    // 관리자가 유저가 작성한 Report 글을 삭제 할 수 있도록 로직 구현
    public Boolean deleteReportPost(Long reportId) {
        try {
            ReportBoard reportBoard = reportRepository.findById(reportId)
                    .orElseThrow(() -> new NotFoundException("해당 신고 게시글을 찾을 수 없습니다."));
            reportRepository.deleteById(reportBoard.getId());
            return true;
        } catch (BadRequestException e) {
            System.err.println("신고 게시글 삭제 로직 실패: " + e.getMessage());
            return false;
        }
    }

    // 관리자가 유저가 작성한 Suggestion 글을 삭제 할 수 있도록 로직 구현
    public Boolean deleteSuggestionPost(Long suggestionId) {
        try {
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionId)
                    .orElseThrow(() -> new NotFoundException("해당 신고 게시글을 찾을 수 없습니다."));
            suggestionRepository.deleteById(suggestionBoard.getId());
            return true;
        } catch (BadRequestException e) {
            System.err.println("건의사항 게시글 삭제 로직 실패: " + e.getMessage());
            return false;
        }
    }

    // 답변 확인
    public Page<ReportCommentResponse> listReportReply(Long reportId, int page, int size, String sortBy, String order) {
        try {
            // 정렬시 기본값 설정, 페이지에 처음 접근할때
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";  // 기본적으로 최신순
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";  // 기본적으로 내림차순
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<ReportComment> reportComments = reportCommentRepository.findByReportBoard_Id(reportId, pageable);
            return reportComments.map(ReportCommentResponse::ofAdminReply);
        } catch (BadRequestException e) {
            System.err.println("게시글내 답변 조회 실패: " + e.getMessage());
            return Page.empty();
        }
    }

    public Page<SuggestionCommentResponse> listSuggestionReply(Long suggestionId, int page, int size, String sortBy, String order) {
        try {
            // 정렬시 기본값 설정, 페이지에 처음 접근할때
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";  // 기본적으로 최신순
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";  // 기본적으로 내림차순
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);
            Page<SuggestionComment> suggestionComments = suggestionCommentRepository.findBySuggestionBoard_Id(suggestionId, pageable);
            return suggestionComments.map(SuggestionCommentResponse::ofAdminReply);
        } catch (BadRequestException e) {
            System.err.println("게시글내 답변 조회 실패: " + e.getMessage());
            return Page.empty();
        }
    }

    public Boolean replyReport(ReportCommentRequest reportCommentRequest) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
            ReportBoard reportBoard = reportRepository.findById(reportCommentRequest.getReportId())
                    .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다 "));
            ReportComment reportComment = reportCommentRequest.toAddComment(user, reportBoard);
            reportCommentRepository.save(reportComment);
            reportBoard.setStatus(Status.INACTIVE); // 댓글 작성시 자동으로 해당 게시글 INACTIVE 설정
            return true;
        } catch (BadRequestException e) {
            System.err.println("댓글 생성 실패: " + e.getMessage());
            return false;
        }
    }

    public Boolean replySuggestion(SuggestionCommentRequest suggestionCommentRequest) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionCommentRequest.getSuggestionId())
                    .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다 "));
            SuggestionComment suggestionComment = suggestionCommentRequest.toAddComment(user, suggestionBoard);
            suggestionCommentRepository.save(suggestionComment);
            suggestionBoard.setStatus(Status.INACTIVE); // 댓글 작성시 자동으로 해당 게시글 INACTIVE 설정
            return true;
        } catch (BadRequestException e) {
            System.err.println("댓글 생성 실패: " + e.getMessage());
            return false;
        }
    }

    public Boolean deletePost(Long boardId) {
        try {
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new NotFoundException("해당 게시물이 존재하지 않습니다."));
            boardRepository.deleteById(board.getId());
            return true;
        } catch (BadRequestException e) {
            System.err.println("게시글 삭제 로직 실패 : " + e.getMessage());
            return false;
        }
    }
//
//
//    public Page<?> listAnnouncement() {
//    }
//
//    public Object createAnnouncement(String authorizationHeader) {
//    }
//
//    public Object modifyAnnouncement(String authorizationHeader, Long announcementId) {
//    }
//
//    public Object deleteAnnouncement(String authorizationHeader, Long announcementId) {
//        return null;
//    }
}
