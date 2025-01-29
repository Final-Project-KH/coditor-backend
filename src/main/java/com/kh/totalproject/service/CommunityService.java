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
import com.kh.totalproject.entity.*;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 게시글 생성 서비스
    public Boolean createPost(String authorizationHeader, BoardRequest boardRequest, String boardType) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            // 토큰에서 인증 정보 확인
            jwtUtil.getAuthentication(token);
            // Access 토큰에서 id 추출
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

            // setName 으로 닉네임을 변수값으로 지정
            boardRequest.setName(user.getNickname());
            // Enum -> String 타입변환
            BoardType type = BoardType.fromString(boardType);
            // 엔티티화 한후 저장
            Board boardEntity = createBoardEntity(boardRequest, user, type);
            // 생성된 게시글 저장 return true
            return saveBoardEntity(boardEntity);
        } catch (BadCredentialsException e) {
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
    public Boolean modifyPost(String authorizationHeader, BoardRequest boardRequest, String boardType) {
        String token = authorizationHeader.replace("Bearer ", "");
        try {
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

            // Enum -> String 형변환 메서드 호출
            BoardType type = BoardType.fromString(boardType);
            Board existingBoard = findByBoardId(boardRequest, type);

            // 게시글 작성자가 맞는지 확인
            if (!existingBoard.getUser().getUserKey().equals(user.getUserKey())) {
                throw new IllegalArgumentException("작성자가 아닙니다.");
            }

            Board updatedBoard = updateBoard(boardRequest, existingBoard, user, type);

            // 수정된 게시글 저장 return true
            return saveBoardEntity(updatedBoard);

        } catch (BadCredentialsException e) {
            return false;
        }
    }

    // 게시글 삭제 서비스 로직
    public Boolean deletePost(String authorizationHeader, Long id) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            // 유저 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다: " + userId));

            // 게시글 검증
            Board board = boardRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글이 존재하지 않습니다."));

            // 권한 검증
            if (!board.getUser().getUserId().equals(user.getUserId())) {
                throw new SecurityException("삭제할 권한이 없습니다.");
            }

            boardRepository.deleteById(id);
            return true;
        } catch (EntityNotFoundException | SecurityException e) {
            System.err.println("삭제중 에러 발생 : " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("잘못된 요청 : " + e.getMessage());
            return false;
        }
    }

    // BoardId 로 해당하는 글을 찾는 메서드
    private Board findByBoardId(BoardRequest boardRequest, BoardType type) {
        return switch (type) {
            case CODING -> codingBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다."));
            case COURSE -> courseBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다."));
            case STUDY -> studyBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다."));
            case TEAM -> teamBoardRepository.findById(boardRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당하는 게시글을 찾을 수 없습니다."));
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

    public Page<BoardResponse> listAllByBoardTypeWithSort(int page, int size, String boardType,
                                                          String sortBy, String order,
                                                          String status, String enumFilter, String search) {
        BoardType type = BoardType.fromString(boardType);

        // 정렬 설정
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";
        }

        Sort sort = createSort(sortBy, order);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // 동적 쿼리 생성
        Specification<?> spec = createSpecification(type, status, enumFilter, search);

        // 게시판 타입에 따른 repository 선택 및 쿼리 실행
        Page<?> result = switch (type) {
            case CODING -> codingBoardRepository.findAll((Specification<CodingBoard>) spec, pageable);
            case COURSE -> courseBoardRepository.findAll((Specification<CourseBoard>) spec, pageable);
            case STUDY -> studyBoardRepository.findAll((Specification<StudyBoard>) spec, pageable);
            case TEAM -> teamBoardRepository.findAll((Specification<TeamBoard>) spec, pageable);
        };

        // 결과를 BoardResponse 로 매핑
        return result.map(board -> mapToBoardResponse(board, type));
    }

    // 정렬 생성 case 별
    private Sort createSort(String sortBy, String order) {
        Sort.Direction direction = Sort.Direction.fromString(order);
        return switch (sortBy) {
            case "createdAt", "viewCnt", "likeCnt", "commentCnt" -> Sort.by(direction, sortBy);
            case "status" -> Sort.by(Sort.Order.by("status"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    // specification 생성, enum json 타입 검색과 게시판 전체검색 구현
    private Specification<?> createSpecification(BoardType type, String status, String enumFilter, String search) {
        return (root, query, cb) -> {
            // predicate 생성
            List<Predicate> predicates = new ArrayList<>();

            // 상태 필터
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), Status.valueOf(status)));
            }

            // Enum 필터 (JSON 타입 필드 검색)
            if (enumFilter != null && !enumFilter.isEmpty()) {
                String fieldName = switch (type) {
                    case CODING -> "language";
                    case COURSE -> "course";
                    case STUDY -> "study";
                    case TEAM -> "team";
                };
                predicates.add(cb.isTrue(cb.function("JSON_CONTAINS", Boolean.class,
                        root.get(fieldName), cb.literal('"' + enumFilter + '"'), cb.literal("$"))));
            }
            
            // 검색 = 제목 + 내용
            if (search != null && !search.isEmpty()) {
                String searchPatten = "%" + search + "%";   // 제목과 내용을 복합하는 변수
                predicates.add(cb.or(
                        cb.like(root.get("title"), searchPatten),
                        cb.like(root.get("content"), searchPatten)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // 게시판별 맵핑 yield 사용시 반환과 로직수행 둘다 가능
    private BoardResponse mapToBoardResponse(Object board, BoardType type) {
        return switch (type) {
            case CODING -> {
                CodingBoard codingBoard = (CodingBoard) board;
                int commentCnt = commentRepository.countByBoardId(codingBoard.getId());
                yield BoardResponse.ofAllCodingBoard(codingBoard, commentCnt, codingBoard.getLikeCnt(), codingBoard.getDislikeCnt());
            }
            case COURSE -> {
                CourseBoard courseBoard = (CourseBoard) board;
                int commentCnt = commentRepository.countByBoardId(courseBoard.getId());
                yield BoardResponse.ofAllCourseBoard(courseBoard, commentCnt, courseBoard.getLikeCnt(), courseBoard.getDislikeCnt());
            }
            case STUDY -> {
                StudyBoard studyBoard = (StudyBoard) board;
                int commentCnt = commentRepository.countByBoardId(studyBoard.getId());
                yield BoardResponse.ofAllStudyBoard(studyBoard, commentCnt, studyBoard.getLikeCnt(), studyBoard.getDislikeCnt());
            }
            case TEAM -> {
                TeamBoard teamBoard = (TeamBoard) board;
                int commentCnt = commentRepository.countByBoardId(teamBoard.getId());
                yield BoardResponse.ofAllTeamBoard(teamBoard, commentCnt, teamBoard.getLikeCnt(), teamBoard.getDislikeCnt());
            }
        };
    }

    // 각 게시판 별 단일 글을 불러오는 서비스
    public BoardResponse listOneById(long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));

        increaseViewCnt(board); // 조회수 증가

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
    }
    
    // 게시글 내 댓글 생성
    public Boolean addComment(String authorizationHeader, CommentRequest commentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
            Board board = boardRepository.findById(commentRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다 "));
            Comment comment = commentRequest.toAddComment(user, board);
            commentRepository.save(comment);
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("잘못된 요청 : " + e.getMessage());
            return false;
        }
    }

    // 게시글 내 댓글 수정
    public Boolean modifyComment(String authorizationHeader, CommentRequest commentRequest) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다 "));
            Board board = boardRepository.findById(commentRequest.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다 "));
            Comment existingComment = commentRepository.findById(commentRequest.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

            // 기존에 존재하는 데이터를 넣어서 수정된게 없으면 그대로 default 값 사용
            Comment comment = commentRequest.toUpdateComment(user, board, existingComment);

            commentRepository.save(comment);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 댓글 삭제 구현
    public Boolean deleteComment(String authorizationHeader, Long id) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            jwtUtil.getAuthentication(token);
            Long userId = jwtUtil.extractUserId(token);

            // 유저 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

            // 댓글 검증
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("삭제 할 댓글이 없습니다."));

            // 권한 검증
            if (!comment.getUser().getUserId().equals(user.getUserId())) {
                throw new SecurityException("삭제할 권한이 없습니다.");
            }

            commentRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토글방법 좋아요 싫어요 클릭
    public void toggleReaction(Long boardId, Long userId, Reaction reactionType) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재 하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저가 존재 하지 않습니다."));

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
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재 하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저가 존재하지 않습니다."));

        Reaction userReaction = board.getUserReaction(user);
        int likeCnt = board.getLikeCnt();
        int dislikeCnt = board.getDislikeCnt();

        return new BoardReactionResponse(userReaction, likeCnt, dislikeCnt);
    }

    // Top Writer 10명 사이드바 구현 JPQL 사용하여 복합 쿼리 생성 list 0 번에유저, 1번에 글작성 횟수
    public List<BoardResponse> getTopWriterBoard() {
        // Pageable 사용해 0페이지 10개 제한 (10명)
        Pageable topTen = PageRequest.of(0, 10);
        // Repository JPQL 결과 반환후에 List 안에 결과 할당 (닉네임, 글 생성 갯수)
        List<Object[]> results = boardRepository.findTopUsersByPostCount(topTen);

        return results.stream()
                .map(result -> BoardResponse.ofTopWriterBoard((String) result[0], ((Long) result[1]).intValue()))
                .collect(Collectors.toList());
    }

    public List<BoardResponse> getWeeklyPopularPost() {
        Pageable topFive = PageRequest.of(0, 5);

        // 저번주 월요일과 일요일 LocalDateTime 으로 변환
        LocalDateTime startDate = WeeklyTimeCalculator.getStartOfLastWeek();
        LocalDateTime endDate = WeeklyTimeCalculator.getEndOfLastWeek();

        List<Object[]> results = boardRepository.findWeeklyPopularPosts(startDate, endDate, topFive);

        return results.stream()
                .map(result -> BoardResponse.ofWeeklyPopularPost((Board) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }
}
