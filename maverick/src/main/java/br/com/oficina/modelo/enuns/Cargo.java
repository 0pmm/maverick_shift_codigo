package br.com.oficina.modelo.enuns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Cargo {
    FUNCIONARIO(1),
    GESTOR(2);

    private final int codigo;
}
