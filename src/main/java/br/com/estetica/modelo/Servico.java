package br.com.estetica.modelo;

import br.com.estetica.modelo.enums.Configuracao;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "servico")
public class Servico implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "preco", nullable = false)
    private double preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "configuracao", nullable = false, length = 20)
    private Configuracao configuracao;

    public Servico() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Configuracao getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(Configuracao configuracao) {
        this.configuracao = configuracao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico)) return false;
        Servico servico = (Servico) o;
        return id != null && id.equals(servico.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
