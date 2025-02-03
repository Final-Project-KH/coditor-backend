package com.kh.totalproject.dto.response;

import com.kh.totalproject.constant.ChallengeDifficulty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChallengeMetaResponse {
    private Long questionId;

    private String title;

    private String category;

    private ChallengeDifficulty difficulty;

    private Float passingRate;

    private Boolean passedBefore;
}
