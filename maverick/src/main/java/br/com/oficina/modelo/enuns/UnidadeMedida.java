package br.com.oficina.modelo.enuns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnidadeMedida {
    MILILITRO(1),
    LITRO(2),
    GALAO(3),
    GRAMA(4),
    QUILOGRAMA(5),
    UNIDADE(6);

    private final int codigo;
}