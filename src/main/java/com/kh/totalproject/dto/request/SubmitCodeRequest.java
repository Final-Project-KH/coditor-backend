package com.kh.totalproject.dto.request;

import com.kh.totalproject.constant.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitCodeRequest {
    private Language codeLanguage;
    private String code;
    private Long questionId;

    // Swagger에서 숨김
    @Schema(hidden = true)
    private Long userId;
}
