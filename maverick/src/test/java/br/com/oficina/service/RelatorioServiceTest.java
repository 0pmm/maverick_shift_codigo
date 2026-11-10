package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.ProdutoDAO;
import br.com.oficina.dao.RelatorioDAO;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Relatorio;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.TipoMovimentacao;
import br.com.oficina.modelo.enuns.TipoRelatorio;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 30);
    private static final LocalDateTime DE = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final LocalDateTime ATE = LocalDateTime.of(2026, 11, 30, 23, 59, 59);

    @Mock
    private RelatorioDAO relatorioDAO;
    @Mock
    private UsuarioDAO usuarioDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private ProdutoDAO produtoDAO;

    @InjectMocks
    private RelatorioService service;

    private Usuario gestor() {
        Usuario usuario = new Usuario();
        usuario.setNome("Gestor");
        usuario.setLogin("gestor");
        return usuario;
    }

    private static List<Object[]> linhas(Object[]... linhas) {
        return List.of(linhas);
    }

    @Test
    void financeiroCalculaOTicketMedioESalvaNoHistorico() {
        when(usuarioDAO.findByLogin("gestor")).thenReturn(Optional.of(gestor()));
        when(relatorioDAO.quantidadePagamentos(DE, ATE)).thenReturn(3L);
        when(relatorioDAO.totalRecebido(DE, ATE)).thenReturn(301.0);

        Map<String, Object> resultado = service.gerar(TipoRelatorio.FINANCEIRO, INICIO, FIM, "gestor");

        @SuppressWarnings("unchecked")
        Map<String, Object> financeiro = (Map<String, Object>) resultado.get("financeiro");
        assertThat(financeiro)
                .containsEntry("quantidadePagamentos", 3L)
                .containsEntry("totalRecebido", 301.0)
                .containsEntry("ticketMedio", 100.33);
        assertThat(resultado).containsEntry("geradoPor", "Gestor")
                .doesNotContainKeys("servicos", "produtos", "clientes");

        ArgumentCaptor<Relatorio> salvo = ArgumentCaptor.forClass(Relatorio.class);
        verify(relatorioDAO).save(salvo.capture());
        assertThat(salvo.getValue().getTipoRelatorio()).isEqualTo(TipoRelatorio.FINANCEIRO);
        assertThat(salvo.getValue().getUsuario().getLogin()).isEqualTo("gestor");
        assertThat(salvo.getValue().getDataGeracao()).isNotNull();
    }

    @Test
    void financeiroSemPagamentosDevolveZero() {
        when(relatorioDAO.quantidadePagamentos(DE, ATE)).thenReturn(0L);
        // sum() sem linhas vem null do banco
        when(relatorioDAO.totalRecebido(DE, ATE)).thenReturn(null);

        Map<String, Object> resultado = service.gerar(TipoRelatorio.FINANCEIRO, INICIO, FIM, null);

        @SuppressWarnings("unchecked")
        Map<String, Object> financeiro = (Map<String, Object>) resultado.get("financeiro");
        assertThat(financeiro)
                .containsEntry("totalRecebido", 0.0)
                .containsEntry("ticketMedio", 0.0);
        assertThat(resultado.get("geradoPor")).isNull();
    }

    @Test
    void geralTrazTodasAsPartes() {
        Produto cera = new Produto();
        cera.setNome("Cera");
        when(relatorioDAO.quantidadePagamentos(DE, ATE)).thenReturn(1L);
        when(relatorioDAO.totalRecebido(DE, ATE)).thenReturn(50.0);
        when(relatorioDAO.agendamentosPorStatus(DE, ATE))
                .thenReturn(linhas(new Object[] { "AGENDADO", 2L }, new Object[] { "CANCELADO", 1L }));
        when(relatorioDAO.servicosMaisAgendados(DE, ATE)).thenReturn(linhas(new Object[] { "Lavagem", 2L }));
        when(relatorioDAO.movimentacoesEstoque(DE, ATE)).thenReturn(linhas(
                new Object[] { "Cera", TipoMovimentacao.ENTRADA, 10.0 },
                new Object[] { "Cera", TipoMovimentacao.SAIDA, 3.0 }));
        when(produtoDAO.findAll(Sort.by("nome"))).thenReturn(List.of(cera));
        when(clienteDAO.count()).thenReturn(7L);
        when(relatorioDAO.clientesQueMaisPagaram(DE, ATE)).thenReturn(linhas(new Object[] { "Maria", 50.0 }));

        Map<String, Object> resultado = service.gerar(TipoRelatorio.GERAL, INICIO, FIM, null);

        assertThat(resultado).containsKeys("financeiro", "servicos", "produtos", "clientes");
        assertThat(resultado.get("servicos").toString())
                .contains("AGENDADO=2", "CANCELADO=1", "Lavagem=2");
        // as duas linhas do mesmo produto viram um mapa so
        assertThat(resultado.get("produtos").toString())
                .contains("movimentacoesNoPeriodo={Cera={ENTRADA=10.0, SAIDA=3.0}}");
        assertThat(resultado.get("clientes").toString())
                .contains("totalClientes=7", "Maria=50.0");
    }

    @Test
    void servicosNaoConsultaFinanceiroNemProdutos() {
        when(relatorioDAO.agendamentosPorStatus(DE, ATE)).thenReturn(linhas());
        when(relatorioDAO.servicosMaisAgendados(DE, ATE)).thenReturn(linhas());

        Map<String, Object> resultado = service.gerar(TipoRelatorio.SERVICOS, INICIO, FIM, null);

        assertThat(resultado).containsKey("servicos").doesNotContainKeys("financeiro", "produtos", "clientes");
        verify(relatorioDAO, never()).quantidadePagamentos(any(), any());
        verify(produtoDAO, never()).findAll(any(Sort.class));
    }

    @Test
    void fimAntesDoInicioDaErroENaoSalva() {
        assertThatThrownBy(() -> service.gerar(TipoRelatorio.GERAL, FIM, INICIO, "gestor"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(relatorioDAO, never()).save(any());
    }

    @Test
    void usuarioInexistenteDaErro() {
        when(usuarioDAO.findByLogin("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gerar(TipoRelatorio.FINANCEIRO, INICIO, FIM, "fantasma"))
                .isInstanceOf(EntityNotFoundException.class);
        verify(relatorioDAO, never()).save(any());
    }
}
