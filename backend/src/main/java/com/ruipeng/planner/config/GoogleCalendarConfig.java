package com.ruipeng.planner.config;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.calendar.CalendarScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

    // Calendar 配置
    @Value("${google.calendar.application-name:Personal Financial Planner}")
    private String applicationName;

    @Value("${google.calendar.calendar-id:primary}")
    private String calendarId;

    @Value("${google.calendar.timezone:Europe/Dublin}")
    private String timeZone;

    // OAuth 配置
    @Value("${google.oauth.client-id:#{null}}")
    private String clientId;

    @Value("${google.oauth.client-secret:#{null}}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri:http://localhost:8080/api/oauth/callback}")
    private String redirectUri;

    // 常量
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarConfig.class);

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 优先使用环境变量
        if (StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret)) {
            log.info("Loading Google OAuth from environment variables");
            return new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    JSON_FACTORY,
                    clientId,
                    clientSecret,
                    SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        }

        // 备用：从文件读取
        log.info("Loading Google OAuth from file");
        return loadFromFile(httpTransport);
    }

    private GoogleAuthorizationCodeFlow loadFromFile(NetHttpTransport httpTransport) throws IOException {
        ClassPathResource resource = new ClassPathResource("financial-planner.json");

        if (!resource.exists()) {
            throw new IllegalStateException(
                    "No Google OAuth credentials found. Please either:\n" +
                            "1. Set environment variables: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET\n" +
                            "2. Provide financial-planner.json file in the classpath"
            );
        }

        try (InputStream in = resource.getInputStream()) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            return new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        }
    }

    // Getters
    public String getCalendarId() { return calendarId; }
    public String getTimeZone() { return timeZone; }
    public String getApplicationName() { return applicationName; }
    public String getRedirectUri() { return redirectUri; }
}