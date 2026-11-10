package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.ProdutoDAO;
import br.com.oficina.dao.RelatorioDAO;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Relatorio;
import br.com.oficina.modelo.enuns.TipoRelatorio;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class RelatorioService {

    private final RelatorioDAO relatorioDAO;
    private final UsuarioDAO usuarioDAO;
    private final ClienteDAO clienteDAO;
    private final ProdutoDAO produtoDAO;

    public RelatorioService(RelatorioDAO relatorioDAO, UsuarioDAO usuarioDAO, ClienteDAO clienteDAO,
            ProdutoDAO produtoDAO) {
        this.relatorioDAO = relatorioDAO;
        this.usuarioDAO = usuarioDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
    }

    @Transactional(readOnly = true)
    public Page<Relatorio> listar(Pageable pageable) {
        return relatorioDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Relatorio buscarPorId(UUID id) {
        return relatorioDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Relatório não encontrado: id=" + id));
    }

    public Map<String, Object> gerar(TipoRelatorio tipo, LocalDate inicio, LocalDate fim, String login) {
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }

        // salva no historico quem gerou o relatorio
        Relatorio relatorio = new Relatorio();
        relatorio.setTipoRelatorio(tipo);
        relatorio.setDataGeracao(LocalDateTime.now());
        if (login != null) {
            relatorio.setUsuario(usuarioDAO.findByLogin(login)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + login)));
        }
        relatorioDAO.save(relatorio);

        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.atTime(23, 59, 59);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("id", relatorio.getId());
        resultado.put("tipoRelatorio", tipo);
        resultado.put("dataGeracao", relatorio.getDataGeracao());
        resultado.put("geradoPor", relatorio.getUsuario() != null ? relatorio.getUsuario().getNome() : null);
        resultado.put("inicio", inicio);
        resultado.put("fim", fim);

        // o GERAL tem todas as partes
        if (tipo == TipoRelatorio.FINANCEIRO || tipo == TipoRelatorio.GERAL) {
            resultado.put("financeiro", financeiro(de, ate));
        }
        if (tipo == TipoRelatorio.SERVICOS || tipo == TipoRelatorio.GERAL) {
            resultado.put("servicos", servicos(de, ate));
        }
        if (tipo == TipoRelatorio.PRODUTOS || tipo == TipoRelatorio.GERAL) {
            resultado.put("produtos", produtos(de, ate));
        }
        if (tipo == TipoRelatorio.CLIENTES || tipo == TipoRelatorio.GERAL) {
            resultado.put("clientes", clientes(de, ate));
        }
        return resultado;
    }

    private Map<String, Object> financeiro(LocalDateTime de, LocalDateTime ate) {
        long quantidade = relatorioDAO.quantidadePagamentos(de, ate);
        Double total = relatorioDAO.totalRecebido(de, ate);
        if (total == null) {
            total = 0.0;
        }
        double ticketMedio = quantidade == 0 ? 0 : total / quantidade;

        Map<String, Object> financeiro = new LinkedHashMap<>();
        financeiro.put("quantidadePagamentos", quantidade);
        financeiro.put("totalRecebido", total);
        financeiro.put("ticketMedio", Math.round(ticketMedio * 100) / 100.0);
        return financeiro;
    }

    private Map<String, Object> servicos(LocalDateTime de, LocalDateTime ate) {
        Map<String, Object> servicos = new LinkedHashMap<>();
        servicos.put("agendamentosPorStatus", paraMapa(relatorioDAO.agendamentosPorStatus(de, ate)));
        servicos.put("servicosMaisAgendados", paraMapa(relatorioDAO.servicosMaisAgendados(de, ate)));
        return servicos;
    }

    private Map<String, Object> produtos(LocalDateTime de, LocalDateTime ate) {
        // produto -> {ENTRADA: x, SAIDA: y}
        Map<String, Map<String, Object>> movimentacoes = new LinkedHashMap<>();
        for (Object[] linha : relatorioDAO.movimentacoesEstoque(de, ate)) {
            String produto = (String) linha[0];
            movimentacoes.computeIfAbsent(produto, p -> new LinkedHashMap<>()).put(linha[1].toString(), linha[2]);
        }

        Map<String, Object> produtos = new LinkedHashMap<>();
        produtos.put("estoqueAtual", produtoDAO.findAll(Sort.by("nome")));
        produtos.put("movimentacoesNoPeriodo", movimentacoes);
        return produtos;
    }

    private Map<String, Object> clientes(LocalDateTime de, LocalDateTime ate) {
        Map<String, Object> clientes = new LinkedHashMap<>();
        clientes.put("totalClientes", clienteDAO.count());
        clientes.put("clientesQueMaisPagaram", paraMapa(relatorioDAO.clientesQueMaisPagaram(de, ate)));
        return clientes;
    }

    // transforma as linhas (nome, valor) das consultas em um mapa
    private Map<String, Object> paraMapa(List<Object[]> linhas) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (Object[] linha : linhas) {
            mapa.put(String.valueOf(linha[0]), linha[1]);
        }
        return mapa;
    }
}
