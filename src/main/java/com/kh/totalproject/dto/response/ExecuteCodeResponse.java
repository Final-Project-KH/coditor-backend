package com.kh.totalproject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExecuteCodeResponse {
    // if status is 200 else null
    private Integer numOfTestcase;

    // if status is not 200 else null
    private String error;
}
