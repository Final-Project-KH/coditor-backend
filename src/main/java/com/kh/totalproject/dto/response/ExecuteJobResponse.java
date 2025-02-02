package com.kh.totalproject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExecuteJobResponse {
    // if status is 200 then Integer
    // else null
    private Integer numOfTestcase;

    // if status is not 200 then String
    // else null
    private String error;
}
