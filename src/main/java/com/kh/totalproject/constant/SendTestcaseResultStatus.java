package com.kh.totalproject.constant;

// SUCCESS 응답이 아닌 경우 celery task는 자동 종료됨
public enum SendTestcaseResultStatus {
    SUCCESS, CLIENT_NOT_FOUND, ERROR, GONE
}
