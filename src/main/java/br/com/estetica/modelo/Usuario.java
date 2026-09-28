package br.com.estetica.modelo;

import br.com.estetica.modelo.enums.Cargo;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false, length = 20)
    private Cargo cargo;

    public Usuario() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
