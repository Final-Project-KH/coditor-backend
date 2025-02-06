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
                .content(content)
                .user(user)
                .suggestionBoard(suggestionBoard)
                .build();
    }
}
