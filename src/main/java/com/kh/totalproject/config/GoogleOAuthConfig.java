package com.kh.totalproject.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleOAuthConfig {
    private final Dotenv dotenv = Dotenv.configure().load();

    public String getGoogleClientId() {
        return dotenv.get("GOOGLE_CLIENT_ID");
    }

    public String getGoogleClientSecret() {
        return dotenv.get("GOOGLE_CLIENT_SECRET");
    }
}

