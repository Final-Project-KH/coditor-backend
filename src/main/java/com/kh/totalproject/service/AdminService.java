package com.kh.totalproject.service;

import com.kh.totalproject.constant.Role;
import com.kh.totalproject.dto.request.ReportCommentRequest;
import com.kh.totalproject.dto.response.ReportCommentResponse;
import com.kh.totalproject.dto.response.ReportResponse;
import com.kh.totalproject.dto.response.SuggestResponse;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.entity.ReportBoard;
import com.kh.totalproject.entity.SuggestionBoard;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.exception.BadRequestException;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.repository.ReportRepository;
import com.kh.totalproject.repository.SuggestionRepository;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import com.kh.totalproject.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
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
    private final ReportRepository reportRepository;
    private final SuggestionRepository suggestionRepository;
    private final JwtUtil jwtUtil;

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

//    public Page<ReportResponse> listReportPost(String authorizationHeader, int page, int size, String sortBy, String order) {
//        try {
//            String role = jwtUtil.extractUserRole(authorizationHeader);
//            if (!role.equals("ADMIN")) {
//                throw new AccessDeniedException("관리자만 접근 할 수 있는 페이지 입니다.");
//            }
//            // 기본 정렬 설정
//            if (sortBy == null || sortBy.isEmpty()) {
//                sortBy = "createdAt";
//            }
//            if (order == null || order.isEmpty()) {
//                order = "DESC";
//            }
//            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
//            Pageable pageable = PageRequest.of(page - 1, size, sort);
//            Page<ReportBoard> reportBoards = reportRepository.findAll(pageable);
//            return reportBoards.map(ReportResponse::ofReportPostList);
//        } catch (AccessDeniedException e) {
//            log.error("해당 신고 글에 대한 접근 권한이 없습니다.");
//            throw e;
//        }
//    }
//
//    public Page<SuggestResponse> listSuggestionPost(String authorizationHeader, int page, int size, String sortBy, String order) {
//        try {
//            String role = jwtUtil.extractUserRole(authorizationHeader);
//            if (!role.equals("ADMIN")) {
//                throw new AccessDeniedException("관리자만 접근 할 수 있는 페이지 입니다.");
//            }
//            // 기본 정렬 설정
//            if (sortBy == null || sortBy.isEmpty()) {
//                sortBy = "createdAt";
//            }
//            if (order == null || order.isEmpty()) {
//                order = "DESC";
//            }
//            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
//            Pageable pageable = PageRequest.of(page - 1, size, sort);
//            Page<SuggestionBoard> suggestionBoards = suggestionRepository.findAll(pageable);
//            return suggestionBoards.map(SuggestResponse::ofSuggestionPostList);
//        } catch (AccessDeniedException e) {
//            log.error("해당 건의사항 글에 대한 접근 권한이 없습니다.");
//            throw e;
//        }
//    }
//
//    public Boolean deleteReport(String authorizationHeader, Long reportId) {
//        try {
//            String role = jwtUtil.extractUserRole(authorizationHeader);
//            if (!role.equals("ADMIN")) {
//                throw new AccessDeniedException("관리자만 접근 할 수 있는 페이지 입니다.");
//            }
//            return true;
//        } catch (AccessDeniedException e) {
//
//        }
//    }
//
//    public Boolean deleteSuggestion(String authorizationHeader, Long suggestionId) {
//    }
//
//    public ReportCommentResponse listReportReply(String authorizationHeader) {
//    }
//
//    public ReportCommentResponse listSuggestionReply(String authorizationHeader) {
//    }
//
//    public ReportCommentRequest replyReport(String authorizationHeader, Long reportId) {
//    }
//
//    public ReportCommentRequest replySuggestion(String authorizationHeader, Long suggestionId) {
//    }
//
//    public Boolean deletePost(String authorizationHeader, Long boardId) {
//    }
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
