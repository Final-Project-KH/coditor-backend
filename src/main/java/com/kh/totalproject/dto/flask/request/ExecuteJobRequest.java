package com.kh.totalproject.dto.flask.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExecuteJobRequest {
    private String jobId;
    private Long userId;
}
