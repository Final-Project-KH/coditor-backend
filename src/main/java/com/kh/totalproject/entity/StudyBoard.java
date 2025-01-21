package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.constant.Study;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_board")
@Getter
@Setter
@NoArgsConstructor
public class StudyBoard extends Board {

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Study study;

    @PrePersist
    private void defaultValues() {
        if (status == null) {
            this.status = Status.ACTIVE;
        }

        if (study == null) {
            this.study = Study.NONE;
        }
    }

    @Builder
    public StudyBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
                       LocalDateTime updatedAt, Status status, Study study) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.status = status;
        this.study = study;
        this.setUser(user);
    }
}