package br.com.oficina.service;

import br.com.oficina.dao.AgendamentoDAO;
import br.com.oficina.dao.ItemProdutoServicoDAO;
import br.com.oficina.dao.ItemServicoAgendamentoDAO;
import br.com.oficina.dao.MovimentacaoEstoqueDAO;
import br.com.oficina.dao.OrdemServicoDAO;
import br.com.oficina.dao.ProdutoDAO;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Agendamento;
import br.com.oficina.modelo.ItemProdutoServico;
import br.com.oficina.modelo.ItemServicoAgendamento;
import br.com.oficina.modelo.MovimentacaoEstoque;
import br.com.oficina.modelo.OrdemServico;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.TipoMovimentacao;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ciclo de vida da ordem de serviço: ABERTA -> EM_ANDAMENTO -> CONCLUIDA.
 * Enquanto não estiver concluída, pode ser CANCELADA.
 */
@Service
@Transactional
public class OrdemServicoService {

    public static final String ABERTA = "ABERTA";
    public static final String EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String CONCLUIDA = "CONCLUIDA";
    public static final String CANCELADA = "CANCELADA";

    // mesmos status usados pelo AgendamentoService (UC04)
    static final String AGENDAMENTO_AGENDADO = "AGENDADO";
    static final String AGENDAMENTO_CONCLUIDO = "CONCLUIDO";
    static final String AGENDAMENTO_CANCELADO = "CANCELADO";

    private final OrdemServicoDAO ordemServicoDAO;
    private final AgendamentoDAO agendamentoDAO;
    private final ItemServicoAgendamentoDAO itemServicoAgendamentoDAO;
    private final ItemProdutoServicoDAO itemProdutoServicoDAO;
    private final MovimentacaoEstoqueDAO movimentacaoEstoqueDAO;
    private final ProdutoDAO produtoDAO;
    private final UsuarioDAO usuarioDAO;

    public OrdemServicoService(OrdemServicoDAO ordemServicoDAO, AgendamentoDAO agendamentoDAO,
            ItemServicoAgendamentoDAO itemServicoAgendamentoDAO, ItemProdutoServicoDAO itemProdutoServicoDAO,
            MovimentacaoEstoqueDAO movimentacaoEstoqueDAO, ProdutoDAO produtoDAO, UsuarioDAO usuarioDAO) {
        this.ordemServicoDAO = ordemServicoDAO;
        this.agendamentoDAO = agendamentoDAO;
        this.itemServicoAgendamentoDAO = itemServicoAgendamentoDAO;
        this.itemProdutoServicoDAO = itemProdutoServicoDAO;
        this.movimentacaoEstoqueDAO = movimentacaoEstoqueDAO;
        this.produtoDAO = produtoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @Transactional(readOnly = true)
    public Page<OrdemServico> listar(Pageable pageable) {
        return ordemServicoDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServico> listarPorStatus(String status, Pageable pageable) {
        return ordemServicoDAO.findByStatus(status.toUpperCase(), pageable);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarPorId(UUID id) {
        return ordemServicoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<ItemServicoAgendamento> listarServicos(UUID id) {
        return itemServicoAgendamentoDAO.findByAgendamentoId(buscarPorId(id).getAgendamento().getId());
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoque> listarMovimentacoes(UUID id) {
        buscarPorId(id);
        return movimentacaoEstoqueDAO.findByOrdemServicoId(id);
    }

    /**
     * Abre a OS a partir de um agendamento. O valor total é a soma dos preços dos serviços agendados.
     * Sem funcionarioId, o responsável é o usuário logado.
     */
    public OrdemServico abrir(UUID agendamentoId, UUID funcionarioId, String loginUsuarioLogado) {
        Agendamento agendamento = agendamentoDAO.findById(agendamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado: id=" + agendamentoId));
        if (!AGENDAMENTO_AGENDADO.equals(agendamento.getStatus())) {
            throw new IllegalArgumentException(
                    "Só é possível abrir OS para agendamento com status AGENDADO (atual: " + agendamento.getStatus() + ")");
        }
        if (ordemServicoDAO.existsByAgendamentoId(agendamentoId)) {
            throw new IllegalArgumentException("Este agendamento já possui uma ordem de serviço");
        }
        List<ItemServicoAgendamento> servicos = itemServicoAgendamentoDAO.findByAgendamentoId(agendamentoId);
        if (servicos.isEmpty()) {
            throw new IllegalArgumentException("O agendamento não possui serviços");
        }

        OrdemServico ordem = new OrdemServico();
        ordem.setAgendamento(agendamento);
        ordem.setFuncionario(funcionarioId != null
                ? buscarFuncionario(funcionarioId)
                : buscarFuncionarioPorLogin(loginUsuarioLogado));
        ordem.setDataAbertura(LocalDateTime.now().withNano(0));
        ordem.setStatus(ABERTA);
        ordem.setValorTotal(servicos.stream().mapToDouble(item -> item.getServico().getPreco()).sum());
        return ordemServicoDAO.save(ordem);
    }

    public OrdemServico trocarFuncionario(UUID id, UUID funcionarioId) {
        OrdemServico ordem = buscarPorId(id);
        exigirStatus(ordem, ABERTA, EM_ANDAMENTO);
        ordem.setFuncionario(buscarFuncionario(funcionarioId));
        return ordemServicoDAO.save(ordem);
    }

    public OrdemServico iniciar(UUID id) {
        OrdemServico ordem = buscarPorId(id);
        exigirStatus(ordem, ABERTA);
        ordem.setStatus(EM_ANDAMENTO);
        return ordemServicoDAO.save(ordem);
    }

    /**
     * Conclui a OS: dá baixa no estoque dos produtos usados pelos serviços (MovimentacaoEstoque SAIDA)
     * e marca o agendamento como CONCLUIDO. Se faltar estoque de algum produto, nada é alterado.
     */
    public OrdemServico concluir(UUID id) {
        OrdemServico ordem = buscarPorId(id);
        exigirStatus(ordem, EM_ANDAMENTO);

        Map<UUID, Double> consumo = new LinkedHashMap<>();
        Map<UUID, Produto> produtos = new LinkedHashMap<>();
        for (ItemServicoAgendamento itemServico : itemServicoAgendamentoDAO.findByAgendamentoId(ordem.getAgendamento().getId())) {
            for (ItemProdutoServico itemProduto : itemProdutoServicoDAO.findByServicoId(itemServico.getServico().getId())) {
                Produto produto = itemProduto.getProduto();
                produtos.put(produto.getId(), produto);
                consumo.merge(produto.getId(), itemProduto.getQuantidadeNecessaria().doubleValue(), Double::sum);
            }
        }

        // confere tudo antes de mexer no estoque, para não baixar só parte dos produtos
        for (Map.Entry<UUID, Double> entrada : consumo.entrySet()) {
            Produto produto = produtos.get(entrada.getKey());
            if (produto.getEstoque() < entrada.getValue()) {
                throw new IllegalArgumentException("Estoque insuficiente de " + produto.getNome()
                        + ": necessário " + entrada.getValue() + ", disponível " + produto.getEstoque());
            }
        }

        LocalDateTime agora = LocalDateTime.now().withNano(0);
        for (Map.Entry<UUID, Double> entrada : consumo.entrySet()) {
            Produto produto = produtos.get(entrada.getKey());
            produto.setEstoque(produto.getEstoque() - entrada.getValue());
            produtoDAO.save(produto);

            MovimentacaoEstoque saida = new MovimentacaoEstoque();
            saida.setData(agora);
            saida.setQuantidade(entrada.getValue());
            saida.setTipoMovimentacao(TipoMovimentacao.SAIDA);
            saida.setProduto(produto);
            saida.setOrdemServico(ordem);
            movimentacaoEstoqueDAO.save(saida);
        }

        Agendamento agendamento = ordem.getAgendamento();
        agendamento.setStatus(AGENDAMENTO_CONCLUIDO);
        agendamentoDAO.save(agendamento);

        ordem.setStatus(CONCLUIDA);
        return ordemServicoDAO.save(ordem);
    }

    // o estoque só é baixado na conclusão, então cancelar não mexe no estoque
    public OrdemServico cancelar(UUID id) {
        OrdemServico ordem = buscarPorId(id);
        exigirStatus(ordem, ABERTA, EM_ANDAMENTO);

        Agendamento agendamento = ordem.getAgendamento();
        agendamento.setStatus(AGENDAMENTO_CANCELADO);
        agendamentoDAO.save(agendamento);

        ordem.setStatus(CANCELADA);
        return ordemServicoDAO.save(ordem);
    }

    private void exigirStatus(OrdemServico ordem, String... permitidos) {
        if (!Set.of(permitidos).contains(ordem.getStatus())) {
            throw new IllegalArgumentException(
                    "Ordem de serviço com status " + ordem.getStatus() + " não permite esta operação");
        }
    }

    private Usuario buscarFuncionario(UUID id) {
        return usuarioDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: id=" + id));
    }

    private Usuario buscarFuncionarioPorLogin(String login) {
        return usuarioDAO.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: login=" + login));
    }
}
