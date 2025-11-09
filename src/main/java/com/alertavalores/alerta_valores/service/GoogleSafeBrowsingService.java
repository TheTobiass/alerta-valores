package com.alertavalores.alerta_valores.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GoogleSafeBrowsingService {

    @Value("${google.api.key:}")
    private String apiKey;

    @Autowired(required = false)
    private RestTemplate restTemplate; // usa o bean com timeout se existir

    private RestTemplate rt() {
        return restTemplate != null ? restTemplate : new RestTemplate();
    }

    private String apiUrl() {
        return "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;
    }

    public static class UrlCheckResult {
        public final Boolean malicious; // null = não verificado
        public final String detail;
        public UrlCheckResult(Boolean malicious, String detail) {
            this.malicious = malicious;
            this.detail = detail;
        }
    }

    public UrlCheckResult checkUrl(String url) {
        if (url == null || url.isBlank()) {
            return new UrlCheckResult(null, null);
        }
        if (apiKey == null || apiKey.isBlank()) {
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
            ResponseEntity<Map> response = rt().postForEntity(apiUrl(), request, Map.class);
            Map responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("matches")) {
                // extrai tipos de ameaça para detalhar
                Object matches = responseBody.get("matches");
                Set<String> types = new LinkedHashSet<>();
                if (matches instanceof Collection<?> coll) {
                    for (Object m : coll) {
                        if (m instanceof Map<?, ?> mm) {
                            Object tt = mm.get("threatType");
                            if (tt != null) types.add(String.valueOf(tt));
                        }
                    }
                }
                String detail = types.isEmpty()
                    ? "A URL informada apresenta correspondência em listas de ameaças (Google Safe Browsing)."
                    : "A URL informada consta como ameaça: " + String.join(", ", types) + ".";
                return new UrlCheckResult(true, detail);
            }
            return new UrlCheckResult(false, "A URL informada não possui registros de ameaça conhecidos no momento.");
        } catch (RestClientResponseException ex) {
            int code = ex.getRawStatusCode();
            String body = sanitize(ex.getResponseBodyAsString());
            String base = "Não foi possível verificar a URL (Google Safe Browsing). ";
            String hint;
            if (code == 403 && body != null && body.toLowerCase().contains("has not been used")) {
                hint = "A API Safe Browsing não está habilitada neste projeto/chave. Ative em Google Cloud Console: APIs & Services > Library > Safe Browsing API (Enable), confirme billing, e aguarde alguns minutos. Depois, teste novamente.";
            } else if (code == 403) {
                hint = "Acesso negado (403). Verifique se a chave é válida, se a API está habilitada e as restrições da chave (HTTP referrer/IP).";
            } else if (code == 400) {
                hint = "Requisição inválida (400). Verifique o formato da URL e a chave da API.";
            } else if (code == 429) {
                hint = "Limite de cota excedido (429). Tente novamente mais tarde ou ajuste as cotas no Console.";
            } else if (code >= 500) {
                hint = "Serviço do Google indisponível no momento (" + code + "). Tente novamente mais tarde.";
            } else {
                hint = "Erro " + code + ".";
            }
            return new UrlCheckResult(null, base + hint);
        } catch (RestClientException ex) {
            // Não propaga 500; retorna estado "não verificado" com detalhe para depuração controlada
            return new UrlCheckResult(null, "Não foi possível verificar a URL (Google Safe Browsing): " + sanitize(ex.getMessage()));
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