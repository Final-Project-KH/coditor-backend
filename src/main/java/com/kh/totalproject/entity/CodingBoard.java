package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Language;
import com.kh.totalproject.constant.Solution;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "coding_board")
@Getter
@Setter
@NoArgsConstructor
public class CodingBoard extends Board {

    @Enumerated(EnumType.STRING)
    private Solution solution;

    @Enumerated(EnumType.STRING)
    private Language language;

    @PrePersist
    private void defaultValues() {
        if (solution == null){
            this.solution = Solution.UNSOLVED;
        }

        if (language == null){
            this.language = Language.NONE;
        }
    }

    @Builder
    public CodingBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
                       LocalDateTime updatedAt, Solution solution, Language language) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.solution = solution;
        this.language = language;
        this.setUser(user);
    }
}
