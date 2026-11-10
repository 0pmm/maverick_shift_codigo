package br.com.oficina.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull(message = "A data e hora são obrigatórias")
    @Column(nullable = false)
    @Schema(example = "2026-11-10T14:30:00")
    private LocalDateTime dataHora;

    // preenchido pelo service (AGENDADO, CONCLUIDO ou CANCELADO)
    @Column(nullable = false, length = 20)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "AGENDADO")
    private String status;

    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "O serviço é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Agendamento other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
