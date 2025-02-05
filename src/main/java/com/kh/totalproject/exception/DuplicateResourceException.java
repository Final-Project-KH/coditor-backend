/* 중복된 데이터 Exception 예시) Admin 접근 가능한 구간에 일반 User 가 접근 을 할때 409 Conflict ERROR 반환 */
package com.kh.totalproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
