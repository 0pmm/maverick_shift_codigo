package br.com.estetica.modelo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Chave composta de {@link ItemServico}. Os nomes dos campos são os mesmos
 * dos atributos @Id da entidade e os tipos são os das chaves de Servico e Produto.
 */
public class ItemServicoId implements Serializable {

    private String servico;
    private String produto;

    public ItemServicoId() {
    }

    public ItemServicoId(String servico, String produto) {
        this.servico = servico;
        this.produto = produto;
    }

    public String getServico() {
        return servico;
    }

    public String getProduto() {
        return produto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemServicoId)) return false;
        ItemServicoId outro = (ItemServicoId) o;
        return Objects.equals(servico, outro.servico) && Objects.equals(produto, outro.produto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servico, produto);
    }
}
