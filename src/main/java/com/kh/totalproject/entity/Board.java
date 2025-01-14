package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "board")  // 테이블 이름 수정
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(length = 1000)
    private String content;

    @Column(length = 50)
    private String category;

    private int viewCnt;
    private int commentCnt;

    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status activity;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key", nullable = false)  // FK 컬럼 지정
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setBoard(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setBoard(null);
    }
}