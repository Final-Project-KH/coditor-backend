package com.kh.totalproject.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoOAuthConfig {
    private final Dotenv dotenv = Dotenv.configure().load();

    public String getKakaoClientId() {
        return dotenv.get("KAKAO_CLIENT_ID");
    }

    public String getKakaoClientSecret() {
        return dotenv.get("KAKAO_CLIENT_SECRET");
    }
}
