package com.kh.totalproject.dto.flask.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExecuteJobResponse {
    // 200, 400, 404
    private Integer status;

    // if status is 200 then int
    // else null
    private Integer numOfTestcase;

    // if status is not 200 then error message
    // else null
    private String error;
}
