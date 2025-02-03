/* 요청한 정보를 불러올 수 없을때 예시) 페이지를 찾을 수 없음, 해당 유저를 찾을 수 없음 404 NOT FOUND */
package com.kh.totalproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
