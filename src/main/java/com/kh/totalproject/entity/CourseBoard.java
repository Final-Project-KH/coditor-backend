package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Course;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_board")
@Getter
@Setter
@NoArgsConstructor
public class CourseBoard extends Board {

    @Enumerated(EnumType.STRING)
    private Course course;

    @PrePersist
    private void defaultValues() {
        if (course == null) {
            this.course = Course.NONE;
        }
    }

    @Builder
    public CourseBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
                       LocalDateTime updatedAt, Course course) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.course = course;
        this.setUser(user);
    }
}