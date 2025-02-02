package com.kh.totalproject.dto.request;

import com.kh.totalproject.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionCommentRequest {
    private Long suggestionId;
    private Long commentId;
    private String content;

    public SuggestionComment toAddComment(User user, SuggestionBoard suggestionBoard) {
        return SuggestionComment.builder()
                .content(this.content)
                .user(user)
                .suggestionBoard(suggestionBoard)
                .build();
    }

    public SuggestionComment toModifyComment(User user, SuggestionBoard suggestionBoard, SuggestionComment existingData) {
        return SuggestionComment.builder()
                .id(commentId)
                .content(this.content != null ? this.content : existingData.getContent())
                .user(user)
                .suggestionBoard(suggestionBoard)
                .createdAt(existingData.getCreatedAt())
                .build();
    }
}
