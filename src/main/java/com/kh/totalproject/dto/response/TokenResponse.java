package com.kh.totalproject.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String grantType;
    private String accessToken;
    private Long accessTokenExpiresIn; //
    private String refreshToken; // Refresh Token
    private Long refreshTokenExpiresIn;
}
