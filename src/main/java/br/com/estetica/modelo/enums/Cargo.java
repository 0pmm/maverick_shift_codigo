package br.com.estetica.modelo.enums;

public enum Cargo {
    FUNCIONARIO(1, "Funcionário"),
    GESTOR(2, "Gestor");

    private final int codigo;
    private final String descricao;

    Cargo(int codigo, String descricao) {
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
