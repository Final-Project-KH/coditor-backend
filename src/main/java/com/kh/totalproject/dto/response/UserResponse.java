package com.kh.totalproject.dto.response;


import com.kh.totalproject.constant.Role;
import com.kh.totalproject.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String userId;
    private String email;
    private String nickname;
    private Role role;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
    private String profileUrl;

    // 내정보 보기 읽기전용 OfAll
    public static UserResponse ofAll(User user) {
        return UserResponse.builder()
                .id(user.getUserKey())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .registeredAt(user.getRegisteredAt())
                .updatedAt(user.getUpdatedAt())
                .profileUrl(user.getProfileUrl())
                .build();
    }

    // 이메일을 통한 ID 찾기시 읽기전용 OfUserId
    public static UserResponse ofUserId(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .registeredAt(user.getRegisteredAt())
                .email(user.getEmail())
                .build();
    }
}
