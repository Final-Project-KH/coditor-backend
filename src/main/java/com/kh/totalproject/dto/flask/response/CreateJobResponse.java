package com.kh.totalproject.dto.flask.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateJobResponse {
    // 201, 400, 404, 422, 500
    private Integer status;

    // if status is 201 then job id
    // else null
    private String jobId;

    // if status is not 201 then error message
    // else null
    private String error;
}
