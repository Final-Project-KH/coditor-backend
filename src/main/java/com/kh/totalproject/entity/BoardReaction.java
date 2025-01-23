package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Reaction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "board_reaction")
public class BoardReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key", nullable = false, referencedColumnName = "user_key")
    private User user;

    @Enumerated(EnumType.STRING)
    private Reaction reaction;

    @PrePersist
    private void defaultReaction() {
        if (reaction == null){
            this.reaction = Reaction.NONE;
        }
    }
}
