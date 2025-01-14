package com.kh.totalproject.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TokenResponse {
    private String grantType;
    private String accessToken; // Access Token
    private String refreshToken; // Refresh Token
    private boolean isNewUser;  // 신규 사용자 여부 추가

}
