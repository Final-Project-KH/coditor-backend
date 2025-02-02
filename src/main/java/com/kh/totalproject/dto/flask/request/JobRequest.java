package com.kh.totalproject.dto.flask.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobRequest {
    private String jobId;
    private Long userId;
}
