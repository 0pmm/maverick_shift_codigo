package br.com.oficina.modelo;

import br.com.oficina.modelo.enuns.TipoMovimentacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    @Schema(example = "2026-11-10T09:00:00")
    private LocalDateTime data;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    @Column(nullable = false)
    @Schema(example = "2.5")
    private Double quantidade;

    @NotNull(message = "O tipo de movimentação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(example = "ENTRADA")
    private TipoMovimentacao tipoMovimentacao;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    // opcional, uma ENTRADA (compra) nao tem ordem de servico
    @ManyToOne
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MovimentacaoEstoque other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
