package com.alertavalores.alerta_valores.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class GoogleSafeBrowsingService {

    @Value("${google.api.key:}")
    private String apiKey;

    @Autowired(required = false)
    private RestTemplate restTemplate; // usa o bean com timeout se existir

    private RestTemplate rt() {
        return restTemplate != null ? restTemplate : new RestTemplate();
    }

    private static final String API_URL_V4 = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=";
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final long expiration;

        CacheEntry(long expiration) {
            this.expiration = expiration;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiration;
        }
    }

    private String apiUrl() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chave da API do Google não configurada.");
        }
        return API_URL_V4 + apiKey;
    }

    public static class UrlCheckResult {
        public final Boolean malicious; // null = não verificado
        public final String detail;
        public final Object rawResponse;
        public UrlCheckResult(Boolean malicious, String detail) {
            this(malicious, detail, null);
        }
        public UrlCheckResult(Boolean malicious, String detail, Object rawResponse) {
            this.malicious = malicious;
            this.detail = detail;
            this.rawResponse = rawResponse;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(GoogleSafeBrowsingService.class);

    public UrlCheckResult checkUrl(String url) {
        logger.info("Iniciando verificação da URL: {}", url);
        if (url == null || url.isBlank()) {
            logger.warn("URL não fornecida ou inválida.");
            return new UrlCheckResult(null, null);
        }
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("Chave da API do Google não configurada.");
            return new UrlCheckResult(null, "Verificação de URL indisponível: chave da API do Google não configurada.");
        }

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

        try {
            String nonNullApiUrl = apiUrl();
            if (nonNullApiUrl == null) {
                throw new IllegalStateException("A URL da API não pode ser nula.");
            }

            HttpMethod nonNullHttpMethod = HttpMethod.POST;
            if (nonNullHttpMethod == null) {
                throw new IllegalStateException("O método HTTP não pode ser nulo.");
            }

            ResponseEntity<Map<String, Object>> response = rt().exchange(
                nonNullApiUrl,
                nonNullHttpMethod,
                request,
                new ParameterizedTypeReference<>() {}
            );
            logger.info("Resposta recebida da API: {}", response.getBody());
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("matches")) {
                if (responseBody.get("matches") instanceof Collection<?> matches) {
                    for (Object match : matches) {
                        if (match instanceof Map<?, ?> matchMap) {
                            Object fullHash = matchMap.get("fullHash");
                            if (fullHash != null) {
                                localCache.put(generateHash(url), new CacheEntry(System.currentTimeMillis() + 3600000));
                                return new UrlCheckResult(true, "A URL foi identificada como maliciosa.", responseBody);
                            }
                        }
                    }
                }
            }
            return new UrlCheckResult(false, "A URL informada não possui registros de ameaça conhecidos no momento.", responseBody);
        } catch (RestClientResponseException ex) {
            logger.error("Erro na resposta da API: {}", ex.getMessage());
            return handleApiError(ex);
        } catch (RestClientException ex) {
            logger.error("Erro na comunicação com a API: {}", ex.getMessage());
            return new UrlCheckResult(null, "Não foi possível verificar a URL (Google Safe Browsing): " + sanitize(ex.getMessage()));
        }
    }

    private UrlCheckResult handleApiError(RestClientResponseException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String responseBodySanitized = sanitize(ex.getResponseBodyAsString());
        String base = "Não foi possível verificar a URL (Google Safe Browsing). ";
        String hint;
        if (status.isSameCodeAs(HttpStatus.FORBIDDEN) && responseBodySanitized != null && responseBodySanitized.toLowerCase().contains("has not been used")) {
            hint = "A API Safe Browsing não está habilitada neste projeto/chave. Ative em Google Cloud Console: APIs & Services > Library > Safe Browsing API (Enable), confirme billing, e aguarde alguns minutos. Depois, teste novamente.";
        } else if (status.isSameCodeAs(HttpStatus.FORBIDDEN)) {
            hint = "Acesso negado (403). Verifique se a chave é válida, se a API está habilitada e as restrições da chave (HTTP referrer/IP).";
        } else if (status.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            hint = "Requisição inválida (400). Verifique o formato da URL e a chave da API.";
        } else if (status.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
            hint = "Limite de cota excedido (429). Tente novamente mais tarde ou ajuste as cotas no Console.";
        } else if (status.is5xxServerError()) {
            hint = "Serviço do Google indisponível no momento (" + status + "). Tente novamente mais tarde.";
        } else {
            hint = "Erro " + (status != null ? status.value() : "desconhecido") + ".";
        }
        return new UrlCheckResult(null, base + hint);
    }

    private String generateHash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }

    // Evita vazar mensagens longas/sigilosas
    private String sanitize(String msg) {
        if (msg == null) return "erro desconhecido";
        msg = msg.replaceAll("\n|\r", " ");
        return msg.length() > 200 ? msg.substring(0, 200) + "..." : msg;
    }

    // Método legado mantido por compatibilidade
    public boolean isUrlMalicious(String url) {
        UrlCheckResult r = checkUrl(url);
        return Boolean.TRUE.equals(r.malicious);
    }
}