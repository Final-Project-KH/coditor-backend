/* 커뮤니티에 해당하는 모든 요청을 처리하는 서비스 계층
*  각 게시판의 CRUD 에서 Create, Update, 각 게시판 전체글 Read 에 해당하는 로직은 각각 상속받는 하위 클래스를 참조하여
*  직접 하위계층의 Repository 로 접근을 하며 Delete 에 해당하는 로직은 부모클래스의 board_id 를 참조하여 삭제
*  comment 와 reaction 는 조회를 제외하고 CUD 동작은 부모게시판의 id 를 참조받아 작업 수행 */

package com.kh.totalproject.service;

import com.kh.totalproject.constant.BoardType;
import com.kh.totalproject.constant.Reaction;
import com.kh.totalproject.constant.Status;
import com.kh.totalproject.dto.request.BoardRequest;
import com.kh.totalproject.dto.request.CommentRequest;
import com.kh.totalproject.dto.response.BoardReactionResponse;
import com.kh.totalproject.dto.response.BoardResponse;
import com.kh.totalproject.dto.response.CommentResponse;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.exception.BadRequestException;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.exception.NotFoundException;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import com.kh.totalproject.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {
    private final CodingBoardRepository codingBoardRepository;
    private final CourseBoardRepository courseBoardRepository;
    private final StudyBoardRepository studyBoardRepository;
    private final TeamBoardRepository teamBoardRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final BoardReactionRepository boardReactionRepository;
    private final JwtUtil jwtUtil;
    private final CustomBoardRepository customBoardRepository;

    // 게시글 생성 서비스 getCurrentUserIdOrThrow 를 통해서 Context 헤더로 들어온 Access 토큰을 처리
    public Boolean createPost(BoardRequest boardRequest, String boardType) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // setName 으로 닉네임을 변수값으로 지정
            boardRequest.setName(user.getNickname());
            // Enum -> String 타입변환
            BoardType type = BoardType.fromString(boardType);
            // 엔티티화 한후 저장
            Board boardEntity = createBoardEntity(boardRequest, user, type);
            // 생성된 게시글 저장 return true
            return saveBoardEntity(boardEntity);
        } catch (BadRequestException e) {
            System.err.println("게시글 생성 실패: " + e.getMessage());
            return false;
        }
    }

    // 각 게시판별 엔티티 생성 스위치문
    private Board createBoardEntity(BoardRequest boardRequest, User user, BoardType type) {
        return switch (type) {
            case CODING -> boardRequest.toCreateCodingPost(user);
            case COURSE -> boardRequest.toCreateCoursePost(user);
            case STUDY -> boardRequest.toCreateStudyPost(user);
            case TEAM -> boardRequest.toCreateTeamPost(user);
        };
    }

    // 게시글 수정 서비스
    public Boolean modifyPost(BoardRequest boardRequest, String boardType) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // Enum -> String 형변환 메서드 호출
            BoardType type = BoardType.fromString(boardType);
            Board existingBoard = findByBoardId(boardRequest, type);

            // 게시글 작성자가 맞는지 확인
            if (!existingBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new ForbiddenException("이 글을 삭제 할 권한이 없습니다.");
            }
            Board updatedBoard = updateBoard(boardRequest, existingBoard, user, type);
            // 수정된 게시글 저장 return true
            return saveBoardEntity(updatedBoard);
        } catch (BadRequestException e) {
            System.err.println("게시글 수정 실패: " + e.getMessage());
            return false;
        }
    }

    // 게시글 삭제 서비스 로직
    public Boolean deletePost(Long id) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            // 유저 검증
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // 게시글 검증
            Board board = boardRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("삭제할 게시글이 존재하지 않습니다."));

            // 삭제 권한 검증
            if (!board.getUser().getUserId().equals(user.getUserId())) {
                throw new SecurityException("삭제할 권한이 없습니다.");
            }
            boardRepository.deleteById(id);
            return true;
        } catch (BadRequestException e) {
            System.err.println("게시글 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    // BoardId 로 해당하는 글을 찾는 메서드
    private Board findByBoardId(BoardRequest boardRequest, BoardType type) {
        return switch (type) {
            case CODING -> codingBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당하는 게시글을 찾을 수 없습니다."));
            case COURSE -> courseBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당하는 게시글을 찾을 수 없습니다."));
            case STUDY -> studyBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당하는 게시글을 찾을 수 없습니다."));
            case TEAM -> teamBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        };
    }

    // 해당하는 RequestDto 를 업데이트 하는 메서드
    private Board updateBoard(BoardRequest boardRequest, Board existingBoard, User user, BoardType type) {
        return switch (type) {
            case CODING -> boardRequest.toModifyCodingPost(user, (CodingBoard) existingBoard);
            case COURSE -> boardRequest.toModifyCoursePost(user, (CourseBoard) existingBoard);
            case STUDY -> boardRequest.toModifyStudyPost(user, (StudyBoard) existingBoard);
            case TEAM -> boardRequest.toModifyTeamPost(user, (TeamBoard) existingBoard);
        };
    }

    // 각 게시판별 글 저장 및 업데이트
    private Boolean saveBoardEntity(Board boardEntity) {
        if (boardEntity instanceof CodingBoard) {
            codingBoardRepository.save((CodingBoard) boardEntity);
        } else if (boardEntity instanceof CourseBoard) {
            courseBoardRepository.save((CourseBoard) boardEntity);
        } else if (boardEntity instanceof StudyBoard) {
            studyBoardRepository.save((StudyBoard) boardEntity);
        } else if (boardEntity instanceof TeamBoard) {
            teamBoardRepository.save((TeamBoard) boardEntity);
        } else {
            return false;
        }
        return true;
    }

//    public Page<BoardResponse> listAllByBoardTypeWithSort(int page, int size, String boardType,
//                                                          String sortBy, String order,
//                                                          String status, String enumFilter, String search) {
//        if (sortBy == null || sortBy.isEmpty()) {
//            sortBy = "createdAt";
//        }
//        if (order == null || order.isEmpty()) {
//            order = "DESC";
//        }
//
//        BoardType type = BoardType.fromString(boardType);
//        Sort sort = createSort(sortBy, order);
//        Pageable pageable = PageRequest.of(page - 1, size, sort);
//        Status statusEnum = (status != null && !status.isEmpty()) ? Status.valueOf(status.toUpperCase()) : null;
//
//        Page<BoardResponse> result;
//        switch (type) {
//            case CODING:
//                Specification<CodingBoard> codingSpec = createSpecification(type, enumFilter, search);
//                log.info("CODING 게시판 필터 조건 - enumFilter: {}, search: {}", enumFilter, search);
//                result = codingBoardRepository.findAllWithCommentCntForCoding(statusEnum, sortBy, codingSpec, pageable)
//                        .map(board -> mapToBoardResponse((CodingBoard) board[0], BoardType.CODING, ((Long) board[1]).longValue()));
//                break;
//            case COURSE:
//                Specification<CourseBoard> courseSpec = createSpecification(type, enumFilter, search);
//                log.info("COURSE 게시판 필터 조건 - enumFilter: {}, search: {}", enumFilter, search);
//                result = courseBoardRepository.findAllWithCommentCntForCourse(sortBy, courseSpec, pageable)
//                        .map(board -> mapToBoardResponse((CourseBoard) board[0], BoardType.COURSE, ((Long) board[1]).longValue()));
//                break;
//            case STUDY:
//                Specification<StudyBoard> studySpec = createSpecification(type, enumFilter, search);
//                log.info("STUDY 게시판 필터 조건 - enumFilter: {}, search: {}", enumFilter, search);
//                result = studyBoardRepository.findAllWithCommentCntForStudy(statusEnum, sortBy, studySpec, pageable)
//                        .map(board -> mapToBoardResponse((StudyBoard) board[0], BoardType.STUDY, ((Long) board[1]).longValue()));
//                break;
//            case TEAM:
//                Specification<TeamBoard> teamSpec = createSpecification(type, enumFilter, search);
//                log.info("TEAM 게시판 필터 조건 - enumFilter: {}, search: {}", enumFilter, search);
//                result = teamBoardRepository.findAllWithCommentCntForTeam(statusEnum, sortBy, teamSpec, pageable)
//                        .map(board -> mapToBoardResponse((TeamBoard) board[0], BoardType.TEAM, ((Long) board[1]).longValue()));
//                break;
//            default:
//                throw new IllegalArgumentException("유효하지 않은 BoardType 입니다.");
//        }
//
//        log.info("쿼리로 호출할때 불려오는 갯수 : {}", result.getTotalElements());
//
//        return result;
//    }
//
//    private Sort createSort(String sortBy, String order) {
//        Sort.Direction direction = "DESC".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
//
//        // commentCnt 기준 정렬
//        if ("commentCnt".equalsIgnoreCase(sortBy)) {
//            return Sort.by(direction, "commentCnt");
//        }
//
//        // 기존 정렬 기준
//        if ("createdAt".equalsIgnoreCase(sortBy) || "viewCnt".equalsIgnoreCase(sortBy)
//                || "likeCnt".equalsIgnoreCase(sortBy)) {
//            return Sort.by(direction, sortBy);
//        }
//
//        // 기본 정렬 (최신순)
//        return Sort.by(Sort.Direction.DESC, "createdAt");
//    }
//
//    private <T> Specification<T> createSpecification(BoardType type, String enumFilter, String search) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // enumFilter 적용
//            if (enumFilter != null && !enumFilter.isEmpty()) {
//                String fieldName = switch (type) {
//                    case CODING -> "language";
//                    case COURSE -> "course";
//                    case STUDY -> "study";
//                    case TEAM -> "team";
//                };
//                String[] filters = enumFilter.split(",");
//                Predicate enumPredicate = cb.or(
//                        Arrays.stream(filters)
//                                .map(filter -> cb.isTrue(cb.function("JSON_CONTAINS", Boolean.class,
//                                        root.get(fieldName), cb.literal("[\"" + filter + "\"]"), cb.literal("$"))))
//                                .toArray(Predicate[]::new)
//                );
//                predicates.add(enumPredicate);
//                log.info("enumFilter 적용: {}", enumFilter);
//            }
//
//            // search 필터 적용
//            if (search != null && !search.isEmpty()) {
//                String searchPattern = "%" + search + "%";
//                predicates.add(cb.or(
//                        cb.like(root.get("title"), searchPattern),
//                        cb.like(root.get("content"), searchPattern)
//                ));
//                log.info("search 필터 적용: {}", search);
//            }
//
//            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    private BoardResponse mapToBoardResponse(Board board, BoardType type, Long commentCnt) {
//        return switch (type) {
//            case CODING -> {
//                CodingBoard codingBoard = (CodingBoard) board;
//                yield BoardResponse.ofAllCodingBoard(codingBoard, commentCnt.intValue(), codingBoard.getLikeCnt(), codingBoard.getDislikeCnt());
//            }
//            case COURSE -> {
//                CourseBoard courseBoard = (CourseBoard) board;
//                yield BoardResponse.ofAllCourseBoard(courseBoard, commentCnt.intValue(), courseBoard.getLikeCnt(), courseBoard.getDislikeCnt());
//            }
//            case STUDY -> {
//                StudyBoard studyBoard = (StudyBoard) board;
//                yield BoardResponse.ofAllStudyBoard(studyBoard, commentCnt.intValue(), studyBoard.getLikeCnt(), studyBoard.getDislikeCnt());
//            }
//            case TEAM -> {
//                TeamBoard teamBoard = (TeamBoard) board;
//                yield BoardResponse.ofAllTeamBoard(teamBoard, commentCnt.intValue(), teamBoard.getLikeCnt(), teamBoard.getDislikeCnt());
//            }
//        };
//    }

    // 각 게시판에서 필터, 정렬, 검색 등등을 하나로 묶어 통합적인 로직, Criteria 동적쿼리 API 를 사용
    public Page<BoardResponse> listAllByBoardTypeWithSort(int page, int size, String boardType,
                                                          String sortBy, String order,
                                                          String status, String enumFilter, String search) {
        try {
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";
            }
            BoardType type = BoardType.fromString(boardType);

            Pageable pageable = PageRequest.of(page - 1, size);
            Status statusEnum = (status != null && !status.isEmpty()) ? Status.valueOf(status.toUpperCase()) : null;

            Page<Object[]> resultPage = customBoardRepository.findAllWithDynamicFilters(type, statusEnum, sortBy, order, enumFilter, search, pageable);

            // Object[]를 BoardResponse 로 변환
            List<BoardResponse> boardResponses = resultPage.getContent().stream()
                    .map(objects -> {
                        Board board = (Board) objects[0];  // Board 엔티티
                        int commentCnt = ((Long) objects[1]).intValue();  // 댓글 개수
                        int likeCnt = ((Long) objects[2]).intValue();  // 좋아요 개수
                        int dislikeCnt = ((Integer) objects[3]).intValue();  // 조회수 개수

                        return BoardResponse.of(board, commentCnt, likeCnt, dislikeCnt);  // BoardResponse 반환
                    })
                    .collect(Collectors.toList());
            return new PageImpl<>(boardResponses, pageable, resultPage.getTotalElements());
        } catch (BadRequestException e) {
            System.err.println("게시글 조회 실패: " + e.getMessage());
            return Page.empty();
        }
    }


    // 단순 조회수 올리기 서비스
    public Boolean listOneByIdCheck(long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 글을 찾을 수 없습니다."));

        increaseViewCnt(board); // 조회수 증가
        return true;
    }

    // 각 게시판 별 단일 글을 불러오는 서비스
    public BoardResponse listOneById(long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 글을 찾을 수 없습니다."));

        // 댓글 수 및 작성자 글 수 가져오기
        int commentCnt = commentRepository.countByBoardId(id);
        int postCntByUser = (int) boardRepository.countByUserUserKey(board.getUser().getUserKey());

        // 게시판 타입에 따라 적절한 응답 생성
        return switch (board.getBoardType()) {
            case CODING -> {
                CodingBoard codingBoard = (CodingBoard) board;
                yield BoardResponse.ofOneCodingPost(codingBoard, postCntByUser, commentCnt, codingBoard.getLikeCnt(), codingBoard.getDislikeCnt());
            }
            case COURSE -> {
                CourseBoard courseBoard = (CourseBoard) board;
                yield BoardResponse.ofOneCoursePost(courseBoard, postCntByUser, commentCnt, courseBoard.getLikeCnt(), courseBoard.getDislikeCnt());
            }
            case STUDY -> {
                StudyBoard studyBoard = (StudyBoard) board;
                yield BoardResponse.ofOneStudyPost(studyBoard, postCntByUser, commentCnt, studyBoard.getLikeCnt(), studyBoard.getDislikeCnt());
            }
            case TEAM -> {
                TeamBoard teamBoard = (TeamBoard) board;
                yield BoardResponse.ofOneTeamPost(teamBoard, commentCnt, postCntByUser, teamBoard.getLikeCnt(), teamBoard.getDislikeCnt());
            }
        };
    }

    // 각 게시판 별 조회수 증가값 저장
    private void increaseViewCnt(Board board) {
        board.setViewCnt(board.getViewCnt() + 1);
        if (board instanceof CodingBoard) {
            codingBoardRepository.save((CodingBoard) board);
        } else if (board instanceof CourseBoard) {
            courseBoardRepository.save((CourseBoard) board);
        } else if (board instanceof StudyBoard) {
            studyBoardRepository.save((StudyBoard) board);
        } else if (board instanceof TeamBoard) {
            teamBoardRepository.save((TeamBoard) board);
        }
    }

    // 게시글 접근시 게시글내 해당하는 댓글도 같이 통신
    public Page<CommentResponse> listComment(Long boardId, int page, int size, String sortBy, String order) {
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
            Page<Comment> comments = commentRepository.findByBoardId(boardId, pageable);
            return comments.map(CommentResponse::ofAllComment);
        } catch (BadRequestException e) {
            System.err.println("게시글내 댓글 조회 실패: " + e.getMessage());
            return Page.empty();
        }
    }

    // 게시글 내 댓글 생성
    public Boolean addComment(CommentRequest commentRequest) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
            Board board = boardRepository.findById(commentRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다 "));
            Comment comment = commentRequest.toAddComment(user, board);
            commentRepository.save(comment);
            return true;
        } catch (BadRequestException e) {
            System.err.println("댓글 생성 실패: " + e.getMessage());
            return false;
        }
    }

    // 게시글 내 댓글 수정
    public Boolean modifyComment(CommentRequest commentRequest) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
            Board board = boardRepository.findById(commentRequest.getBoardId())
                    .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다 "));
            Comment existingComment = commentRepository.findById(commentRequest.getCommentId())
                    .orElseThrow(() -> new NotFoundException("해당 댓글을 찾을 수 없습니다."));

            if (!user.getUserKey().equals(existingComment.getUser().getUserKey())) {
                throw new ForbiddenException("해당 유저는 댓글을 수정 할 권한이 없습니다.");
            }
            // 기존에 존재하는 데이터와 비교해, 바뀐 값이면 수정, 아니면 기존의 데이터 사용
            Comment comment = commentRequest.toUpdateComment(user, board, existingComment);
            commentRepository.save(comment);
            return true;
        } catch (BadRequestException e) {
            System.err.println("댓글 수정 실패: " + e.getMessage());
            return false;
        }
    }

    // 게시글 내 댓글 삭제 구현
    public Boolean deleteComment(Long id) {
        try {
            Long userKey = SecurityUtil.getCurrentUserIdOrThrow();
            User user = userRepository.findById(userKey)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // 댓글 검증
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

            // 권한 검증
            if (!comment.getUser().getUserId().equals(user.getUserId())) {
                throw new ForbiddenException("해당 댓글을 삭제할 권한이 없습니다.");
            }

            commentRepository.deleteById(id);
            return true;
        } catch (BadRequestException e) {
            System.err.println("댓글 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    // 토글방법 좋아요 싫어요 클릭
    public void toggleReaction(Long boardId, Long userId, Reaction reactionType) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

        Optional<BoardReaction> existingReaction = boardReactionRepository.findByBoardAndUser(board, user);

        if (existingReaction.isPresent()) {
            BoardReaction reaction = existingReaction.get();
            if (reaction.getReaction() == reactionType) {
                // 같은 반응을 다시 누르면 반응 제거
                boardReactionRepository.delete(reaction);
                board.getBoardReactions().remove(reaction);
            } else {
                // 다른 반응으로 변경
                reaction.setReaction(reactionType);
            }
        } else {
            // 새로운 반응 추가
            BoardReaction newReaction = BoardReaction.builder()
                    .board(board)
                    .user(user)
                    .reaction(reactionType)
                    .build();
            board.getBoardReactions().add(newReaction);
            boardReactionRepository.save(newReaction);
        }

        // 좋아요와 싫어요 수 업데이트
        board.setLikeCnt(board.getLikeCnt());
        board.setDislikeCnt(board.getDislikeCnt());
        boardRepository.save(board);
    }

    // 사용자 좋아요 싫어요 클릭시 확인 Status 구현
    public BoardReactionResponse getReactionStatus(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("해당 게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

        Reaction userReaction = board.getUserReaction(user);
        int likeCnt = board.getLikeCnt();
        int dislikeCnt = board.getDislikeCnt();

        return new BoardReactionResponse(userReaction, likeCnt, dislikeCnt);
    }

    // Top Writer 10명 사이드바 구현 JPQL 사용하여 복합 쿼리 생성 list 0 번에유저, 1번에 글작성 횟수
    public List<BoardResponse> getTopWriterBoard() {
        // Pageable 사용해 0페이지 10개 제한 (10명)
        Pageable topTen = PageRequest.of(0, 10);
        // Repository JPQL 결과 반환후에 List 안에 결과 할당 (닉네임, 프로필 사진, 글 생성 갯수)
        List<Object[]> results = boardRepository.findTopUsersByPostCount(topTen);

        return results.stream()
                .map(result -> BoardResponse.ofTopWriterBoard(
                        (String) result[1],         // 닉네임
                        (String) result[0],         // 프로필 사진
                        ((Long) result[2]).intValue() // 게시글 개수
                ))
                .collect(Collectors.toList());
    }

    // 주간 인기글 로직 JPQL 사용해 복합쿼리 생성 List 0 번에 게시글, 1번에 프로필사진, 2번에 유저 닉네임
    public List<BoardResponse> getWeeklyPopularPost() {
        Pageable topFive = PageRequest.of(0, 5);

        // 저번주 월요일과 일요일 LocalDateTime 으로 변환
        LocalDateTime startDate = WeeklyTimeCalculator.getStartOfLastWeek();
        LocalDateTime endDate = WeeklyTimeCalculator.getEndOfLastWeek();

        List<Object[]> results = boardRepository.findWeeklyPopularPosts(startDate, endDate, topFive);

        return results.stream()
                .map(result -> BoardResponse.ofWeeklyPopularPost(
                        (Board) result[0],           // 게시글
                        (String) result[2],          // 닉네임
                        (String) result[1]           // 프로필 사진 URL
                ))
                .collect(Collectors.toList());
    }

    // 상대방이 쓴 글 목록 확인 (글 전체 확인 가능)
    public Page<BoardResponse> listOthersPost(Long userId, int page, int size, String sortBy, String order) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));

            // 정렬 기준이 null 이거나 비어있으면 기본값 설정
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";  // 기본적으로 최신순
            }
            if (order == null || order.isEmpty()) {
                order = "DESC";  // 기본적으로 내림차순
            }

            Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            Pageable pageable = PageRequest.of(page - 1, size, sort);

            // 유저별 게시글 목록 가져오기
            Page<Board> boards = boardRepository.findByUserKey(user.getUserKey(), pageable);


            // 게시글 목록과 관련된 정보(commentCnt, likeCnt, dislikeCnt)를 가져와서 변환
            return boards.map(board -> {
                int commentCnt = commentRepository.countCommentsByBoardId(board.getId());
                int likeCnt = boardReactionRepository.countLikesByBoardId(board.getId());
                int dislikeCnt = boardReactionRepository.countDislikesByBoardId(board.getId());
                return BoardResponse.ofPost(board, commentCnt, likeCnt, dislikeCnt);
            });
        } catch (BadRequestException e) {
            System.err.println("목록 불러오기 실패: " + e.getMessage());
        }

        // 오류 처리
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시글 목록을 불러올 수 없습니다.");
    }

    // 상대방 프로필 검색 (포스트 갯수 와 자기소개, url 등등 확인 가능)
    public UserResponse listOthersProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저를 찾을 수 없습니다."));
        int postCntByUser = (int) boardRepository.countByUserUserKey(user.getUserKey());
        return UserResponse.ofOtherUserProfile(user, postCntByUser);
    }
}
