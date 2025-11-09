package com.alertavalores.alerta_valores.model;

public class AnaliseUrl {
    private String mensagem;
    private String url;
    private String canal;

    public AnaliseUrl() {}

    public AnaliseUrl(String mensagem, String url, String canal) {
        this.mensagem = mensagem;
        this.url = url;
        this.canal = canal;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    
}