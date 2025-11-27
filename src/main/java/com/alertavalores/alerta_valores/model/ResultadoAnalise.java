package com.alertavalores.alerta_valores.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "resultado_analises")
public class ResultadoAnalise {
    @Id
    private String id;

    private boolean seguro;
    private String detalhe;
    // Campos opcionais relacionados à verificação de URL (Google Safe Browsing)
    private String url;          // URL analisada (se informada)
    private Boolean urlSuspeita; // true se a URL foi marcada como maliciosa
    private String urlDetalhe;   // mensagem explicativa sobre a URL
    private String mensagem;     // mensagem analisada (se informada)
    private LocalDateTime dataAnalise; // timestamp da análise

    public boolean getSeguro() { return seguro; }
    public void setSeguro(boolean seguro) { this.seguro = seguro; }

    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String detalhe) { this.detalhe = detalhe; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Boolean getUrlSuspeita() { return urlSuspeita; }
    public void setUrlSuspeita(Boolean urlSuspeita) { this.urlSuspeita = urlSuspeita; }

    public String getUrlDetalhe() { return urlDetalhe; }
    public void setUrlDetalhe(String urlDetalhe) { this.urlDetalhe = urlDetalhe; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getDataAnalise() { return dataAnalise; }
    public void setDataAnalise(LocalDateTime dataAnalise) { this.dataAnalise = dataAnalise; }

    public ResultadoAnalise() {
        this.dataAnalise = LocalDateTime.now();
    }
}