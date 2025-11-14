package com.alertavalores.alerta_valores.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urls")
public class UrlModel {
    @Id
    private String id;
    private String endereco;
    private String descricao;

    // Construtores, getters e setters
    public UrlModel() {}

    public UrlModel(String endereco, String descricao) {
        this.endereco = endereco;
        this.descricao = descricao;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}