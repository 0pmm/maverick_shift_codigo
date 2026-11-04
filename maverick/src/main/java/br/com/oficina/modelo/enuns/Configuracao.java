package br.com.oficina.modelo.enuns;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Configuracao {
    HATCH(1),
    SUV(2),
    SEDAN(3),
    PICKUP(4),
    MINIVAN(5),
    PERUA(6),
    COUPE(7),
    CONVERSIVEL(8);

    private final int codigo;
}
