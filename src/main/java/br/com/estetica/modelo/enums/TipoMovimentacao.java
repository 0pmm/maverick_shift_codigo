package br.com.estetica.modelo.enums;

public enum TipoMovimentacao {
    ENTRADA(1, "Entrada"),
    SAIDA(2, "Saída");

    private final int codigo;
    private final String descricao;

    TipoMovimentacao(int codigo, String descricao) {
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
