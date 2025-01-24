//package com.kh.totalproject.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//import java.util.HashMap;
//
///**
// * 기본적으로 Spring Security는 처리되지 않은 예외를 401로 처리합니다. (ResponseStatusException 등 일부 제외)
// * 따라서 보다 의미있는 응답을 보장하고, 실패 상황에 따른 응답을 간편하게 처리하기 위해 예외 핸들러를 사용합니다.
// */
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(UnauthenticatedException.class)
//    public ResponseEntity<Object> handleUnauthenticatedException(UnauthenticatedException ex) {
//        return new ResponseEntity<>(new HashMap<>() {{
//            put("error", "Unauthorized");
//        }}, HttpStatus.UNAUTHORIZED);
//    }
//}