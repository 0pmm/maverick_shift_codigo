package br.com.estetica.modelo;

import br.com.estetica.modelo.enums.TipoMovimentacao;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "quantidade", nullable = false)
    private double quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 20)
    private TipoMovimentacao tipoMovimentacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // opcional: uma ENTRADA de estoque (compra) não está ligada a nenhuma ordem de serviço
    @ManyToOne
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;

    public MovimentacaoEstoque() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovimentacaoEstoque)) return false;
        MovimentacaoEstoque movimentacao = (MovimentacaoEstoque) o;
        return id != null && id.equals(movimentacao.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
