/* 접근 제한에 걸렸을때 Exception 예시) Admin 접근 가능한 구간에 일반 User 가 접근 을 할때 403 FORBIDDEN ERROR 반환 */
package com.kh.totalproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
