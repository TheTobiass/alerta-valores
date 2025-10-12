package service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

@Service
public class GoogleSafeBrowsingService {
    private static final String API_KEY;
    
    static {
        Properties properties = new Properties();
        try (InputStream input = GoogleSafeBrowsingService.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
            API_KEY = properties.getProperty("GOOGLE_API_KEY");
        } catch (IOException e) {
            throw new RuntimeException("Error loading API key", e);
        }
    }
    private static final String API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + API_KEY;

    public boolean isUrlMalicious(String url) {
        if (url == null || url.isBlank()) return false;

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = Map.of(
            "client", Map.of("clientId", "alertavalores", "clientVersion", "1.0"),
            "threatInfo", Map.of(
                "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                "platformTypes", List.of("ANY_PLATFORM"),
                "threatEntryTypes", List.of("URL"),
                "threatEntries", List.of(Map.of("url", url))
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        Map responseBody = response.getBody();
        return responseBody != null && responseBody.containsKey("matches");
    }
}