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

                String html = """
                                <!DOCTYPE html>
                                <html lang="en">
                                <head>
                                  <meta charset="UTF-8"/>
                                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                                  <title>Google Calendar Connected</title>
                                  <style>
                                    * { margin: 0; padding: 0; box-sizing: border-box; }
                                    body {
                                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                        background: #f0f7ff;
                                        min-height: 100vh;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        padding: 2rem;
                                    }
                                    .card {
                                        background: #fff;
                                        border-radius: 24px;
                                        box-shadow: 0 10px 25px rgba(0,0,0,0.05);
                                        padding: 3.5rem 2.5rem;
                                        max-width: 440px;
                                        width: 100%;
                                        text-align: center;
                                    }
                                    .icon {
                                        width: 80px;
                                        height: 80px;
                                        background: #e7f1ff;
                                        border-radius: 50%;
                                        margin: 0 auto 1.5rem;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                    }
                                    h1 {
                                        font-size: 22px;
                                        font-weight: 600;
                                        color: #1a1c1e;
                                        margin-bottom: 0.75rem;
                                    }
                                    p {
                                        font-size: 15px;
                                        color: #444746;
                                        line-height: 1.6;
                                        margin-bottom: 2rem;
                                    }
                                    .info-box {
                                        background: #f8fbff;
                                        border: 1px solid #dceeff;
                                        border-radius: 12px;
                                        padding: 1.25rem;
                                        text-align: left;
                                        margin-bottom: 2rem;
                                    }
                                    .info-row {
                                        display: flex;
                                        align-items: center;
                                        gap: 10px;
                                        margin-bottom: 8px;
                                    }
                                    .info-row:last-child { margin-bottom: 0; }
                                    .badge {
                                        margin-left: auto;
                                        font-size: 11px;
                                        font-weight: 600;
                                        background: #0061a4;
                                        color: #ffffff;
                                        padding: 2px 10px;
                                        border-radius: 20px;
                                        text-transform: uppercase;
                                    }
                                    .info-text { font-size: 13px; color: #444746; }
                                    .info-title { font-size: 14px; font-weight: 600; color: #001d35; }
                                    button {
                                        width: 100%;
                                        padding: 0.85rem;
                                        background: #0061a4;
                                        color: white;
                                        border: none;
                                        border-radius: 12px;
                                        font-size: 15px;
                                        font-weight: 600;
                                        cursor: pointer;
                                        transition: background 0.2s ease;
                                    }
                                    button:hover { background: #004a7d; }
                                  </style>
                                </head>
                                <body>
                                  <div class="card">
                                    <div class="icon">
                                      <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
                                        <path d="M5 13l4 4L19 7" stroke="#0061a4" stroke-width="2.5"
                                              stroke-linecap="round" stroke-linejoin="round"/>
                                      </svg>
                                    </div>
                                    <h1>Google Calendar Connected</h1>
                                    <p>Your Google account has been successfully authorized.<br/>
                                       You can now return to the app to finish scheduling your interview.</p>
                                    <div class="info-box">
                                      <div class="info-row">
                                        <span class="info-title">Google Calendar</span>
                                        <span class="badge">Active</span>
                                      </div>
                                      <div class="info-row">
                                        <span class="info-text">Event creation permissions granted</span>
                                      </div>
                                    </div>
                                    <button onclick="window.close()">Close this tab</button>
                                  </div>
                                </body>
                                </html>
                                """;

                return ResponseEntity.ok()
                                .header("Content-Type", "text/html; charset=UTF-8")
                                .body(html);
        }
}