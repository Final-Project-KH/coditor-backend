package com.kh.totalproject.controller;

import com.kh.totalproject.dto.flask.response.CreateJobResponse;
import com.kh.totalproject.dto.flask.response.ExecuteJobResponse;
import com.kh.totalproject.dto.request.SubmitCodeRequest;
import com.kh.totalproject.dto.response.ExecuteCodeResponse;
import com.kh.totalproject.dto.response.SubmitCodeResponse;
import com.kh.totalproject.service.CodeChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        CreateJobResponse result = codeChallengeService.submit(dto);
        return ResponseEntity.status(result.getStatus()).body(
                SubmitCodeResponse.builder()
                        .jobId(result.getJobId())
                        .error(result.getError())
                        .build()
        );
    }

    @GetMapping("/before-subscribe")
    public ResponseEntity<Void> beforeSubscribe() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            HttpServletRequest request,
            @RequestParam String jobId
    ) {
        Long userId = getCurrentUserIdOrThrow();
        Integer lastEventId = null;
        if (request.getHeader("Last-Event-ID") != null) lastEventId = Integer.parseInt(request.getHeader("Last-Event-ID"));

        SseEmitter emitter = new SseEmitter(180_000L); // 3분 타임아웃
        codeChallengeService.addSubscription(jobId, emitter);
        emitter.onCompletion(() -> {
            log.info("SSE Stream completed for jobId: {}", jobId);
            codeChallengeService.removeSubscription(jobId, emitter);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE Stream timed out for jobId: {}", jobId);
            codeChallengeService.removeSubscription(jobId, emitter);
        });

        emitter.onError((e) -> {
            log.error("SSE Stream error for jobId: {}, error: {}", jobId, e.getMessage());
            codeChallengeService.removeSubscription(jobId, emitter);
        });

        try {
            emitter.send(SseEmitter.event().data("connected"));
        } catch (IOException e) {
            log.error("Failed to send init event", e);
        }

        return emitter;
    }

    @GetMapping("/execute")
    public ResponseEntity<ExecuteCodeResponse> executeJob(
            @RequestParam(name = "jobid") String jobId
    ) {
        Long userId = getCurrentUserIdOrThrow();
        ExecuteJobResponse result = codeChallengeService.executeCode(jobId, userId);
        // 비정상인 경우 프론트는 SSE 연결을 종료
        return ResponseEntity.status(result.getStatus()).body(
                ExecuteCodeResponse.builder()
                        .numOfTestcase(result.getNumOfTestcase())
                        .error(result.getError())
                        .build());
    }
}
