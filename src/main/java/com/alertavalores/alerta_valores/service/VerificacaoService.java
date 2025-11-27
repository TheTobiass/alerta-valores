package com.alertavalores.alerta_valores.service;

import org.springframework.stereotype.Service;
import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.repository.ResultadoAnaliseRepository;
import java.text.Normalizer;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VerificacaoService {

    @Autowired
    private GoogleSafeBrowsingService googleSafeBrowsingService;

    @Autowired
    private ResultadoAnaliseRepository resultadoAnaliseRepository;

    public ResultadoAnalise analisar(AnaliseUrl req) {
        ResultadoAnalise r = new ResultadoAnalise();
        r.setUrl(req.getUrl());

        // Se o campo de URL estiver preenchido, só valida a URL
        if (req.getUrl() != null && !req.getUrl().isEmpty()) {
            GoogleSafeBrowsingService.UrlCheckResult urlResult = googleSafeBrowsingService.checkUrl(req.getUrl());
            r.setUrlSuspeita(urlResult.malicious);
            r.setUrlDetalhe(urlResult.detail);

            if (Boolean.TRUE.equals(urlResult.malicious)) {
                r.setSeguro(false);
                r.setDetalhe("A URL fornecida aparenta ser maliciosa segundo verificação de segurança.");
            } else {
                r.setSeguro(true);
                r.setDetalhe("Nenhum indício de golpe encontrado.");
            }
        } else {
            // Se não houver URL, valida apenas o texto
            boolean textoSuspeito = contemPalavrasDeGolpe(req.getMensagem());
            if (textoSuspeito) {
                r.setSeguro(false);
                r.setDetalhe(
                    "Possível golpe detectado! Mensagem contém dados pessoais ou solicitações suspeitas.\n" +
                    "O único site oficial para consulta de valores a receber é: https://valoresareceber.bcb.gov.br\n" +
                    "O Banco Central não envia links nem solicita dados pessoais por e-mail, SMS, WhatsApp ou Telegram.\n" +
                    "Somente a instituição que aparece na consulta pode contatar o cliente, mas nunca irá pedir dados pessoais ou senha.\n" +
                    "Dicas:\n" +
                    "- NÃO clicar em links suspeitos enviados por e-mail, SMS, WhatsApp ou Telegram;\n" +
                    "- NÃO fazer qualquer tipo de pagamento para ter acesso aos valores;\n" +
                    "- NÃO existe a opção de receber algum valor pelo uso de cartões de crédito. Não há lei ou norma do BC sobre recall de cartões de crédito."
                );
            } else {
                r.setSeguro(true);
                r.setDetalhe("Nenhum indício de golpe encontrado.");
            }
        }

        resultadoAnaliseRepository.save(r);
        return r;
    }

    private boolean contemPalavrasDeGolpe(String texto) {
        if (texto == null) return false;
        String t = texto.toLowerCase();

        String[] palavrasChave = {
            "nome", "nome completo", "sobrenome", "cpf", "rg", "chave pix", "telefone"
        };

        for (String palavra : palavrasChave) {
            // Remove acentos e espaços extras para melhorar a busca
            String normalizada = Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("\\s+", " ");
            String palavraNormalizada = Normalizer.normalize(palavra, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("\\s+", " ");
            if (normalizada.contains(palavraNormalizada)) {
                return true;
            }
        }
        return false;
    }
}