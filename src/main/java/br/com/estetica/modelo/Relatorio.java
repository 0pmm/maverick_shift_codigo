package br.com.estetica.modelo;

import br.com.estetica.modelo.enums.TipoRelatorio;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name = "relatorio")
public class Relatorio implements Serializable {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relatorio", nullable = false, length = 20)
    private TipoRelatorio tipoRelatorio;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Relatorio() {
        this.id = UUID.randomUUID().toString();
    }

    // ---- getters e setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public TipoRelatorio getTipoRelatorio() {
        return tipoRelatorio;
    }

    public void setTipoRelatorio(TipoRelatorio tipoRelatorio) {
        this.tipoRelatorio = tipoRelatorio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relatorio)) return false;
        Relatorio relatorio = (Relatorio) o;
        return id != null && id.equals(relatorio.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
