package com.kh.totalproject.dto.response;

import com.kh.totalproject.constant.*;
import com.kh.totalproject.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {
    private Long boardId;
    private String name;
    private String title;
    private String content;
    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCnt;
    private int commentCnt;
    private int likeCnt;
    private int dislikeCnt;
    private Reaction reactionStat;
    private Reaction userReaction;
    private Status status;
    private Solution solution;
    private Language language;
    private Course course;
    private Study study;
    private Team team;

    // 댓글 목록 추가
    private List<CommentResponse> comments;

    // 내정보에서 내가 쓴 글을 확인 하기 위한 매핑
//    public static BoardResponse ofMyPost(Board board) {
//        return BoardResponse.builder()
//                .boardId(board.getId())
//                .title(board.getTitle())
//                .content(board.getContent())
//                .createdAt(board.getCreatedAt())
//                .build();
//    }

    // 코딩질문 게시판 전체 보여주는 매핑
    public static BoardResponse ofAllCodingBoard(CodingBoard codingBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(codingBoard.getId())
                .name(codingBoard.getUser().getNickname())
                .title(codingBoard.getTitle())
                .content(codingBoard.getContent())
                .createdAt(codingBoard.getCreatedAt())
                .updatedAt(codingBoard.getUpdatedAt())
                .solution(codingBoard.getSolution())
                .language(codingBoard.getLanguage())
                .viewCnt(codingBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    // 진로질문 게시판 전체 보여주는 매핑
    public static BoardResponse ofAllCourseBoard(CourseBoard courseBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(courseBoard.getId())
                .name(courseBoard.getUser().getNickname())
                .title(courseBoard.getTitle())
                .content(courseBoard.getContent())
                .createdAt(courseBoard.getCreatedAt())
                .updatedAt(courseBoard.getUpdatedAt())
                .course(courseBoard.getCourse())
                .viewCnt(courseBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    // 스터디모집 게시판 전체 보여주는 매핑
    public static BoardResponse ofAllStudyBoard(StudyBoard studyBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(studyBoard.getId())
                .name(studyBoard.getUser().getNickname())
                .title(studyBoard.getTitle())
                .content(studyBoard.getContent())
                .createdAt(studyBoard.getCreatedAt())
                .updatedAt(studyBoard.getUpdatedAt())
                .status(studyBoard.getStatus())
                .study(studyBoard.getStudy())
                .viewCnt(studyBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    // 프로젝트모집 게시판 전체 보여주는 매핑
    public static BoardResponse ofAllTeamBoard(TeamBoard teamBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(teamBoard.getId())
                .name(teamBoard.getUser().getNickname())
                .title(teamBoard.getTitle())
                .content(teamBoard.getContent())
                .createdAt(teamBoard.getCreatedAt())
                .updatedAt(teamBoard.getUpdatedAt())
                .status(teamBoard.getStatus())
                .team(teamBoard.getTeam())
                .viewCnt(teamBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    public static BoardResponse ofOneCodingPost(CodingBoard codingBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(codingBoard.getId())
                .name(codingBoard.getUser().getNickname())
                .title(codingBoard.getTitle())
                .content(codingBoard.getContent())
                .imgUrl(codingBoard.getImgUrl())
                .createdAt(codingBoard.getCreatedAt())
                .updatedAt(codingBoard.getUpdatedAt())
                .solution(codingBoard.getSolution())
                .language(codingBoard.getLanguage())
                .viewCnt(codingBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    public static BoardResponse ofOneCoursePost(CourseBoard courseBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(courseBoard.getId())
                .name(courseBoard.getUser().getNickname())
                .title(courseBoard.getTitle())
                .content(courseBoard.getContent())
                .imgUrl(courseBoard.getImgUrl())
                .createdAt(courseBoard.getCreatedAt())
                .updatedAt(courseBoard.getUpdatedAt())
                .course(courseBoard.getCourse())
                .viewCnt(courseBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    public static BoardResponse ofOneStudyPost(StudyBoard studyBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(studyBoard.getId())
                .name(studyBoard.getUser().getNickname())
                .title(studyBoard.getTitle())
                .content(studyBoard.getContent())
                .imgUrl(studyBoard.getImgUrl())
                .createdAt(studyBoard.getCreatedAt())
                .updatedAt(studyBoard.getUpdatedAt())
                .status(studyBoard.getStatus())
                .study(studyBoard.getStudy())
                .viewCnt(studyBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }

    public static BoardResponse ofOneTeamPost(TeamBoard teamBoard, int commentCnt, int likeCnt, int dislikeCnt) {
        return BoardResponse.builder()
                .boardId(teamBoard.getId())
                .name(teamBoard.getUser().getNickname())
                .title(teamBoard.getTitle())
                .content(teamBoard.getContent())
                .imgUrl(teamBoard.getImgUrl())
                .createdAt(teamBoard.getCreatedAt())
                .updatedAt(teamBoard.getUpdatedAt())
                .status(teamBoard.getStatus())
                .team(teamBoard.getTeam())
                .viewCnt(teamBoard.getViewCnt())
                .likeCnt(likeCnt)
                .dislikeCnt(dislikeCnt)
                .commentCnt(commentCnt)
                .build();
    }
}
