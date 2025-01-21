package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.constant.Team;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_board")
@Getter
@Setter
@NoArgsConstructor
public class TeamBoard extends Board {

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Team team;

    @PrePersist
    private void defaultValues() {
        if (status == null) {
            this.status = Status.ACTIVE;
        }
        if (team == null) {
            this.team = Team.NONE;
        }
    }

    @Builder
    TeamBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
              LocalDateTime updatedAt, Status status, Team team) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.status = status;
        this.team = team;
        this.setUser(user);
    }
}
