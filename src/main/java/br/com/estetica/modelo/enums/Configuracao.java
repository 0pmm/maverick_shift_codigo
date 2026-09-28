package br.com.estetica.modelo.enums;

public enum Configuracao {
    HATCH(1, "Hatch"),
    SUV(2, "SUV"),
    SEDAN(3, "Sedan"),
    PICKUP(4, "Picape"),
    MINIVAN(5, "Minivan"),
    PERUA(6, "Perua"),
    COUPE(7, "Cupê"),
    CONVERSIVEL(8, "Conversível");

    private final int codigo;
    private final String descricao;

    Configuracao(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}
