package com.kh.totalproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.totalproject.constant.SseSendResultStatus;
import com.kh.totalproject.dto.flask.callback.TestcaseResult;
import com.kh.totalproject.dto.flask.request.ExecuteJobRequest;
import com.kh.totalproject.dto.flask.response.CreateJobResponse;
import com.kh.totalproject.dto.flask.response.ExecuteJobResponse;
import com.kh.totalproject.dto.request.SubmitCodeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Transactional
@RequiredArgsConstructor
@Service
@Slf4j
public class CodeChallengeService {
    // Java에서 멀티 스레드 환경에서 효율적으로 동작하도록 설계된 Map 인터페이스의 구현체
    private final ConcurrentHashMap<String, SseEmitter> subscriptions = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final String FLASK_URL = "http://127.0.0.1:5000";

    public CreateJobResponse submit(SubmitCodeRequest dto) {
        Map<String, Object> flaskResponse = sendRequestToFlask(FLASK_URL + "/job/create", dto, HttpMethod.POST);
        CreateJobResponse result = new CreateJobResponse();

        int status = (int) flaskResponse.get("status");
        result.setStatus(status);
        if (status == 201) {
            Map<String, Object> data = (Map<String, Object>) flaskResponse.get("data");
            String jobId = (String) data.get("jobId");
            result.setStatus(200);
            result.setJobId(jobId);
        } else if (status == 400) {
            result.setError("잘못된 요청 형식입니다.");
        } else if (status == 404) {
            result.setError("존재하지 않는 코딩 테스트 문제입니다.");
        } else if (status == 422) {
            result.setError("동시 채점은 회원 당 최대 2개로 제한됩니다");
        } else {
            result.setError("서버에 오류가 발생하였습니다.");
        }
        return result;
    }

    public void addSubscription(String jobId, SseEmitter emitter) {
        subscriptions.put(jobId, emitter);
    }

    public void removeSubscription(String jobId, SseEmitter emitter) {
        subscriptions.remove(jobId);
    }

    public SseSendResultStatus sendTestcaseResult(String jobId, TestcaseResult result) {
        SseEmitter emitter = subscriptions.get(jobId);
        if (emitter == null) {
            return SseSendResultStatus.CLIENT_NOT_FOUND;
        }

        try {
            if (
                result.getSuccess() &&
                result.getDetail() != null &&
                result.getDetail().contains("complete")
            ) {
                emitter.send(SseEmitter.event().data("complete"));
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("success", result.getSuccess());
                data.put("runningTime", result.getRunningTime());
                data.put("memoryUsage", result.getMemoryUsage());
                data.put("codeSize", result.getCodeSize());
                data.put("error", result.getError());
                data.put("detail", result.getDetail());

                emitter.send(SseEmitter.event()
                        .id(String.valueOf(result.getTestcaseIndex()))
                        .data(data)
                );
            }
            // 실패 시 error가 포함된 경우 front-end는 연결을 중단하고 에러 메시지 알림과 함께 UI 업데이트
            // 또한 모든 테스트 케이스를 다 받은 경우에도 연결 중단 및 완료 처리
        } catch (IOException e) {
            log.info("error: {}", e.getMessage());
            emitter.completeWithError(e);
            subscriptions.remove(jobId);
            return SseSendResultStatus.ERROR;
        }

        return SseSendResultStatus.SUCCESS;
    }

    public ExecuteJobResponse executeCode(String jobId, Long userId) {
        ExecuteJobRequest request = ExecuteJobRequest.builder()
                .jobId(jobId)
                .userId(userId)
                .build();
        Map<String, Object> flaskResponse = sendRequestToFlask(FLASK_URL + "/job/execute", request, HttpMethod.POST);
        ExecuteJobResponse result = new ExecuteJobResponse();

        int status = (int) flaskResponse.get("status");
        result.setStatus(status);
        Map<String, Object> data = (Map<String, Object>) flaskResponse.get("data");
        if ((Boolean) (data.get("success"))) {
            int numOfTestcase = (int) data.get("numOfTestcase");
            result.setNumOfTestcase(numOfTestcase);
        } else {
            result.setError((String) data.get("error"));
        }
        return result;
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
            response.put("data", flaskResponse.getBody());
            return response;
        } catch (HttpClientErrorException e) {
            // 예외 발생 시 상태 코드와 본문 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", e.getStatusCode().value());

            try {
                // 응답 본문이 JSON 형식인 경우 변환
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> responseBody = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
                errorResponse.put("data", responseBody);
            } catch (Exception parseException) {
                // JSON 파싱 실패 시 원본 문자열을 본문으로 반환
                errorResponse.put("data", e.getResponseBodyAsString());
            }

            return errorResponse;
        }
    }
}
