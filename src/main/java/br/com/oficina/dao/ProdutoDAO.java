package br.com.oficina.dao;

import br.com.oficina.modelo.Produto;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProdutoDAO extends GenericDAO<Produto, String> {

    public ProdutoDAO() {
        super(Produto.class);
    }

    // métodos de consulta específicos de Produto entram aqui, ex:
    // public List<Produto> buscarPorCategoria(String categoria) { ... }
}
