package com.kh.totalproject.dto.response;

import com.kh.totalproject.entity.User;
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

    public static TokenResponse ofAccessToken(TokenResponse tokenresponse) {
        return TokenResponse.builder()
                .grantType(tokenresponse.getGrantType())
                .accessToken(tokenresponse.getAccessToken())
                .build();
    }
}
