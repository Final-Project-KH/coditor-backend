package com.kh.totalproject.dto.response;


import com.kh.totalproject.constant.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResponse {
    private Long userId; // user ID 필요?
    private String email;
    private String nickname;
    private UserStatus userStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
}
