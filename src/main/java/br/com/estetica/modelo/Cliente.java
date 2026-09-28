package br.com.estetica.modelo;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "email", length = 150)
    private String email;

    public Cliente() {
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        Cliente cliente = (Cliente) o;
        return id != null && id.equals(cliente.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
