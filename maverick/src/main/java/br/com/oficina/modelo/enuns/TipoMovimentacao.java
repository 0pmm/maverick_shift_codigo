package br.com.oficina.modelo.enuns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMovimentacao {
    ENTRADA(1),
    SAIDA(2);

    private final int codigo;
}
