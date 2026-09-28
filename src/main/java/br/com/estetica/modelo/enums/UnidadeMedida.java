package br.com.estetica.modelo.enums;

public enum UnidadeMedida {
    MILILITRO(1, "Mililitro (ml)"),
    LITRO(2, "Litro (l)"),
    GALAO(3, "Galão"),
    GRAMA(4, "Grama (g)"),
    QUILOGRAMA(5, "Quilograma (kg)"),
    UNIDADE(6, "Unidade (un)");

    private final int codigo;
    private final String descricao;

    UnidadeMedida(int codigo, String descricao) {
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
