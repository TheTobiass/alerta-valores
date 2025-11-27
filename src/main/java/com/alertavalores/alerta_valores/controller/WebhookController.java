    // ...existing code...
package com.alertavalores.alerta_valores.controller;

import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.service.VerificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alertavalores.alerta_valores.model.ValidacaoUrl;
import com.alertavalores.alerta_valores.repository.ValidacaoUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Exemplo de webhook para integrar provedores externos (WhatsApp, SMS Gateway, Chatbot, etc.).
 * Ajuste os campos conforme o payload real do provedor escolhido.
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final ValidacaoUrlRepository validacaoUrlRepository;
    private final VerificacaoService verificacaoService;

    public WebhookController(VerificacaoService verificacaoService, ValidacaoUrlRepository validacaoUrlRepository) {
        this.verificacaoService = verificacaoService;
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
     * Payload esperado (JSON): { "url": "https://example.com", "message": "texto opcional" }
     * Retorna 400 se a mensagem contiver padrões de golpe, 200 se seguro, 503 se erro de API
     */
    @PostMapping("/verificar-url")
    public ResponseEntity<?> verificarUrl(@RequestBody Map<String, String> payload) {
        logger.info("Iniciando verificação de URL via webhook");
        String url = payload.get("url");
        // Aceita tanto "message" (English) quanto "mensagem" (Portuguese)
        String message = payload.get("message");
        if (message == null) {
            message = payload.get("mensagem");
        }
        String erro = null;
        Boolean segura = null;
        String detalhes = null;

        // Validações básicas
        if ((url == null || url.trim().isEmpty()) && (message == null || message.trim().isEmpty())) {
            erro = "URL ou mensagem não fornecida.";
            detalhes = erro;
            segura = null;
            logger.warn("URL e mensagem vazias na requisição");
            salvarValidacaoAssincrona(url, segura, detalhes, erro);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("erro", erro);
            response.put("detalhes", detalhes);
            response.put("url", url);
            response.put("message", message);
            response.put("rawResponse", null);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Criar objeto AnaliseUrl para processar
            AnaliseUrl analise = new AnaliseUrl();
            analise.setUrl(url);
            analise.setMensagem(message);
            analise.setCanal("webhook");

            // Usar o serviço para analisar
            ResultadoAnalise resultado = verificacaoService.analisar(analise);

            // Se for inseguro/suspeito, retornar 400
            if (!resultado.getSeguro()) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("url", url);
                response.put("message", message);
                response.put("segura", false);
                response.put("detalhes", resultado.getDetalhe());
                response.put("urlSuspeita", resultado.getUrlSuspeita());
                response.put("urlDetalhe", resultado.getUrlDetalhe());
                logger.warn("Mensagem/URL suspeita detectada");
                salvarValidacaoAssincrona(url, false, resultado.getDetalhe(), null);
                return ResponseEntity.badRequest().body(response);
            }

            // Se for seguro, retornar 200
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("url", url);
            response.put("message", message);
            response.put("segura", true);
            response.put("detalhes", resultado.getDetalhe());
            response.put("urlSuspeita", resultado.getUrlSuspeita() != null ? resultado.getUrlSuspeita() : false);
            response.put("urlDetalhe", resultado.getUrlDetalhe());
            logger.info("URL/mensagem verificada com sucesso: segura=true");
            salvarValidacaoAssincrona(url, true, resultado.getDetalhe(), null);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException ex) {
            erro = "Serviço de verificação indisponível: " + ex.getMessage();
            detalhes = erro;
            segura = null;
            logger.error("Erro de configuração na verificação: {}", ex.getMessage(), ex);
            salvarValidacaoAssincrona(url, segura, detalhes, erro);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("erro", erro);
            response.put("detalhes", "Verifique se a chave da API do Google está configurada corretamente");
            response.put("url", url);
            response.put("message", message);
            response.put("rawResponse", null);
            return ResponseEntity.status(503).body(response);
        } catch (Exception ex) {
            erro = "Erro interno ao processar a requisição";
            detalhes = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            segura = null;
            logger.error("Erro inesperado ao verificar: ", ex);
            salvarValidacaoAssincrona(url, segura, detalhes, erro);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("erro", erro);
            response.put("detalhes", detalhes);
            response.put("url", url);
            response.put("message", message);
            response.put("rawResponse", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Salva a validação no banco de forma assíncrona para não bloquear a resposta HTTP
     */
    private void salvarValidacaoAssincrona(String url, Boolean segura, String detalhes, String erro) {
        new Thread(() -> {
            try {
                ValidacaoUrl validacao = new ValidacaoUrl(url, segura, detalhes, erro);
                validacaoUrlRepository.save(validacao);
                logger.debug("Validação salva no banco com sucesso para URL: {}", url);
            } catch (Exception dbEx) {
                logger.warn("Falha ao salvar validação no banco de forma assíncrona (URL {}): {}", url, dbEx.getMessage());
            }
        }).start();
    }

    /**
     * Endpoint para listar todas as validações de URL salvas no banco H2.
     */
    @GetMapping("/validacoes")
    public ResponseEntity<?> listarValidacoes() {
        return ResponseEntity.ok(validacaoUrlRepository.findAll());
    }
}
