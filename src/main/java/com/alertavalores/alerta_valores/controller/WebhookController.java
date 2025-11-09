package com.alertavalores.alerta_valores.controller;

import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.service.VerificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exemplo de webhook para integrar provedores externos (WhatsApp, SMS Gateway, Chatbot, etc.).
 * Ajuste os campos conforme o payload real do provedor escolhido.
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final VerificacaoService verificacaoService;

    @Autowired
    public WebhookController(VerificacaoService verificacaoService) {
        this.verificacaoService = verificacaoService;
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
}
