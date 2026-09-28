package br.com.estetica.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "pagamento")
public class Pagamento implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "valor", nullable = false)
    private double valor;

    // no diagrama aparece "descricao : int"; mapeado como texto por se tratar de uma descrição
    @Column(name = "descricao", length = 255)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    public Pagamento() {
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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pagamento)) return false;
        Pagamento pagamento = (Pagamento) o;
        return id != null && id.equals(pagamento.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
