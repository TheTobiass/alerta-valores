package com.alertavalores.alerta_valores.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

@ExtendWith(MockitoExtension.class)
public class VerificacaoServiceTest {

    @Mock
    private GoogleSafeBrowsingService googleSafeBrowsingService;

    @InjectMocks
    private VerificacaoService verificacaoService;

    @Test
    void deveIdentificarUrlNaoOficial() {
        AnaliseUrl request = new AnaliseUrl();
        request.setUrl("https://site-falso.com/valores-receber");
        request.setCanal("whatsapp");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("único site oficial"));
    }

    @Test
    void deveIdentificarSolicitacaoDePagamento() {
        AnaliseUrl request = new AnaliseUrl();
        request.setMensagem("Para liberar seu valor, faça um PIX de R$ 10");
        request.setCanal("sms");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("TOTALMENTE GRATUITO"));
    }

    @Test
    void deveIdentificarSolicitacaoDeDadosPessoais() {
        AnaliseUrl request = new AnaliseUrl();
        request.setMensagem("Confirme seus dados pessoais e CPF para receber");
        request.setCanal("email");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("NUNCA envia links nem solicita dados pessoais"));
    }

    @Test
    void deveIdentificarMencaoCartaoCredito() {
        AnaliseUrl request = new AnaliseUrl();
        request.setMensagem("Você tem um recall do cartão de crédito para receber");
        request.setCanal("whatsapp");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("NÃO existe opção de receber valores pelo uso de cartões"));
    }

    @Test
    void deveIdentificarValoresEspecificos() {
        AnaliseUrl request = new AnaliseUrl();
        request.setMensagem("Você tem R$ 5.000,00 disponível para saque");
        request.setCanal("sms");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("nunca informa valores específicos"));
    }

    @Test
    void deveAprovarMensagemLegitima() {
        AnaliseUrl request = new AnaliseUrl();
        request.setUrl("https://valoresareceber.bcb.gov.br");
        request.setMensagem("Para consultar valores a receber, acesse o site oficial do Banco Central");
        request.setCanal("email");

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertTrue(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("✅"));
    }

    @Test
    void deveIdentificarUrlMaliciosa() {
        AnaliseUrl request = new AnaliseUrl();
        request.setUrl("http://site-malicioso.com");
        request.setCanal("whatsapp");

        when(googleSafeBrowsingService.isUrlMalicious(anyString())).thenReturn(true);

        ResultadoAnalise resultado = verificacaoService.analisar(request);

        assertFalse(resultado.getSeguro());
        assertTrue(resultado.getDetalhe().contains("único site oficial"));
    }
}