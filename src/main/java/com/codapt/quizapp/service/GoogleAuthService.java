package com.codapt.quizapp.service;

import com.codapt.quizapp.service.impl.GoogleAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        if (clientId == null || clientId.isBlank()) {
            throw new GoogleAuthException("Google client ID is not configured");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                return idToken.getPayload();
            } else {
                throw new GoogleAuthException("Invalid Google token");
            }
        } catch (Exception e) {
            throw new GoogleAuthException("Failed to verify Google token", e);
        }
    }
}
