package com.kh.totalproject.dto.response;

import com.kh.totalproject.entity.Board;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {
    private Long boardId;
    private String title;
    private String categories;
    private String content;
    private String imgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;

    // 댓글 목록 추가
    private List<CommentResponse> comments;

    public static BoardResponse ofMyPost(Board board) {
        return BoardResponse.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
