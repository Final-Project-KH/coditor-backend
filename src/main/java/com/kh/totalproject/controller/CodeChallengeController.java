package com.kh.totalproject.controller;

import com.kh.totalproject.dto.request.SubmitCodeRequest;
import com.kh.totalproject.dto.response.ExecuteCodeResponse;
import com.kh.totalproject.dto.response.SubmitCodeResponse;
import com.kh.totalproject.service.CodeChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static com.kh.totalproject.util.SecurityUtil.getCurrentUserIdOrThrow;

@RestController
@RequestMapping("/api/code-challenge")
@Slf4j
@RequiredArgsConstructor
public class CodeChallengeController {
    private final CodeChallengeService codeChallengeService;

    @PostMapping("/submit")
    public ResponseEntity<SubmitCodeResponse> submit(
            @RequestBody SubmitCodeRequest dto
    ) {
        dto.setUserId(getCurrentUserIdOrThrow());
        String jobId = codeChallengeService.submit(dto);
        codeChallengeService.addSubscription(jobId, new SseEmitter(180_000L));
        return ResponseEntity.ok().body(
                SubmitCodeResponse.builder()
                        .jobId(jobId)
                        .error(null)
                        .build()
        );
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            HttpServletRequest request,
            @RequestParam String jobId
    ) {
        SseEmitter emitter = codeChallengeService.getEmitter(jobId);

        if (jobId == null || emitter == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid job id");
        }

        // Last-Event-ID 헤더 처리 (재연결 시 사용)
        // 구현 X
//        Integer lastEventId = null;
//        String lastEventHeader = request.getHeader("Last-Event-ID");
//        if (lastEventHeader != null && !lastEventHeader.isEmpty()) {
//            try {
//                lastEventId = Integer.parseInt(lastEventHeader);
//            } catch (NumberFormatException e) {
//                // 잘못된 형식의 값은 무시
//                log.warn("Invalid Last-Event-ID: {}", lastEventHeader);
//            }
//        }

        // 3. 클라이언트 연결 이벤트 핸들러 설정
        emitter.onCompletion(() -> {
            log.info("SSE Stream completed for job id: {}", jobId);
            codeChallengeService.removeSubscription(jobId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE Stream timed out for job id: {}", jobId);
            codeChallengeService.removeSubscription(jobId);
        });

        emitter.onError(e -> {
            log.warn("SSE error for job id: {}, error message: {}", jobId, e.getMessage());
            codeChallengeService.removeSubscription(jobId);
        });

        // 4. 초기 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .data("Connection Established"));
        } catch (IOException e) {
            try {
                emitter.complete();
            } catch (IllegalStateException e2) {
                // 이미 complete 인 경우 또 complete 되어 발생하는 로그 제거
            }
        }

        return emitter;
    }

    @GetMapping("/execute")
    public ResponseEntity<ExecuteCodeResponse> executeJob(
            @RequestParam(name = "jobid") String jobId
    ) {
        Long userId = getCurrentUserIdOrThrow();
        int numOfTestcase = codeChallengeService.executeCode(jobId, userId);
        // 비정상인 경우 프론트는 SSE 연결을 종료
        return ResponseEntity.ok().body(
                ExecuteCodeResponse.builder()
                        .numOfTestcase(numOfTestcase)
                        .error(null)
                        .build());
    }
}
