package br.com.oficina.service;

import br.com.oficina.dao.ItemProdutoServicoDAO;
import br.com.oficina.dao.ServicoDAO;
import br.com.oficina.modelo.ItemProdutoServico;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Servico;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ServicoService {

    private final ServicoDAO servicoDAO;
    private final ItemProdutoServicoDAO itemDAO;
    private final ProdutoService produtoService;

    public ServicoService(ServicoDAO servicoDAO, ItemProdutoServicoDAO itemDAO, ProdutoService produtoService) {
        this.servicoDAO = servicoDAO;
        this.itemDAO = itemDAO;
        this.produtoService = produtoService;
    }

    @Transactional(readOnly = true)
    public Page<Servico> listar(Pageable pageable) {
        return servicoDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Servico> buscarPorDescricao(String descricao, Pageable pageable) {
        return servicoDAO.findByDescricaoContainingIgnoreCase(descricao, pageable);
    }

    @Transactional(readOnly = true)
    public Servico buscarPorId(UUID id) {
        return servicoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: id=" + id));
    }

    public Servico criar(Servico servico) {
        servico.setId(null);
        return servicoDAO.save(servico);
    }

    public Servico atualizar(UUID id, Servico dados) {
        Servico existente = buscarPorId(id);
        existente.setDescricao(dados.getDescricao());
        existente.setPreco(dados.getPreco());
        existente.setConfiguracao(dados.getConfiguracao());
        return servicoDAO.save(existente);
    }

    // Os produtos vinculados só existem dentro do serviço, então saem junto com ele
    public void deletar(UUID id) {
        Servico servico = buscarPorId(id);
        itemDAO.deleteByServicoId(id);
        servicoDAO.delete(servico);
    }

    // ---- Produtos necessários para executar o serviço (ItemProdutoServico) ----

    @Transactional(readOnly = true)
    public List<ItemProdutoServico> listarProdutos(UUID servicoId) {
        buscarPorId(servicoId);
        return itemDAO.findByServicoId(servicoId);
    }

    public ItemProdutoServico adicionarProduto(UUID servicoId, UUID produtoId, Integer quantidadeNecessaria) {
        Servico servico = buscarPorId(servicoId);
        if (itemDAO.existsByServicoIdAndProdutoId(servicoId, produtoId)) {
            throw new IllegalArgumentException("Este produto já está vinculado ao serviço");
        }
        Produto produto = produtoService.buscarPorId(produtoId);

        ItemProdutoServico item = new ItemProdutoServico();
        item.setServico(servico);
        item.setProduto(produto);
        item.setQuantidadeNecessaria(quantidadeNecessaria);
        return itemDAO.save(item);
    }

    public ItemProdutoServico atualizarProduto(UUID servicoId, UUID itemId, Integer quantidadeNecessaria) {
        ItemProdutoServico item = buscarItem(servicoId, itemId);
        item.setQuantidadeNecessaria(quantidadeNecessaria);
        return itemDAO.save(item);
    }

    public void removerProduto(UUID servicoId, UUID itemId) {
        itemDAO.delete(buscarItem(servicoId, itemId));
    }

    private ItemProdutoServico buscarItem(UUID servicoId, UUID itemId) {
        return itemDAO.findByIdAndServicoId(itemId, servicoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto vinculado não encontrado neste serviço: id=" + itemId));
    }
}
