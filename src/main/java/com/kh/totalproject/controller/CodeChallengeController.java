package com.kh.totalproject.controller;

import com.kh.totalproject.constant.ChallengeDifficulty;
import com.kh.totalproject.dto.request.SubmitCodeRequest;
import com.kh.totalproject.dto.response.*;
import com.kh.totalproject.entity.CodeChallengeInfo;
import com.kh.totalproject.entity.CodeChallengeSubmission;
import com.kh.totalproject.service.CodeChallengeService;
import com.kh.totalproject.util.Base64Util;
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
import java.util.List;
import java.util.Map;

import static com.kh.totalproject.util.SecurityUtil.getCurrentUserIdOrThrow;

@RestController
@RequestMapping("/api/code-challenge")
@Slf4j
@RequiredArgsConstructor
public class CodeChallengeController {
    private final CodeChallengeService codeChallengeService;

    @PostMapping("/submit")
    public ResponseEntity<Object> submit(
            @RequestBody SubmitCodeRequest dto
    ) {
        dto.setUserId(getCurrentUserIdOrThrow());

        if (!Base64Util.isBase64Encoded(dto.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "제출된 코드는 조작되었거나 유효하지 않습니다.")
            );
        }

        String jobId = codeChallengeService.createJob(dto);

        // 2분 30초 수명 설정
        codeChallengeService.addSubscription(jobId, new SseEmitter(150_000L));
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
            if (e instanceof IOException) {
                log.info("Disconnected from client!");
            } else {
                log.warn("SSE error for job id: {}, error message: {}", jobId, e.getMessage());
            }

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

    @PostMapping("/execute")
    public ResponseEntity<ExecuteJobResponse> executeJob(
        @RequestBody Map<String, String> body
    ) {
        Long userId = getCurrentUserIdOrThrow();

        String jobId = body.get("jobId");
        if (jobId == null || jobId.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ExecuteJobResponse.builder()
                            .numOfTestcase(null)
                            .error("요청 본문에 \"jobId\"가 존재하지 않습니다")
                            .build());
        }

        int numOfTestcase = codeChallengeService.executeJob(body.get("jobId"), userId);
        // 비정상인 경우 프론트는 SSE 연결을 종료
        return ResponseEntity.ok().body(
                ExecuteJobResponse.builder()
                        .numOfTestcase(numOfTestcase)
                        .error(null)
                        .build());
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancelJobResponse> cancelJob(
            @RequestBody Map<String, String> body
    ) {
        Long userId = getCurrentUserIdOrThrow();

        String jobId = body.get("jobId");
        if (jobId == null || jobId.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CancelJobResponse.builder()
                            .success(false)
                            .error("요청 본문에 \"jobId\"가 존재하지 않습니다")
                            .build()
            );
        }

        codeChallengeService.cancelJob(jobId, userId);
        return ResponseEntity.ok().body(
                CancelJobResponse.builder()
                        .success(true)
                        .error(null)
                        .build()
        );
    }

    @GetMapping("/submission/{questionId}")
    public ResponseEntity<Object> getSubmissionHistory(
        @PathVariable Long questionId
    ) {
        Long userId = getCurrentUserIdOrThrow();
        CodeChallengeInfo challengeInfo = codeChallengeService.getChallengeInfo(questionId);
        if (challengeInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "존재하지 않는 코딩 테스트 문제입니다.")
            );
        }

        List<CodeChallengeSubmission> result = codeChallengeService.getSubmissionHistory(userId, challengeInfo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<CodeChallengeSubmission>> getAllSubmissionHistory() {
        Long userId = getCurrentUserIdOrThrow();
        List<CodeChallengeSubmission> results = codeChallengeService.getAllSubmissionHistory(userId);
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/challenge/{questionId}")
    public ResponseEntity<Object> getChallengeDetail(
        @PathVariable Long questionId,
        @RequestParam(required = false, name = "user") Long userId
    ) {
        CodeChallengeInfo challengeInfo = codeChallengeService.getChallengeInfo(questionId);
        if (challengeInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "존재하지 않는 코딩 테스트 문제입니다.")
            );
        }

        Object result = codeChallengeService.getChallengeDetail(challengeInfo, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/challenges/{difficulty}")
    public ResponseEntity<List<ChallengeMetaResponse>> getAllChallengeMeta(
        @PathVariable String difficulty,
        @RequestParam(required = false, name = "user") Long userId
    ) {
        ChallengeDifficulty challengeDifficulty;
        try {
            challengeDifficulty = ChallengeDifficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
        List<ChallengeMetaResponse> results = codeChallengeService.getChallengeMetaList(challengeDifficulty, userId);
        return ResponseEntity.ok(results);
    }
}
