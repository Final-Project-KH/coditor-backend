package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Status;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class SuggestionBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Long id;

    private String title;

    @Lob
    @Column(length = 1000)
    private String content;

    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 의견제시글 작성시 드롭바 메뉴 선택 (예시 : 디자인, 기능 개선)
    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private List<String> suggestion;

    @Enumerated(EnumType.STRING)
    private Status status;

    // 게시글 새로운 생성시 작동
    @PrePersist
    public void defaultTime() {
        if (status == null) {
            this.status = Status.ACTIVE;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_key", nullable = false,  referencedColumnName = "user_key")  // FK 컬럼 지정
    private User user;

    @OneToMany(mappedBy = "suggestionBoard", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<SuggestionComment> suggestionComments = new ArrayList<>();

    public void addComment(SuggestionComment suggestionComment) {
        suggestionComments.add(suggestionComment);
        suggestionComment.setSuggestionBoard(this);
    }

    public void removeComment(SuggestionComment suggestionComment) {
        suggestionComments.remove(suggestionComment);
        suggestionComment.setSuggestionBoard(null);
    }
}
