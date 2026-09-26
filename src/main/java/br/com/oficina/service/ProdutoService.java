package br.com.oficina.service;

import br.com.oficina.dao.ProdutoDAO;
import br.com.oficina.modelo.Produto;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProdutoService {

    @Inject
    private ProdutoDAO produtoDAO;

    public void salvar(Produto produto) {
        validar(produto);
        produtoDAO.salvar(produto);
    }

    public void excluir(String id) {
        produtoDAO.excluir(id);
    }

    public Produto buscarPorId(String id) {
        return produtoDAO.buscarPorId(id);
    }

    public List<Produto> listarTodos() {
        return produtoDAO.listarTodos();
    }

    private void validar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (produto.getUnidadeMedida() == null) {
            throw new IllegalArgumentException("A unidade de medida é obrigatória.");
        }
        if (produto.getEstoque() < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }
    }
}
