/* 커뮤니티에 해당하는 모든 요청을 처리하는 서비스 계층
*  각 게시판의 CRUD 에서 Create, Update, 각 게시판 전체글 Read 에 해당하는 로직은 각각 상속받는 하위 클래스를 참조하여
*  직접 하위계층의 Repository 로 접근을 하며 Delete 에 해당하는 로직은 부모클래스의 board_id 를 참조하여 삭제
*  comment 와 reaction 는 조회를 제외하고 CUD 동작은 부모게시판의 id 를 참조받아 작업 수행 */

package com.kh.totalproject.service;

import com.kh.totalproject.constant.BoardType;
import com.kh.totalproject.dto.request.BoardRequest;
import com.kh.totalproject.dto.request.CommentRequest;
import com.kh.totalproject.dto.response.BoardResponse;
import com.kh.totalproject.dto.response.CommentResponse;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<BoardResponse> listAllByBoardTypeWithSort(int page, int size, String boardType, String sortBy, String order) {
        // 정렬시 기본값 설정, 페이지에 처음 접근할때
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";  // 기본적으로 최신순
        }
        if (order == null || order.isEmpty()) {
            order = "DESC";  // 기본적으로 내림차순
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page -1, size, sort);

        // 게시판 타입
        BoardType type = BoardType.fromString(boardType);

        // 각 게시판에 따른 추가 정렬 처리 상태를 분별해줌 예) 모집중 / 모집완료
        if ("status".equals(sortBy) && (type == BoardType.STUDY || type == BoardType.TEAM)) {
            // status 값을 기준으로 정렬: ACTIVE 와 INACTIVE 로 정렬
            if ("ACTIVE".equals(order)) {
                sort = Sort.by(Sort.Direction.ASC, "status");  // 모집중 게시글을 오름차순으로 정렬
            } else if ("INACTIVE".equals(order)) {
                sort = Sort.by(Sort.Direction.DESC, "status");  // 모집완료 게시글을 내림차순으로 정렬
            }
            pageable = PageRequest.of(page - 1, size, sort);
        } else if ("solution".equals(sortBy) && type == BoardType.CODING) {
            // solution 값을 기준으로 정렬: SOLVED 와 UNSOLVED 로 정렬
            if ("SOLVED".equals(order)) {
                sort = Sort.by(Sort.Direction.DESC, "solution");  // 해결된 게시글을 내림차순으로 정렬
            } else if ("UNSOLVED".equals(order)) {
                sort = Sort.by(Sort.Direction.ASC, "solution");  // 미해결 게시글을 오름차순으로 정렬
            }
            pageable = PageRequest.of(page - 1, size, sort);
        } else if ("likeCnt".equals(sortBy) || "commentCnt".equals(sortBy)) {
            // 좋아요와 댓글순서 정렬
            sort = Sort.by(Sort.Direction.fromString(order), sortBy);
            pageable = PageRequest.of(page - 1, size, sort);
        }

        // 각 게시판에 맞는 데이터를 조회하여 반환
        return switch (type) {
            case CODING -> codingBoardRepository.findAll(pageable).map(board ->
                    BoardResponse.ofAllCodingBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case COURSE -> courseBoardRepository.findAll(pageable).map(board ->
                    BoardResponse.ofAllCourseBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case STUDY -> studyBoardRepository.findAll(pageable).map(board ->
                    BoardResponse.ofAllStudyBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case TEAM -> teamBoardRepository.findAll(pageable).map(board ->
                    BoardResponse.ofAllTeamBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
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
                yield BoardResponse.ofOneCodingPost(codingBoard, codingBoard.getCommentCnt(), codingBoard.getLikeCnt(), codingBoard.getDislikeCnt());  // 반환 값을 Response 로 지정
            }
            case COURSE -> {
                CourseBoard courseBoard = courseBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(courseBoard);
                yield BoardResponse.ofOneCoursePost(courseBoard, courseBoard.getCommentCnt(), courseBoard.getLikeCnt(), courseBoard.getDislikeCnt());
            }
            case STUDY -> {
                StudyBoard studyBoard = studyBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(studyBoard);
                yield BoardResponse.ofOneStudyPost(studyBoard, studyBoard.getCommentCnt(), studyBoard.getLikeCnt(), studyBoard.getDislikeCnt());
            }
            case TEAM -> {
                TeamBoard teamBoard = teamBoardRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 글이 존재하지 않습니다."));
                increaseViewCnt(teamBoard);
                yield BoardResponse.ofOneTeamPost(teamBoard, teamBoard.getCommentCnt(), teamBoard.getLikeCnt(), teamBoard.getDislikeCnt());
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
        return null;
    }

    public Object voteForPost(String authorizationHeader, Long id, Boolean like, Boolean dislike) {
        return null;
    }
}
