package com.kh.totalproject.exception;

import com.kh.totalproject.dto.response.CancelJobResponse;
import com.kh.totalproject.dto.response.ExecuteJobResponse;
import com.kh.totalproject.dto.response.SubmitCodeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

/**
 * 기본적으로 Spring Security는 처리되지 않은 예외를 401로 처리합니다. (ResponseStatusException 등 일부 제외)
 * 따라서 보다 의미있는 응답을 보장하고, 실패 상황에 따른 응답을 간편하게 처리하기 위해 예외 핸들러를 사용합니다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private final String UNKNOWN_FLASK_ERROR_MESSAGE = "서버 내부 통신 과정에서 알 수 없는 오류가 발생하였습니다.";

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Object> handleUnauthenticatedException(UnauthenticatedException ex) {
        return new ResponseEntity<>(new HashMap<>() {{
            put("error", "Unauthorized");
        }}, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 유저의 코딩 테스트 관련 요청을 처리하는 과정에서
     * Flask -> Spring Boot 4XX 응답 코드 처리
     */
    @ExceptionHandler(CustomHttpClientErrorException.class)
    public ResponseEntity<Object> handleHttpClientErrorException(CustomHttpClientErrorException ex) {
        // Flask 에서 4xx response 수신 시 처리
        int statusCode = ex.getStatusCode().value();
        String url = ex.getRequestUrl();

        if (url.contains("/job/create")) {
            return this.handleSubmitError(statusCode);
        }

        else if (url.contains("/job/execute")) {
            return this.handleExecuteError(statusCode);
        }

        else if (url.contains("/job/cancel")) {
            return this.handleCancelError(statusCode);
        }

        return ResponseEntity.internalServerError().body(
                Map.of("error", UNKNOWN_FLASK_ERROR_MESSAGE));
    }

    /**
     * 유저의 코딩 테스트 관련 요청을 처리하는 과정에서
     * Flask -> Spring Boot 5XX 응답 코드 처리
     */
    @ExceptionHandler(CustomHttpServerErrorException.class)
    public ResponseEntity<Object> handleHttpServerErrorException(CustomHttpServerErrorException ex) {
        log.error("Flask 내부에서 오류가 발생하였습니다.\n응답코드: {}\n예외 메시지: {}", ex.getStatusCode().value(), ex.getMessage());

        return ResponseEntity.internalServerError().body(
            Map.of("error", "서버 내부에서 알 수 없는 오류가 발생하였습니다.")
        );
    }

    @ExceptionHandler(InvalidResponseBodyException.class)
    public ResponseEntity<Object> handleFlaskResponseIsNotValidException(InvalidResponseBodyException ex) {
        log.error("InvalidResponseBodyException: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body(
            Map.of("error", UNKNOWN_FLASK_ERROR_MESSAGE)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
            Map.of("error", "잘못된 형식의 요청입니다.")
        );
    }

    // Spring -> Flask 요청 응답이 없을 시
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Object> handleResourceAccessException(ResourceAccessException ex) {
        return ResponseEntity.internalServerError().body(
                Map.of("error", "현재 해당 요청을 처리할 수 없습니다. 잠시후 다시 시도해주십시오. 문제가 지속될 경우 관리자에게 문의 부탁드립니다.")
        );
    }

    /***************************************
     * 내부 Private Helper 메서드 시작
     ***************************************/
    private ResponseEntity<Object> handleSubmitError(int statusCode) {
        String errorMessage = switch (statusCode) {
            case 400 -> "요청 본문이 유효하지 않습니다.";
            case 404 -> "유효하지 않은 코딩 테스트 문제입니다.";
            case 422 -> "동시 채점은 회원 당 최대 2개로 제한됩니다.";
            default -> UNKNOWN_FLASK_ERROR_MESSAGE;
        };

        return ResponseEntity.status(statusCode).body(
                SubmitCodeResponse.builder()
                        .jobId(null)
                        .error(errorMessage)
                        .build()
        );
    }

    private ResponseEntity<Object> handleExecuteError(int statusCode) {
        String errorMessage = switch (statusCode) {
            case 400 -> "요청 본문이 유효하지 않습니다.";
            case 404 -> "작업 정보가 존재하지 않습니다.";
            default -> UNKNOWN_FLASK_ERROR_MESSAGE;
        };

        return ResponseEntity.status(statusCode).body(
                ExecuteJobResponse.builder()
                        .numOfTestcase(null)
                        .error(errorMessage)
                        .build()
        );
    }

    private ResponseEntity<Object> handleCancelError(int statusCode) {
        String errorMessage = switch (statusCode) {
            case 400 -> "요청 본문이 유효하지 않습니다.";
            case 404 -> "작업 정보가 존재하지 않습니다.";
            default -> UNKNOWN_FLASK_ERROR_MESSAGE;
        };

        return ResponseEntity.badRequest().body(
                CancelJobResponse.builder()
                        .success(false)
                        .error(errorMessage)
                        .build()
        );
    }
}