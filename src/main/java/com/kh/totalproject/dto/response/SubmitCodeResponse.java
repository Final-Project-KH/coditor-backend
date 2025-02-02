package com.kh.totalproject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubmitCodeResponse {
    // if status is 200(or 201) then String
    // else null
    private String jobId;

    // if status is not 200(or 201) then String
    // else null
    private String error;
}
