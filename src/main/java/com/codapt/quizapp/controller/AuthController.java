package com.codapt.quizapp.controller;

import com.codapt.quizapp.dto.JwtResponse;
import com.codapt.quizapp.dto.TokenRequest;
import com.codapt.quizapp.service.GoogleAuthService;
import com.codapt.quizapp.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    public AuthController(GoogleAuthService googleAuthService,
                          JwtService jwtService) {
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody TokenRequest request) {

        try {
            var payload = googleAuthService.verifyToken(request.getToken());

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // TODO: Save user in DB if not exists

            String jwt = jwtService.generateToken(email,name);

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
