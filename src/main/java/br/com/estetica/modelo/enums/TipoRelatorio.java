package br.com.estetica.modelo.enums;

public enum TipoRelatorio {
    FINANCEIRO(1, "Financeiro"),
    SERVICOS(2, "Serviços"),
    PRODUTOS(3, "Produtos"),
    CLIENTES(4, "Clientes"),
    GERAL(5, "Geral");

    private final int codigo;
    private final String descricao;

    TipoRelatorio(int codigo, String descricao) {
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
