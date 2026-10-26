package br.com.oficina.modelo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
@Table(name = "ordens_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull(message = "A data de abertura é obrigatória")
    @Column(nullable = false)
    @Schema(example = "2026-11-10T14:30:00")
    private LocalDateTime dataAbertura;

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    @Column(nullable = false, length = 20)
    @Schema(example = "ABERTA")
    private String status;

    @NotNull(message = "O valor total é obrigatório")
    @PositiveOrZero(message = "O valor total não pode ser negativo")
    @Column(nullable = false)
    @Schema(example = "150.0")
    private Double valorTotal;

    @NotNull(message = "O funcionário é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Usuario funcionario;

    // 1 para 1 no diagrama
    @NotNull(message = "O agendamento é obrigatório")
    @OneToOne(optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrdemServico other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
