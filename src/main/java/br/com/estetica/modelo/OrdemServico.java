package br.com.estetica.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "ordem_servico")
public class OrdemServico implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Usuario funcionario;

    // 1 para 1: cada agendamento gera uma única ordem de serviço
    @OneToOne(optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    public OrdemServico() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Usuario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Usuario funcionario) {
        this.funcionario = funcionario;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdemServico)) return false;
        OrdemServico ordemServico = (OrdemServico) o;
        return id != null && id.equals(ordemServico.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
