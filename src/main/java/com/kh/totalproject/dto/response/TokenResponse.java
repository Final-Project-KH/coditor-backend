/* 로그인시 필요한 Token 데이터 전송 Dto */
package com.kh.totalproject.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String grantType;   // Bearer
    private String accessToken; // Access Token
    private String refreshToken; // Refresh Token
}
