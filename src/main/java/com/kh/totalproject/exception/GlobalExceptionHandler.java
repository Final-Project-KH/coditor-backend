//package com.kh.totalproject.exception;
//
//import com.kh.totalproject.dto.response.ApiResponse;
//import jakarta.validation.ConstraintViolation;
//import lombok.extern.slf4j.Slf4j;
//import org.hibernate.exception.ConstraintViolationException;
//import org.springframework.dao.DataAccessException;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.UnexpectedRollbackException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//@Slf4j
//@ControllerAdvice
//public class GlobalExceptionHandler {
//    private static final Map<String, String> CONSTRAINT_MESSAGE_MAP = new HashMap<>();
//
//    // https://mag1c.tistory.com/363 클래스 초기화 문법
//    // https://www.tcpschool.com/java/java_member_initBlock
//    static {
//        CONSTRAINT_MESSAGE_MAP.put("user.unique_email", "동일한 이메일이 존재합니다.");
//        CONSTRAINT_MESSAGE_MAP.put("board.unique_name", "동일한 게시판이 존재합니다.");
//    }
//
//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(NoSuchElementException ex) {
//        ApiResponse<Void> response = new ApiResponse<>(ex.getMessage());
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    // 비슷한 시점에 동시에 삽입 시도 되는 경우 등에 대한 안전빵 처리
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
//        String message = "데이터 무결성 오류가 발생했습니다."; // 기본 메시지
//
//        Throwable cause = ex.getCause();
//        if (cause instanceof ConstraintViolationException constraintEx) {
//            String constraintName = constraintEx.getConstraintName();
//
//            if (CONSTRAINT_MESSAGE_MAP.containsKey(constraintName)) {
//                message = CONSTRAINT_MESSAGE_MAP.get(constraintName);
//            } else {
//                log.warn("알 수 없는 제약 조건 위반: {}", constraintName);
//            }
//        }
//
//        ApiResponse<Void> response = new ApiResponse<>(message);
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    }
//
//    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
//    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
//        String errorMessage = ex.getConstraintViolations()
//                .stream()
//                .findFirst()
//                .map(ConstraintViolation::getMessage)
//                .orElse("값 유효성 검증 실패.");
//        ApiResponse<Void> response = new ApiResponse<>(errorMessage);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    @ExceptionHandler(DataAccessException.class)
//    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
//        log.error("처리되지 않은 DB CRUD 예외 : {}\n예외 상세 : {}", ex.getClass(), ex.getMessage());
//        ApiResponse<Void> response = new ApiResponse<>("데이터베이스 CRUD 과정에서 오류가 발생했습니다.");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//
//    @ExceptionHandler(UnexpectedRollbackException.class)
//    public ResponseEntity<ApiResponse<Void>> handleUnexpectedRollbackException(UnexpectedRollbackException ex) {
//        log.error("Rollback된 트랜잭션 예외 상세 : {}", ex.getMessage());
//        ApiResponse<Void> response = new ApiResponse<>("트랜잭션 처리 중 오류가 발생했습니다.");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
//        log.error("처리되지 않은 예외 : {}\n예외 상세 : {}", ex.getClass(), ex.getMessage());
//        ApiResponse<Void> response = new ApiResponse<>("서버 내부 오류가 발생했습니다.");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//
//}
