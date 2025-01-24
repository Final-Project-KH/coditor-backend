package com.kh.totalproject.dto.flask.callback;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestcaseResult {
    private String jobId;
    private Boolean success;

    private String error;
    private String detail;

    private Integer testcaseIndex;
    private Float memoryUsage;
    private Integer runningTime;
    private Integer codeSize;
}
