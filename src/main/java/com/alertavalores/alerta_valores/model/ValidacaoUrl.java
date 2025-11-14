package com.alertavalores.alerta_valores.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection = "validacoes_url")
public class ValidacaoUrl {
    @Id
    private String id;

    private String url;
    private Boolean segura;
    private String detalhes;
    private LocalDateTime dataValidacao;
    private String erro;

    public ValidacaoUrl() {}

    public ValidacaoUrl(String url, Boolean segura, String detalhes, String erro) {
        this.url = url;
        this.segura = segura;
        this.detalhes = detalhes;
        this.erro = erro;
        this.dataValidacao = LocalDateTime.now();
    }

    // Getters e setters
    public String getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Boolean getSegura() { return segura; }
    public void setSegura(Boolean segura) { this.segura = segura; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }
    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }
}
