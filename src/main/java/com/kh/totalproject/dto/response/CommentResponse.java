package com.kh.totalproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String name;
    private Long boardId;
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
}
