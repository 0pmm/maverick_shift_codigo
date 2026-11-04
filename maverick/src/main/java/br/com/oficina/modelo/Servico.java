package br.com.oficina.modelo;

import br.com.oficina.modelo.enuns.Configuracao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    @Schema(example = "Lavagem completa")
    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    @Column(nullable = false)
    @Schema(example = "80.0")
    private Double preco;

    // tipo do veiculo
    @NotNull(message = "A configuração do veículo é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(example = "SEDAN")
    private Configuracao configuracao;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Servico other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
