package com.kh.totalproject.service;

import com.kh.totalproject.constant.BoardType;
import com.kh.totalproject.dto.request.BoardRequest;
import com.kh.totalproject.dto.response.BoardResponse;
import com.kh.totalproject.entity.*;
import com.kh.totalproject.repository.*;
import com.kh.totalproject.util.JwtUtil;
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

    // 각 게시판 별 전체 글을 불러오는 서비스
    public Page<BoardResponse> listAllByBoardTypeWithSort(int page, int size, String boardType, String sortBy, String order) {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        BoardType type = BoardType.fromString(boardType);
        return switch (type) {
            case CODING -> codingBoardRepository.findAll(pageable).map(board -> BoardResponse.ofAllCodingBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case COURSE -> courseBoardRepository.findAll(pageable).map(board -> BoardResponse.ofAllCourseBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case STUDY -> studyBoardRepository.findAll(pageable).map(board -> BoardResponse.ofAllStudyBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
            case TEAM -> teamBoardRepository.findAll(pageable).map(board -> BoardResponse.ofAllTeamBoard(board, board.getCommentCnt(), board.getLikeCnt(), board.getDislikeCnt()));
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

}
