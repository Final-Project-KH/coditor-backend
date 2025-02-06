package com.kh.totalproject.dto.response;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.entity.SuggestionBoard;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestResponse {
    private Long suggestionId;
    private String title;
    private String name;
    private String content;
    private String imgUrl;
    private Status status;
    private LocalDateTime createdAt;
    private List<String> suggestion;
    private List<SuggestionCommentResponse> suggestionCommentResponses;

    public static SuggestResponse ofSuggestionPostList(SuggestionBoard suggestionBoard) {
        return SuggestResponse.builder()
                .suggestionId(suggestionBoard.getId())
                .name(suggestionBoard.getUser().getNickname())
                .title(suggestionBoard.getTitle())
                .createdAt(suggestionBoard.getCreatedAt())
                .status(suggestionBoard.getStatus())
                .suggestion(suggestionBoard.getSuggestion())
                .content(suggestionBoard.getContent())
                .build();
    }

    public static SuggestResponse ofOneSuggestionPost(SuggestionBoard suggestionBoard) {
        return SuggestResponse.builder()
                .suggestionId(suggestionBoard.getId())
                .name(suggestionBoard.getUser().getNickname())
                .title(suggestionBoard.getTitle())
                .content(suggestionBoard.getContent())
                .createdAt(suggestionBoard.getCreatedAt())
                .status(suggestionBoard.getStatus())
                .suggestion(suggestionBoard.getSuggestion())
                .build();
    }
}
