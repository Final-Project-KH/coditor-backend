package com.kh.totalproject.dto.request;

import com.kh.totalproject.entity.SuggestionBoard;
import com.kh.totalproject.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestRequest {
    private Long suggestionId;
    private String title;
    private String name;
    private String content;
    private String imgUrl;
    private List<String> suggestion;

    public SuggestionBoard toCreateSuggestionPost(User user) {
        return SuggestionBoard.builder()
                .id(suggestionId)
                .user(user)
                .content(content)
                .imgUrl(imgUrl)
                .suggestion(suggestion)
                .build();
    }

    public SuggestionBoard toModifySuggestionPost(SuggestionBoard existingData) {
        return SuggestionBoard.builder()
                .id(suggestionId)
                .user(existingData.getUser())
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .suggestion(suggestion != null ? suggestion : existingData.getSuggestion())
                .build();
    }
}
