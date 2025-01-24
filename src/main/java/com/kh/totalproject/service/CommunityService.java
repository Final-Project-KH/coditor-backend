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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

            boardRequest.setName(user.getNickname());
            // Enum -> String 타입변환
            BoardType type = BoardType.fromString(boardType);
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
            if (!existingBoard.getUser().getId().equals(user.getId())) {
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
                                                          String status, String enumFilter) {
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
        Specification<?> spec = createSpecification(type, status, enumFilter);

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

    private Sort createSort(String sortBy, String order) {
        Sort.Direction direction = Sort.Direction.fromString(order);
        return switch (sortBy) {
            case "createdAt", "viewCnt", "likeCnt", "commentCnt" -> Sort.by(direction, sortBy);
            case "status" -> Sort.by(Sort.Order.by("status"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private Specification<?> createSpecification(BoardType type, String status, String enumFilter) {
        return (root, query, cb) -> {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

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
    public BoardResponse listOneByBoardType(long id, String boardType) {
        BoardType type = BoardType.fromString(boardType);
        return switch (type) {
            case CODING -> {
                CodingBoard codingBoard = codingBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(codingBoard);   // 조회수 증가 로직 실행
                int commentCnt = commentRepository.countByBoardId(id);  // 댓글수 불러오기
                yield BoardResponse.ofOneCodingPost(codingBoard, commentCnt, codingBoard.getLikeCnt(), codingBoard.getDislikeCnt());  // 반환 값을 Response 로 지정
            }
            case COURSE -> {
                CourseBoard courseBoard = courseBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(courseBoard);
                int commentCnt = commentRepository.countByBoardId(id);
                yield BoardResponse.ofOneCoursePost(courseBoard, commentCnt, courseBoard.getLikeCnt(), courseBoard.getDislikeCnt());
            }
            case STUDY -> {
                StudyBoard studyBoard = studyBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(studyBoard);
                int commentCnt = commentRepository.countByBoardId(id);
                yield BoardResponse.ofOneStudyPost(studyBoard, commentCnt, studyBoard.getLikeCnt(), studyBoard.getDislikeCnt());
            }
            case TEAM -> {
                TeamBoard teamBoard = teamBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(teamBoard);
                int commentCnt = commentRepository.countByBoardId(id);
                yield BoardResponse.ofOneTeamPost(teamBoard, commentCnt, teamBoard.getLikeCnt(), teamBoard.getDislikeCnt());
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
    public Page<CommentResponse> listComment(int page, int size, String sortBy, String order) {
        // 정렬시 기본값 설정, 페이지에 처음 접근할때
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";  // 기본적으로 최신순
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";  // 기본적으로 내림차순
        }
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> comments = commentRepository.findAll(pageable);
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

    public void toggleReaction(Long boardId, Long userId, Reaction reactionType) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

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

    public BoardReactionResponse getReactionStatus(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Reaction userReaction = board.getUserReaction(user);
        int likeCnt = board.getLikeCnt();
        int dislikeCnt = board.getDislikeCnt();

        return new BoardReactionResponse(userReaction, likeCnt, dislikeCnt);
    }
}
