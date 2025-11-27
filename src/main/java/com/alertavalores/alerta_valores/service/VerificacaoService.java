package com.alertavalores.alerta_valores.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alertavalores.alerta_valores.model.AnaliseUrl;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import com.alertavalores.alerta_valores.repository.ResultadoAnaliseRepository;
import java.text.Normalizer;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class VerificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(VerificacaoService.class);

    @Autowired
    private GoogleSafeBrowsingService googleSafeBrowsingService;

    @Autowired
    private ResultadoAnaliseRepository resultadoAnaliseRepository;

    public ResultadoAnalise analisar(AnaliseUrl req) {
        ResultadoAnalise r = new ResultadoAnalise();
        r.setUrl(req.getUrl());
        r.setMensagem(req.getMensagem());

        // Analisa tanto URL quanto texto (se informados). Resultado inseguro se qualquer um dos dois for suspeito.
        boolean textoSuspeito = contemPalavrasDeGolpe(req.getMensagem());
        Boolean urlSuspeita = null;
        String urlDetalhe = null;

        if (req.getUrl() != null && !req.getUrl().isEmpty()) {
            GoogleSafeBrowsingService.UrlCheckResult urlResult = googleSafeBrowsingService.checkUrl(req.getUrl());
            urlSuspeita = urlResult.malicious;
            urlDetalhe = urlResult.detail;
            r.setUrlSuspeita(urlSuspeita);
            r.setUrlDetalhe(urlDetalhe);
        }

        // Determina segurança: se a URL for maliciosa ou o texto for suspeito, considera inseguro
        boolean inseguro = (Boolean.TRUE.equals(urlSuspeita)) || textoSuspeito;

        if (inseguro) {
            r.setSeguro(false);
            StringBuilder sb = new StringBuilder();
            if (textoSuspeito) {
                sb.append("Possível golpe detectado! Mensagem contém dados pessoais ou solicitações suspeitas.\n");
                sb.append("O único site oficial para consulta de valores a receber é: https://valoresareceber.bcb.gov.br\n");
                sb.append("O Banco Central não envia links nem solicita dados pessoais por e-mail, SMS, WhatsApp ou Telegram.\n");
                sb.append("Somente a instituição que aparece na consulta pode contatar o cliente, mas nunca irá pedir dados pessoais ou senha.\n");
                sb.append("Dicas:\n");
                sb.append("- NÃO clicar em links suspeitos enviados por e-mail, SMS, WhatsApp ou Telegram;\n");
                sb.append("- NÃO fazer qualquer tipo de pagamento para ter acesso aos valores;\n");
                sb.append("- NÃO existe a opção de receber algum valor pelo uso de cartões de crédito. Não há lei ou norma do BC sobre recall de cartões de crédito.\n");
            }
            if (Boolean.TRUE.equals(urlSuspeita)) {
                sb.append("A URL fornecida aparenta ser maliciosa segundo verificação de segurança.");
                if (urlDetalhe != null && !urlDetalhe.isEmpty()) {
                    sb.append(" Detalhe: ").append(urlDetalhe);
                }
            }
            r.setDetalhe(sb.toString());
        } else {
            r.setSeguro(true);
            r.setDetalhe("Nenhum indício de golpe encontrado.");
        }

        // Salva o resultado de forma assíncrona para não bloquear a resposta HTTP
        new Thread(() -> {
            try {
                resultadoAnaliseRepository.save(r);
                logger.debug("Resultado de análise salvo com sucesso (assíncrono)");
            } catch (Exception dbEx) {
                // Não propagar erro para o fluxo principal — apenas logar
                logger.warn("Falha ao salvar ResultadoAnalise de forma assíncrona: {}", dbEx.getMessage());
            }
        }).start();

        return r;
    }

    private boolean contemPalavrasDeGolpe(String texto) {
        if (texto == null) return false;
        String t = texto.toLowerCase();

        // Remover acentos para melhor detecção
        String normalizada = Normalizer.normalize(t, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .replaceAll("\\s+", " ");

        // Padrões de solicitação de dados pessoais
        String[] dadosPessoais = {
            "nome", "nome completo", "sobrenome", "cpf", "rg", "chave pix", "telefone",
            "data de nascimento", "numero de conta", "agencia", "cartao de credito"
        };

        // Padrões de solicitação de pagamento
        String[] pagamentos = {
            "faca um pix", "faz um pix", "transferencia", "deposito", "pague", "pagamento",
            "taxa", "boleto", "cartao de credito", "comprovante", "valor"
        };

        // Padrões de menção a valores específicos
        String[] valoresAspecificos = {
            "r$", "reais", "disponivel", "saque", "receber", "liberar"
        };

        // Padrões de phishing e URLs suspeitas
        String[] phishing = {
            "clique aqui", "confirme", "valide", "autentique", "verifique", "atualize seus dados",
            "site oficial", "acesso restrito", "conta bloqueada", "acao imediata", "urgente"
        };

        // Padrões de recall de cartão (golpe comum)
        String[] cartaoRecall = {
            "recall do cartao", "recall cartao", "cartao de credito", "resgate de credito"
        };

        // Verificar dados pessoais + valores = padrão de phishing
        boolean temDadosPessoais = verificarPalavras(normalizada, dadosPessoais);
        boolean temPagamento = verificarPalavras(normalizada, pagamentos);
        boolean temValores = verificarPalavras(normalizada, valoresAspecificos);
        boolean temPhishing = verificarPalavras(normalizada, phishing);
        boolean temRecall = verificarPalavras(normalizada, cartaoRecall);

        // Lógica de detecção
        if (temDadosPessoais || temPagamento || temRecall) {
            return true;
        }

        // Combinação de valores + phishing é suspeita
        if (temValores && temPhishing) {
            return true;
        }

        return false;
    }

    private boolean verificarPalavras(String texto, String[] palavras) {
        for (String palavra : palavras) {
            if (texto.contains(palavra)) {
                return true;
            }
        }
        return false;
    }
}