package com.kh.totalproject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubmitCodeResponse {
    // if status is 201 else null
    private String jobId;

    // if status is not 201 else null
    private String error;
}
