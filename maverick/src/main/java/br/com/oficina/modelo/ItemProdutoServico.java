package br.com.oficina.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Produto que um serviço consome e a quantidade necessária para executá-lo.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "itens_produto_servico")
public class ItemProdutoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull(message = "A quantidade necessária é obrigatória")
    @Positive(message = "A quantidade necessária deve ser maior que zero")
    @Column(nullable = false)
    @Schema(example = "4")
    private Integer quantidadeNecessaria;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemProdutoServico other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
