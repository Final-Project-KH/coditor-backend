package com.kh.totalproject.dto.request;

import com.kh.totalproject.constant.*;
import com.kh.totalproject.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequest {
    private Long boardId;
    private String name;
    private String title;
    private String content;
    private String imgUrl;
    private Status status;
    private List<String> language;
    private List<String> course;
    private List<String> study;
    private List<String> team;

    public CodingBoard toCreateCodingPost(User user) {
        return CodingBoard.builder()
                .title(title)
                .user(user)
                .content(content)
                .imgUrl(imgUrl)
                .status(status)
                .language(language)
                .build();
    }

    public CourseBoard toCreateCoursePost(User user) {
        return CourseBoard.builder()
                .title(title)
                .user(user)
                .content(content)
                .imgUrl(imgUrl)
                .course(course)
                .build();
    }

    public StudyBoard toCreateStudyPost(User user) {
        return StudyBoard.builder()
                .title(title)
                .user(user)
                .content(content)
                .imgUrl(imgUrl)
                .status(status)
                .study(study)
                .build();
    }

    public TeamBoard toCreateTeamPost(User user) {
        return TeamBoard.builder()
                .title(title)
                .user(user)
                .content(content)
                .imgUrl(imgUrl)
                .status(status)
                .team(team)
                .build();
    }

    public CodingBoard toModifyCodingPost(User user, CodingBoard existingData) {
        return CodingBoard.builder()
                .boardId(existingData.getId())
                .title(title != null ? title : existingData.getTitle())
                .user(user)
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .status(status != null ? status : existingData.getStatus())
                .language(language != null ? language : existingData.getLanguage())
                .createdAt(existingData.getCreatedAt())
                .build();
    }

    public CourseBoard toModifyCoursePost(User user, CourseBoard existingData) {
        return CourseBoard.builder()
                .boardId(existingData.getId())
                .title(title != null ? title : existingData.getTitle())
                .user(user)
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .course(course != null ? course : existingData.getCourse())
                .createdAt(existingData.getCreatedAt())
                .build();
    }

    public StudyBoard toModifyStudyPost(User user, StudyBoard existingData) {
        return StudyBoard.builder()
                .boardId(existingData.getId())
                .title(title != null ? title : existingData.getTitle())
                .user(user)
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .status(status != null ? status : existingData.getStatus())
                .study(study != null ? study : existingData.getStudy())
                .createdAt(existingData.getCreatedAt())
                .build();
    }

    public TeamBoard toModifyTeamPost(User user, TeamBoard existingData) {
        return TeamBoard.builder()
                .boardId(existingData.getId())
                .title(title != null ? title : existingData.getTitle())
                .user(user)
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .status(status != null ? status : existingData.getStatus())
                .team(team != null ? team : existingData.getTeam())
                .createdAt(existingData.getCreatedAt())
                .build();
    }
}