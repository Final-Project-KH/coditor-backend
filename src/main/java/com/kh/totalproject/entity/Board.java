package com.kh.totalproject.entity;

import com.kh.totalproject.constant.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    private String title;

    @Lob
    @Column(length = 1000)
    private String content;

    private int viewCnt = 0;
    private int commentCnt = 0;
    private int likeCnt = 0;
    private int dislikeCnt = 0;

    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private BoardType boardType;

    // 매개변수가 포함된 생성자는 @NoArgs 어노테이션으로 처리가 불가능 하므로 매개변수가 있는 생성자 명시
    public Board(Long boardId, String title, String content, String imgUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = boardId;
        this.title = title;
        this.content = content;
        this.imgUrl = imgUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 게시글 새로운 생성시 작동
    @PrePersist
    public void defaultTime() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // 게시글 업데이트시 작동
    @PreUpdate
    public void updatedTime() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key", nullable = false,  referencedColumnName = "user_key")  // FK 컬럼 지정
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportBoard> reportBoards = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardReaction> boardReactions = new ArrayList<>();

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setBoard(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setBoard(null);
    }

    // 좋아요 수를 동적으로 계산하고 likeCnt 필드를 업데이트
    public int getLikeCnt() {
        return (int) boardReactions.stream()
                .filter(reaction -> reaction.getReaction().equals(Reaction.LIKE))
                .count();
    }

    // 싫어요 수를 동적으로 계산
    public int getDislikeCnt() {
        return (int) boardReactions.stream()
                .filter(reaction -> reaction.getReaction().equals(Reaction.DISLIKE))
                .count();
    }

    // 사용자의 반응을 처리 (사용자가 좋아요, 싫어요를 했는지 확인)
    public Reaction getUserReaction(User user) {
        return boardReactions.stream()
                .filter(br -> br.getUser().equals(user))
                .findFirst()
                .map(BoardReaction::getReaction)
                .orElse(Reaction.NONE);
    }
}
