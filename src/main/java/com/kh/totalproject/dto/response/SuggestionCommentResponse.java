package com.kh.totalproject.dto.response;

import com.kh.totalproject.entity.ReportComment;
import com.kh.totalproject.entity.SuggestionComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionCommentResponse {
    private String name;
    private Long suggestionId;
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SuggestionCommentResponse ofAdminReply(SuggestionComment suggestionComment) {
        return SuggestionCommentResponse.builder()
                .name(suggestionComment.getUser().getNickname())
                .suggestionId(suggestionComment.getSuggestionBoard().getId())
                .commentId(suggestionComment.getId())
                .content(suggestionComment.getContent())
                .createdAt(suggestionComment.getCreatedAt())
                .build();
    }
}
