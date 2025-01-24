package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.constant.Team;
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
@Table(name = "team_board")
@Getter
@Setter
@NoArgsConstructor
public class TeamBoard extends Board {

    @Enumerated(EnumType.STRING)
    private Status status;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> team;

    @PrePersist
    private void defaultValues() {
        if (status == null) {
            this.status = Status.ACTIVE;
        }
        if (team == null) {
            this.team = new ArrayList<>();
        }
    }

    @Builder
    TeamBoard(User user, Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt,
              LocalDateTime updatedAt, Status status, List<String> team) {
        super(boardId, title, content, imgUrl, createdAt, updatedAt);
        this.status = status;
        this.team = team;
        this.setUser(user);
    }
}
