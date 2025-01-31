package com.kh.totalproject.exception;

// Flask 응답이 기대하는 형태가 아닌 경우 사용
public class FlaskResponseIsNotValidException extends RuntimeException {
    public FlaskResponseIsNotValidException(String message) {
        super(message);
    }
}
