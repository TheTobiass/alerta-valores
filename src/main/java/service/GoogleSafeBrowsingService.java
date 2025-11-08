package service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GoogleSafeBrowsingService {

    @Value("${google.api.key:}")
    private String apiKey;

    private String apiUrl() {
        return "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;
    }

    public boolean isUrlMalicious(String url) {
        if (url == null || url.isBlank()) return false;
        if (apiKey == null || apiKey.isBlank()) return false; // no API key configured

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

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl(), request, Map.class);

        Map responseBody = response.getBody();
        return responseBody != null && responseBody.containsKey("matches");
    }
}