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
public class UserInfoResponse {
    private Long id;
    private String userId;
    private String email;
    private String nickname;
    private Role role;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;

    public static UserInfoResponse of(User user){
        return UserInfoResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .registeredAt(user.getRegisteredAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
