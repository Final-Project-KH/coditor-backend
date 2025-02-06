package com.kh.totalproject.dto.response;

import com.kh.totalproject.constant.ChallengeDifficulty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChallengeDetailResponse {
    private String title;

    private String description;

    private String cond;

    private ChallengeDifficulty difficulty;

    private Integer memoryLimit;

    private Integer runningTimeLimit;

    private String lastSubmittedCode;
}
