package br.com.oficina.dao;

import br.com.oficina.modelo.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RelatorioDAO extends JpaRepository<Relatorio, UUID> {

    // ---- consultas usadas pra montar os relatorios ----

    @Query("select count(p) from Pagamento p where p.data between :inicio and :fim")
    Long quantidadePagamentos(LocalDateTime inicio, LocalDateTime fim);

    @Query("select sum(p.valor) from Pagamento p where p.data between :inicio and :fim")
    Double totalRecebido(LocalDateTime inicio, LocalDateTime fim);

    @Query("select a.status, count(a) from Agendamento a "
            + "where a.dataHora between :inicio and :fim group by a.status")
    List<Object[]> agendamentosPorStatus(LocalDateTime inicio, LocalDateTime fim);

    @Query("select a.servico.descricao, count(a) from Agendamento a "
            + "where a.dataHora between :inicio and :fim and a.status <> 'CANCELADO' "
            + "group by a.servico.id, a.servico.descricao order by count(a) desc")
    List<Object[]> servicosMaisAgendados(LocalDateTime inicio, LocalDateTime fim);

    @Query("select m.produto.nome, m.tipoMovimentacao, sum(m.quantidade) from MovimentacaoEstoque m "
            + "where m.data between :inicio and :fim "
            + "group by m.produto.id, m.produto.nome, m.tipoMovimentacao")
    List<Object[]> movimentacoesEstoque(LocalDateTime inicio, LocalDateTime fim);

    @Query("select p.cliente.nome, sum(p.valor) from Pagamento p "
            + "where p.data between :inicio and :fim "
            + "group by p.cliente.id, p.cliente.nome order by sum(p.valor) desc")
    List<Object[]> clientesQueMaisPagaram(LocalDateTime inicio, LocalDateTime fim);
}
