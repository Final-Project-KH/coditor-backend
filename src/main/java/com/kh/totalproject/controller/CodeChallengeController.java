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
        String jobId = codeChallengeService.createJob(dto);
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
        if (jobId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        SseEmitter emitter = codeChallengeService.getEmitter(jobId);
        if (emitter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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

        emitter.onCompletion(() -> {
            log.info("SSE Stream completed for job id: {}", jobId);
            codeChallengeService.removeSubscriptionAndSetEmitterComplete(jobId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE Stream timed out for job id: {}", jobId);
            codeChallengeService.removeSubscriptionAndSetEmitterComplete(jobId);
        });

        emitter.onError(e -> {
            log.warn("SSE error for job id: {}, error message: {}", jobId, e.getMessage());
            codeChallengeService.removeSubscriptionAndSetEmitterComplete(jobId);
        });

        codeChallengeService.sendSseMessage(
            jobId,
            emitter,
            "Connection Established",
            null
        );

        return emitter;
    }

    @GetMapping("/execute")
    public ResponseEntity<ExecuteCodeResponse> executeJob(
            @RequestParam(name = "jobid") String jobId
    ) {
        Long userId = getCurrentUserIdOrThrow();
        int numOfTestcase = codeChallengeService.executeJob(jobId, userId);
        // 비정상인 경우 프론트는 SSE 연결을 종료
        return ResponseEntity.ok().body(
                ExecuteCodeResponse.builder()
                        .numOfTestcase(numOfTestcase)
                        .error(null)
                        .build());
    }
}
