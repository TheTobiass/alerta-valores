package com.alertavalores.alerta_valores.service;

import org.springframework.stereotype.Service;
import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.repository.ResultadoAnaliseRepository;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VerificacaoService {

    @Autowired
    private GoogleSafeBrowsingService googleSafeBrowsingService;

    @Autowired
    private ResultadoAnaliseRepository resultadoAnaliseRepository;

    public ResultadoAnalise analisar(AnaliseUrl req) {
        boolean textoSuspeito = contemPalavrasDeGolpe(req.getMensagem());

        GoogleSafeBrowsingService.UrlCheckResult urlResult = googleSafeBrowsingService.checkUrl(req.getUrl());

        ResultadoAnalise r = new ResultadoAnalise();
        r.setUrl(req.getUrl());
        r.setUrlSuspeita(urlResult.malicious); // pode ser true/false/null
        r.setUrlDetalhe(urlResult.detail);

        boolean urlMaliciosa = Boolean.TRUE.equals(urlResult.malicious);
        if (urlMaliciosa || textoSuspeito) {
            r.setSeguro(false);
            StringBuilder sb = new StringBuilder();
            sb.append("Detectamos indícios de golpe envolvendo valores a receber.\n");
            sb.append("O site do Banco Central nunca informa valores exatos nem solicita CPF por mensagens.\n");
            if (textoSuspeito) {
                sb.append("Sinais encontrados na mensagem: palavras-chave suspeitas.\n");
            }
            if (urlMaliciosa) {
                sb.append("A URL fornecida aparenta ser maliciosa segundo verificação de segurança.\n");
            }
            r.setDetalhe(sb.toString().trim());
        } else {
            r.setSeguro(true);
            r.setDetalhe("Nenhum indício de golpe encontrado.");
        }
        resultadoAnaliseRepository.save(r);
        return r;
    }

    private boolean contemPalavrasDeGolpe(String texto) {
        if (texto == null) return false;
        String t = texto.toLowerCase();
        return t.contains("valores a receber") ||
               t.contains("banco central") ||
               t.contains("pix") ||
               t.contains("r$") ||
               t.contains("cpf") ||
               t.contains("consultar saldo") ||
               t.contains("premio");
    }

    
}