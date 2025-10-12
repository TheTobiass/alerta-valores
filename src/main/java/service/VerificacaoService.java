package service;

import org.springframework.stereotype.Service;

import model.AnaliseUrl;
import model.ResultadoAnalise;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VerificacaoService {

    @Autowired
    private GoogleSafeBrowsingService googleSafeBrowsingService;

    public ResultadoAnalise analisar(AnaliseUrl req) {
        boolean urlSuspeita = googleSafeBrowsingService.isUrlMalicious(req.getUrl());
        boolean textoSuspeito = contemPalavrasDeGolpe(req.getMensagem());

        ResultadoAnalise resultado;

        if (urlSuspeita || textoSuspeito) {
            resultado = new ResultadoAnalise();

            resultado.setSeguro(false);
            resultado.setDetalhe("""
                Detectamos indícios de golpe envolvendo valores a receber.
                O site do Banco Central nunca informa valores exatos nem solicita CPF por mensagens.
                """);
        } else {
            resultado = new ResultadoAnalise();
            resultado.setSeguro(true);
            resultado.setDetalhe("Nenhum indício de golpe encontrado.");
        }

        return resultado;
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