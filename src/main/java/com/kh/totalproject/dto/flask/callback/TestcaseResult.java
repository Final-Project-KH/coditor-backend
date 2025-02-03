package com.kh.totalproject.dto.flask.callback;

import com.kh.totalproject.constant.Language;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestcaseResult {
    private Long userId;
    private String jobId;
    private Long questionId;
    private Boolean success;

    private String error;
    private String detail;

    private Integer testcaseIndex;
    private Float memoryUsage;
    private Integer runningTime;
    private String code;
    private Language codeLanguage;
    private Integer codeSize;

    private LocalDateTime createdAt;
}
