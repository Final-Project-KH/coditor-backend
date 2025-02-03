package com.kh.totalproject.service;

import com.kh.totalproject.constant.SendTestcaseResultStatus;
import com.kh.totalproject.dto.flask.callback.TestcaseResult;
import com.kh.totalproject.dto.flask.request.JobRequest;
import com.kh.totalproject.dto.request.SubmitCodeRequest;
import com.kh.totalproject.entity.CodeChallengeMeta;
import com.kh.totalproject.entity.CodeChallengeSubmission;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.exception.CustomHttpClientErrorException;
import com.kh.totalproject.exception.CustomHttpServerErrorException;
import com.kh.totalproject.exception.InvalidResponseBodyException;
import com.kh.totalproject.repository.CodeChallengeMetaRepository;
import com.kh.totalproject.repository.CodeChallengeSubmissionRepository;
import com.kh.totalproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class CodeChallengeService {
    // Java에서 멀티 스레드 환경에서 효율적으로 동작하도록 설계된 Map 인터페이스의 구현체
    // 추후 빈에 등록 후 서비스를 쪼개서 사용하는 것도 고려 중 입니다
    private final ConcurrentHashMap<String, SseEmitter> subscriptions = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final CodeChallengeMetaRepository codeChallengeMetaRepository;
    private final CodeChallengeSubmissionRepository codeChallengeSubmissionRepository;

    private final RestTemplate restTemplate;
    private final String FLASK_URL = "http://127.0.0.1:5000";

    public String createJob(SubmitCodeRequest dto) {
        Map<String, Object> flaskResponse = sendRequestToFlask(FLASK_URL + "/job/create", dto, HttpMethod.POST);
        Map<String, Object> responseData = (Map<String, Object>) flaskResponse.get("data");

        if (responseData.get("jobId") == null) {
            throw new InvalidResponseBodyException("코딩 테스트 submit 요청에 대한 응답 본문에서 jobId를 가져올 수 없습니다.");
        }
        return (String) responseData.get("jobId");
    }

    public SendTestcaseResultStatus sendTestcaseResult(String jobId, TestcaseResult result) {
        SseEmitter emitter = subscriptions.get(jobId);

        // 구독 중인 사용자가 없는 경우
        if (emitter == null) {
            return SendTestcaseResultStatus.CLIENT_NOT_FOUND;
        }

        // 사용자의 중단 요청에 대한 처리
        // 반환 값과 관계 없이 Celery Task는 자동 종료됨
        else if (
            result.getSuccess() &&
            result.getDetail() != null &&
            result.getDetail().contains("중단")
        ) {
            removeSubscriptionAndSetEmitterComplete(jobId);
            return SendTestcaseResultStatus.SUCCESS;
        }

        // Task 실행 완료 처리
        // 반환 값과 관계 없이 Celery Task는 자동 종료됨
        else if (
            result.getDetail() != null &&
            result.getDetail().contains("complete")
        ) {
            removeSubscriptionAndSetEmitterComplete(jobId);

            User user = userRepository.findById(result.getUserId()).orElse(null);
            CodeChallengeMeta codeChallengeMeta = codeChallengeMetaRepository.findById(result.getQuestionId()).orElse(null);
            codeChallengeSubmissionRepository.save(
                    CodeChallengeSubmission.builder()
                        .user(user)
                        .codeChallengeMeta(codeChallengeMeta)
                        .code(result.getCode())
                        .codeLanguage(result.getCodeLanguage())
                        .success(result.getSuccess())
                        .memoryUsage(result.getMemoryUsage())
                        .runningTime(result.getRunningTime())
                        .codeSize(result.getCodeSize())
                        .submittedAt(result.getCreatedAt())
                        .build()
            );
            return SendTestcaseResultStatus.SUCCESS;
        }

        // Celery Task 실행 중 치명적 에러 발생
        // 반환 값과 관계 없이 Celery Task는 자동 종료됨
        else if (
            !result.getSuccess() &&
            result.getError() != null &&
            !result.getError().contains("런타임") &&
            !result.getError().contains("컴파일")
        ) {
            sendSseMessage(
                jobId,
                emitter,
                "error " + result.getError(),
                null
            );

            removeSubscriptionAndSetEmitterComplete(jobId);
            return SendTestcaseResultStatus.SUCCESS;
        }

        // 테스트 케이스 메시지 전송
        // 반환 값에 따라 Celery Task에서 추가 작업 여부를 판단
        else {
            Map<String, Object> data = new HashMap<>();
            data.put("success", result.getSuccess());
            data.put("runningTime", result.getRunningTime());
            data.put("memoryUsage", result.getMemoryUsage());
            data.put("codeSize", result.getCodeSize());
            data.put("error", result.getError());
            data.put("detail", result.getDetail());

            return sendSseMessage(
                jobId,
                emitter,
                data,
                String.valueOf(result.getTestcaseIndex())
            );
        }
    }

    public int executeJob(String jobId, Long userId) {
        JobRequest request = JobRequest.builder()
                .jobId(jobId)
                .userId(userId)
                .build();
        Map<String, Object> flaskResponse = sendRequestToFlask(FLASK_URL + "/job/execute", request, HttpMethod.POST);
        Map<String, Object> responseData = (Map<String, Object>) flaskResponse.get("data");

        if (responseData.get("numOfTestcase") == null) {
            throw new InvalidResponseBodyException("코딩 테스트 execute 요청에 대한 응답 본문에서 numOfTestcase를 가져올 수 없습니다.");
        }
        return (int) responseData.get("numOfTestcase");
    }

    public void cancelJob(String jobId, Long userId) {
        JobRequest request = JobRequest.builder()
                .jobId(jobId)
                .userId(userId)
                .build();

        sendRequestToFlask(FLASK_URL + "/job/cancel", request, HttpMethod.POST);
    }

    public void deleteJob(String jobId, Long userId) {
        JobRequest request = JobRequest.builder()
                .jobId(jobId)
                .userId(userId)
                .build();
        sendRequestToFlask(FLASK_URL + "/job/delete", request, HttpMethod.DELETE);
    }

    public void addSubscription(String jobId, SseEmitter emitter) {
        subscriptions.put(jobId, emitter);
    }

    public void removeSubscriptionAndSetEmitterComplete(String jobId) {
        SseEmitter emitter = subscriptions.get(jobId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (IllegalStateException e2) {
                // 이미 complete 인 경우 또 complete 되어 발생하는 로그 제거
            }
            subscriptions.remove(jobId);
        }
    }

    public SseEmitter getEmitter(String jobId) {
        return subscriptions.get(jobId);
    }

    public SendTestcaseResultStatus sendSseMessage(
        String jobId,
        SseEmitter emitter,
        Object data,
        @Nullable String id
    ) {
        try {
            if (id == null) {
                emitter.send(SseEmitter.event().data(data));
            } else {
                emitter.send(SseEmitter.event()
                        .id(id)
                        .data(data)
                );
            }
            return SendTestcaseResultStatus.SUCCESS;
        } catch (IOException e) {
            // 클라이언트와의 SSE 연결이 모종의 이유(이탈, 네트워크 장애)로 끊어져
            // 메시지를 send 할 수 없는 경우 처리
            log.info("Disconnected from client: {}", e.getMessage());
            removeSubscriptionAndSetEmitterComplete(jobId);
            return SendTestcaseResultStatus.GONE;
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("Unexpected error occurred while sending event for jobId: {}", jobId, e);
            removeSubscriptionAndSetEmitterComplete(jobId);
            return SendTestcaseResultStatus.ERROR;
        }
    }

    public List<CodeChallengeSubmission> getSubmissions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return codeChallengeSubmissionRepository.findByUser(user);
    }

    private Map<String, Object> sendRequestToFlask(String url, Object body, HttpMethod method) {
        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Api-Key", "WfTgeAS7or5aDfFbAzlsTkmvBljTaIuEk6EFCE3i4Jc=");
        headers.set("X-Client-Id", "spring-boot-server");

        // HttpEntity에 DTO와 Header 추가
        HttpEntity<Object> requestEntity;
        if (body == null) requestEntity = new HttpEntity<>(headers);
        else requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> flaskResponse = restTemplate.exchange(url, method, requestEntity, Map.class);
            Map<String, Object> response = new HashMap<>();
            response.put("status", flaskResponse.getStatusCode().value());
            response.put("data", flaskResponse.getBody()); // data는 최소 null 값 보장
            return response;
        }
        catch (HttpClientErrorException e) {
            // 4xx 응답 처리
            throw new CustomHttpClientErrorException(e, url);
        }
        catch (HttpServerErrorException e) {
            // 5xx 응답 처리
            throw new CustomHttpServerErrorException(e, url);
        }
    }
}
