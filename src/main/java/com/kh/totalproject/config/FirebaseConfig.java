package com.kh.totalproject.config;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.http.client.HttpClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;


import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() throws IOException {
        InputStream serviceAccount =
                new ClassPathResource("firebase-service-account.json").getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket("coditor-a24fa.firebasestorage.app")
                .build();

        if (FirebaseApp.getApps().isEmpty()){
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase has been initialized successfully!");
        }
    }

}
