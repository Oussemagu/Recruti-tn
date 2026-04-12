package tn.recruti.recruti_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import tn.recruti.recruti_backend.security.JwtUtil;
import java.util.Map;

/**
 * Handles Google Calendar OAuth2 consent flow.
 * Completely separate from AuthController — zero risk of interference.
 */
@Slf4j
@RestController
@RequestMapping("/api/google") // ← different base URL, nothing shared
@RequiredArgsConstructor
public class GoogleCalendarAuthController {

        private final GoogleAuthorizationCodeFlow flow;
        private final JwtUtil jwtUtil;

        @Value("${google.redirect-uri}")
        private String redirectUri;

        /**
         * GET /api/google/connect
         * Recruiter clicks "Connect Google Calendar" in Angular.
         * Returns the Google consent URL to redirect to.
         */
        @GetMapping("/connect")
        public ResponseEntity<Map<String, String>> getGoogleAuthUrl(
                        @RequestHeader("Authorization") String bearerToken) throws Exception {

                String recruiterId = jwtUtil.extractId(bearerToken.substring(7));

                String url = flow.newAuthorizationUrl()
                                .setRedirectUri(redirectUri)
                                .setState(recruiterId)
                                .build();
                log.info("Generated Google Calendar auth URL for recruiter ID: {}", recruiterId);
                return ResponseEntity.ok(Map.of("url", url));
        }

        /**
         * GET /api/google/callback
         * Google redirects here after recruiter approves consent.
         */
        @GetMapping("/callback")
        public ResponseEntity<String> handleCallback(
                        @RequestParam String code,
                        @RequestParam String state) throws Exception {

                flow.createAndStoreCredential(
                                flow.newTokenRequest(code)
                                                .setRedirectUri(redirectUri)
                                                .execute(),
                                state);
                log.info("Google Calendar connected for recruiter ID: {}", state);
                return ResponseEntity.ok("Google Calendar connecté. Vous pouvez fermer cet onglet.");
        }
}