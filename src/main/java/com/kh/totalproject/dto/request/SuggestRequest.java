package com.kh.totalproject.dto.request;

import com.kh.totalproject.constant.Status;
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
    private Status status;
    private List<String> suggestion;

    public SuggestionBoard toCreateSuggestionPost(User user) {
        return SuggestionBoard.builder()
                .user(user)
                .title(title)
                .content(content)
                .imgUrl(imgUrl)
                .suggestion(suggestion)
                .build();
    }

    public SuggestionBoard toModifySuggestionPost(SuggestionBoard existingData) {
        return SuggestionBoard.builder()
                .id(suggestionId)
                .user(existingData.getUser())
                .title(title != null ? title : existingData.getTitle())
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .suggestion(suggestion != null ? suggestion : existingData.getSuggestion())
                .status(status != null ? status : existingData.getStatus())
                .createdAt(existingData.getCreatedAt())
                .build();
    }
}
