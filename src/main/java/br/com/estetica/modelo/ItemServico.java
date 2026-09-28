package br.com.estetica.modelo;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Produto consumido por um serviço (ex.: 200 ml de shampoo por lavagem).
 * Não tem id próprio no diagrama: é identificado pelo par servico + produto.
 */
@Entity
@Table(name = "item_servico")
@IdClass(ItemServicoId.class)
public class ItemServico implements Serializable {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade_necessaria", nullable = false)
    private int quantidadeNecessaria;

    // ---- getters e setters ----

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidadeNecessaria() {
        return quantidadeNecessaria;
    }

    public void setQuantidadeNecessaria(int quantidadeNecessaria) {
        this.quantidadeNecessaria = quantidadeNecessaria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemServico)) return false;
        ItemServico item = (ItemServico) o;
        return servico != null && servico.equals(item.servico)
                && produto != null && produto.equals(item.produto);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
