package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Course;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course_board")
@Getter
@Setter
@NoArgsConstructor
public class CourseBoard extends Board {

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> course;

    @PrePersist
    private void defaultValues() {
        if (course == null) {
            this.course = new ArrayList<>();
        }
    }

    @Builder
    public CourseBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
                       LocalDateTime updatedAt, List<String> course) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.course = course;
        this.setUser(user);
    }
}