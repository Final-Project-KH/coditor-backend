/* 커뮤니티 스터디, 프로젝트 게시글 상태 (모집중 / 모집완료) */
package com.kh.totalproject.constant;
import lombok.Getter;

@Getter
public enum Status {
    ACTIVE,
    INACTIVE;


    public static Status fromString(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 Stat 타입입니다: " + status);
        }
    }
}
