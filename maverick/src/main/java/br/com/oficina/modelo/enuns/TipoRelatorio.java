package br.com.oficina.modelo.enuns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoRelatorio {
    FINANCEIRO(1),
    SERVICOS(2),
    PRODUTOS(3),
    CLIENTES(4),
    GERAL(5);

    private final int codigo;
}
