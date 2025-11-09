package com.alertavalores.alerta_valores.model;

public class ResultadoAnalise {
    private boolean seguro;
    private String detalhe;
    // Campos opcionais relacionados à verificação de URL (Google Safe Browsing)
    private String url;          // URL analisada (se informada)
    private Boolean urlSuspeita; // true se a URL foi marcada como maliciosa
    private String urlDetalhe;   // mensagem explicativa sobre a URL

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
}