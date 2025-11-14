    // ...existing code...
package com.alertavalores.alerta_valores.controller;

import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.service.GoogleSafeBrowsingService;
import com.alertavalores.alerta_valores.service.VerificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alertavalores.alerta_valores.model.ValidacaoUrl;
import com.alertavalores.alerta_valores.repository.ValidacaoUrlRepository;

import java.util.Map;

/**
 * Exemplo de webhook para integrar provedores externos (WhatsApp, SMS Gateway, Chatbot, etc.).
 * Ajuste os campos conforme o payload real do provedor escolhido.
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private final ValidacaoUrlRepository validacaoUrlRepository;

    private final VerificacaoService verificacaoService;
    private final GoogleSafeBrowsingService googleSafeBrowsingService;

    public WebhookController(VerificacaoService verificacaoService, GoogleSafeBrowsingService googleSafeBrowsingService, ValidacaoUrlRepository validacaoUrlRepository) {
        this.verificacaoService = verificacaoService;
        this.googleSafeBrowsingService = googleSafeBrowsingService;
        this.validacaoUrlRepository = validacaoUrlRepository;
    }

    /**
     * Endpoint exemplo que recebe uma mensagem textual e canal e retorna a análise.
     * Payload esperado (JSON): { "mensagem": "texto recebido", "canal": "whatsapp" }
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<ResultadoAnalise> receberWhatsApp(@RequestBody AnaliseUrl entrada) {
        ResultadoAnalise resultado = verificacaoService.analisar(entrada);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para verificar se uma URL é segura usando o Google Safe Browsing.
     * Payload esperado (JSON): { "url": "https://example.com" }
     */
    @PostMapping("/verificar-url")
    public ResponseEntity<?> verificarUrl(@RequestBody Map<String, String> payload) {
        String url = payload.get("url");
        String erro = null;
        Boolean segura = null;
        String detalhes = null;

        if (url == null || url.trim().isEmpty()) {
            erro = "URL não fornecida ou inválida.";
            detalhes = erro;
            segura = null;
            ValidacaoUrl validacao = new ValidacaoUrl(url, segura, detalhes, erro);
            validacaoUrlRepository.save(validacao);
            return ResponseEntity.badRequest().body(Map.of(
                "erro", erro,
                "detalhes", detalhes,
                "rawResponse", null
            ));
        }

        try {
            GoogleSafeBrowsingService.UrlCheckResult resultado = googleSafeBrowsingService.checkUrl(url);
            if (resultado.malicious == null) {
                erro = resultado.detail;
                detalhes = resultado.detail;
                segura = null;
                ValidacaoUrl validacao = new ValidacaoUrl(url, segura, detalhes, erro);
                validacaoUrlRepository.save(validacao);
                return ResponseEntity.status(503).body(Map.of(
                    "erro", erro,
                    "detalhes", detalhes,
                    "rawResponse", resultado.rawResponse
                ));
            }

            segura = !resultado.malicious;
            detalhes = resultado.detail;
            ValidacaoUrl validacao = new ValidacaoUrl(url, segura, detalhes, null);
            validacaoUrlRepository.save(validacao);

            return ResponseEntity.ok(Map.of(
                "url", url,
                "segura", segura,
                "detalhes", detalhes,
                "rawResponse", resultado.rawResponse
            ));
        } catch (Exception ex) {
            erro = "Erro interno ao processar a URL: " + ex.getMessage();
            detalhes = erro;
            segura = null;
            ValidacaoUrl validacao = new ValidacaoUrl(url, segura, detalhes, erro);
            validacaoUrlRepository.save(validacao);
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "erro", erro,
                "detalhes", detalhes,
                "rawResponse", null
            ));
        }
    }

    /**
     * Endpoint para listar todas as validações de URL salvas no banco H2.
     */
    @GetMapping("/validacoes")
    public ResponseEntity<?> listarValidacoes() {
        return ResponseEntity.ok(validacaoUrlRepository.findAll());
    }
}
