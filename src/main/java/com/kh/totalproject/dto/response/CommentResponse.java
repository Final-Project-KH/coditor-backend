package com.kh.totalproject.dto.response;

import com.kh.totalproject.entity.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private String name;
    private Long boardId;
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse ofAllComment(Comment comment) {
        return CommentResponse.builder()
                .name(comment.getUser().getNickname())
                .boardId(comment.getBoard().getId())
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
