package br.com.oficina.service;

import br.com.oficina.dao.ProdutoDAO;
import br.com.oficina.modelo.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoDAO produtoDAO;

    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    @Transactional(readOnly = true)
    public Page<Produto> listar(Pageable pageable) {
        return produtoDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Produto> buscarPorNome(String nome, Pageable pageable) {
        return produtoDAO.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(UUID id) {
        return produtoDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: id=" + id));
    }

    public Produto criar(Produto produto) {
        produto.setId(null);
        return produtoDAO.save(produto);
    }

    public Produto atualizar(UUID id, Produto dados) {
        Produto existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setEstoque(dados.getEstoque());
        existente.setCategoria(dados.getCategoria());
        existente.setUnidadeMedida(dados.getUnidadeMedida());
        return produtoDAO.save(existente);
    }

    public void deletar(UUID id) {
        produtoDAO.delete(buscarPorId(id));
    }
}