package model;

import javax.persistence.*;

@Entity
@Table(name = "urls")
public class UrlModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String endereco;

    private String descricao;

    // Construtores, getters e setters
    public UrlModel() {}

    public UrlModel(String endereco, String descricao) {
        this.endereco = endereco;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}