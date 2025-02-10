package com.kh.totalproject.service;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.dto.request.ReportRequest;
import com.kh.totalproject.dto.request.SuggestRequest;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.exception.BadRequestException;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.exception.NotFoundException;
import com.kh.totalproject.repository.*;
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

    // 내 소개글 조회 메소드
    public String getUserIntroduction(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        jwtUtil.getAuthentication(token);
        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        return user.getIntroduction() != null ? user.getIntroduction() : "";
    }


    // 내 소개글 등록 메소드
    public boolean saveUserIntroduction(String authorizationHeader, String introduction) {
        String token = authorizationHeader.replace("Bearer ", "");
        jwtUtil.getAuthentication(token);
        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        user.setIntroduction(introduction);
        userRepository.save(user);
        return true;
    }

    // 닉네임 중복 검사 로직
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname); // 닉네임이 DB에 없으면 사용 가능 (true 반환)
    }

    // 닉네임 변경 로직
    public boolean changeNickname(String authorizationHeader, String newNickname) {
        String token = authorizationHeader.replace("Bearer ", "");
        jwtUtil.getAuthentication(token);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        // 중복 검사 추가
        if (!isNicknameAvailable(newNickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        user.setNickname(newNickname);
        userRepository.save(user);
        return true;
    }


    // 현재 비밀번호 확인 메소드 추가
    public boolean checkPw(String authorizationHeader, String inputPw) {
        String token = authorizationHeader.replace("Bearer ", "");
        jwtUtil.getAuthentication(token);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return true; // 비밀번호가 NULL이면 검증 자동 통과
        }

        return passwordEncoder.matches(inputPw, storedPassword);
    }

    // 내 비밀번호 변경
    public boolean changePw(String authorizationHeader, String inputPw, String newPw) {
        String token = authorizationHeader.replace("Bearer ", "");
        jwtUtil.getAuthentication(token);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        String storedPassword = user.getPassword();

        if (storedPassword != null && !passwordEncoder.matches(inputPw, storedPassword)) {
            return false; //  기존 비밀번호가 존재하는 경우, 검증 실패 시 변경 불가
        }

        user.setPassword(passwordEncoder.encode(newPw)); // 새로운 비밀번호 설정
        userRepository.save(user);
        return true; // 변경 성공
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

    // Report 게시글 목록 서비스 구현 (해당 로직 요청시 시작값 status = INACTIVE / ACTIVE 로 설정 해줘야함)
    // Report 게시글 목록 서비스 구현 (해당 로직 요청 시 status = INACTIVE / ACTIVE로 설정해야 함)
    public Page<ReportResponse> myReportList(int page, int size, String sortBy, String order, String status) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // 기본 정렬 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            // status가 null 또는 빈 문자열일 경우 INACTIVE 상태로 설정
            Status reportStatus = (status == null || status.isEmpty()) ? Status.INACTIVE : Status.valueOf(status);

            // Specification 생성 (userKey와 status 필터링)
            Specification<ReportBoard> spec = createSpecificationReport(user.getUserKey(), reportStatus);

            // 페이징, 정렬된 ReportBoard 조회
            Page<ReportBoard> reportBoards = reportRepository.findAll(spec, pageable);

            return reportBoards.map(ReportResponse::ofReportPostList);
        } catch (BadRequestException e) {
            System.err.println("해당 메서드를 수행할 수 없습니다.");
            throw e;
        }
    }

    // userKey와 status에 따라 동적으로 조건을 추가하는 Specification (Report)
    private static Specification<ReportBoard> createSpecificationReport(Long userKey, Status status) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (userKey != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("user").get("userKey"), userKey));
            }

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            return predicate;
        };
    }


    // Suggestion 게시글 목록 서비스 구현 (해당 로직 요청 시 status = INACTIVE / ACTIVE로 설정해야 함)
    public Page<SuggestResponse> mySuggestionList(int page, int size, String sortBy, String order, String status) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // 기본 정렬 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            // status가 null 또는 빈 문자열일 경우 INACTIVE 상태로 설정
            Status suggestionStatus = (status == null || status.isEmpty()) ? Status.INACTIVE : Status.valueOf(status);

            // Specification 생성 (userKey와 status 필터링)
            Specification<SuggestionBoard> spec = createSpecificationSuggestion(user.getUserKey(), suggestionStatus);

            // 페이징, 정렬된 SuggestionBoard 조회
            Page<SuggestionBoard> suggestionBoards = suggestionRepository.findAll(spec, pageable);

            return suggestionBoards.map(SuggestResponse::ofSuggestionPostList);
        } catch (BadRequestException e) {
            System.err.println("해당 메서드를 수행할 수 없습니다.");
            throw e;
        }
    }

    // userKey와 status에 따라 동적으로 조건을 추가하는 Specification (Suggestion)
    private static Specification<SuggestionBoard> createSpecificationSuggestion(Long userKey, Status status) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (userKey != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("user").get("userKey"), userKey));
            }

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            return predicate;
        };
    }


    public ReportResponse myReportPost(long reportId) {
        Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userKey)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
        ReportBoard reportBoard = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));
        if (!Objects.equals(user.getUserKey(), reportBoard.getUser().getUserKey())) {
            throw new ForbiddenException("당신은 이 글에 대한 열람 권한이 없습니다.");
        }
        return ReportResponse.ofOneReportPost(reportBoard);
    }

    public SuggestResponse mySuggestionPost(long suggestionId) {
        Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userKey)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
        SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));
        if (!Objects.equals(user.getUserKey(), suggestionBoard.getUser().getUserKey())) {
            throw new ForbiddenException("당신은 이 글에 대한 열람 권한이 없습니다.");
        }
        return SuggestResponse.ofOneSuggestionPost(suggestionBoard);
    }

//    // 내 신고 글 수정 메서드
//    public Boolean modifyMyReport(String authorizationHeader, ReportRequest reportRequest) {
//        try {
//            String token = authorizationHeader.replace("Bearer ", "");
//            // 토큰에서 인증 정보 확인
//            jwtUtil.getAuthentication(token);
//            // Access 토큰에서 id 추출
//            Long userId = jwtUtil.extractUserId(token);
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다"));
//
//            // reportId와 boardId로 기존 레코드를 찾아 수정
//            ReportBoard reportBoard = reportRepository.findByBoardIdAndReportId(reportRequest.getBoardId(), reportRequest.getReportId())
//                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
//
//            // 수정 권한 체크
//            if (!reportBoard.getUser().getUserKey().equals(user.getUserKey())) {
//                throw new AccessDeniedException("당신은 이 글에 수정 권한이 없습니다.");
//            }
//
//            // 수정할 데이터를 기존 데이터에 반영하여 업데이트
//            ReportBoard updatedBoard = reportRequest.toModifyReportPost(reportBoard);
//
//            // 기존 레코드를 수정하기 전에 업데이트된 report 의 ID가 올바른지 확인
//            if (updatedBoard.getId() == null) {
//                updatedBoard.setId(reportBoard.getId()); // 기존 ID를 설정
//            }
//
//            // 기존 레코드를 수정
//            reportRepository.save(updatedBoard);
//
//            return true;
//        } catch (AccessDeniedException e) {
//            return false;
//        }
//    }
//
//    // 내 건의사항 글 수정
//    public Boolean modifyMySuggestion(String authorizationHeader, SuggestRequest suggestRequest) {
//        try {
//            String token = authorizationHeader.replace("Bearer ", "");
//            // 토큰에서 인증 정보 확인
//            jwtUtil.getAuthentication(token);
//            // Access 토큰에서 id 추출
//            Long userId = jwtUtil.extractUserId(token);
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
//            SuggestionBoard suggestionBoard = suggestionRepository.findById(suggestRequest.getSuggestionId())
//                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재 하지 않습니다."));
//            if (!suggestionBoard.getUser().getUserKey().equals(user.getUserKey())) {
//                throw new AccessDeniedException("당신은 이 글에 수정 권한이 없습니다.");
//            }
//            SuggestionBoard updatedBoard = suggestRequest.toModifySuggestionPost(suggestionBoard);
//            suggestionRepository.save(updatedBoard);
//        } catch (AccessDeniedException e) {
//            return false;
//        }
//        return true;
//    }

    // 내 신고 글 삭제
    public Boolean deleteMyReportPost(Long reportId) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
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
    public Boolean deleteMySuggestionPost(Long suggestionId) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
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
    public boolean connectGoogleEmail(String authorizationHeader){
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        String googelemail = user.getGoogleemail();
        if(googelemail == null) {return false;}
        else return true;
    }

    public boolean connectKakaoEmail(String authorizationHeader){
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        String kakaoemail = user.getKakaoemail();
        if(kakaoemail == null) {return false;}
        else return true;
    }

    public boolean connectNaverEmail(String authorizationHeader){
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        String naveremail = user.getNaveremail();
        if(naveremail == null) {return false;}
        else return true;
    }
}
