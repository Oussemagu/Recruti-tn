//this file do one critical thing
// reads the client-id and client-secret from application.properties (which in turn reads from .env)
// from a google cloud project with Calendar API enabled
// performs the OAuth2 handshake with Google to obtain the necessary credentials
// and finally builds and exposes a Calendar client as a Spring Bean
//Without it, when InterviewService tries to use googleCalendarClient, 
//Spring has nothing to inject and the app crashes on startup with:No qualifying bean of type 'com.google.api.services.calendar.Calendar'
package tn.recruti.recruti_backend.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

        // ── Values from application.properties ───────────────────────────────────

        @Value("${google.client-id}")
        private String clientId;

        @Value("${google.client-secret}")
        private String clientSecret;

        @Value("${google.tokens-directory}")
        private String tokensDirectory;

        @Value("${google.application-name}")
        private String applicationName;

        @Value("${google.redirect-uri}")
        private String redirectUri;

        // Calendar scope is all we need to create events and generate Meet links
        private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR);

        // ── Beans ─────────────────────────────────────────────────────────────────

        /**
         * The OAuth2 flow bean.
         *
         * Exposed as a bean so both GoogleCalendarConfig and GoogleAuthController
         * share the exact same flow instance — this is important because the token
         * saved during the /callback must be readable when building the Calendar
         * client.
         */
        @Bean
        public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow()
                        throws GeneralSecurityException, IOException {

                NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                // .setWeb() — correct for Angular + Spring Boot running in Docker.
                // Unlike .setInstalled(), this does NOT use LocalServerReceiver or port 8888.
                // The OAuth2 redirect is handled by a real Spring Boot @GetMapping controller.
                GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                                .setClientId(clientId)
                                .setClientSecret(clientSecret);
                GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(details);

                return new GoogleAuthorizationCodeFlow.Builder(
                                transport, jsonFactory, clientSecrets, SCOPES)
                                // Saves the token to ./tokens after first consent.
                                // Mount this folder as a Docker volume so it survives container restarts.
                                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectory)))
                                // offline = gets a refresh token so the session never expires silently
                                .setAccessType("offline")
                                .build();
        }

}
