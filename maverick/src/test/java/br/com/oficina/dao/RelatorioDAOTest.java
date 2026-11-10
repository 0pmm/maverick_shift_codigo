package br.com.oficina.dao;

import br.com.oficina.modelo.Agendamento;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.MovimentacaoEstoque;
import br.com.oficina.modelo.Pagamento;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Servico;
import br.com.oficina.modelo.enuns.Configuracao;
import br.com.oficina.modelo.enuns.TipoMovimentacao;
import br.com.oficina.modelo.enuns.UnidadeMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// roda as consultas do relatorio (UC08) e do pagamento (UC06) num H2 de verdade
@DataJpaTest
@ActiveProfiles("test")
class RelatorioDAOTest {

    private static final LocalDateTime DE = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final LocalDateTime ATE = LocalDateTime.of(2026, 11, 30, 23, 59, 59);
    private static final LocalDateTime DENTRO = LocalDateTime.of(2026, 11, 10, 10, 0);
    private static final LocalDateTime FORA = LocalDateTime.of(2026, 12, 5, 10, 0);

    @Autowired
    private RelatorioDAO relatorioDAO;
    @Autowired
    private PagamentoDAO pagamentoDAO;
    @Autowired
    private ClienteDAO clienteDAO;
    @Autowired
    private ServicoDAO servicoDAO;
    @Autowired
    private AgendamentoDAO agendamentoDAO;
    @Autowired
    private ProdutoDAO produtoDAO;
    @Autowired
    private MovimentacaoEstoqueDAO movimentacaoEstoqueDAO;

    private Cliente maria;
    private Cliente joao;

    @BeforeEach
    void popularBanco() {
        maria = cliente("Maria");
        joao = cliente("Joao");

        pagamento(maria, 100.0, DENTRO);
        pagamento(maria, 50.0, DENTRO);
        pagamento(joao, 30.0, DENTRO);
        pagamento(joao, 999.0, FORA);

        Servico lavagem = servico("Lavagem");
        Servico polimento = servico("Polimento");
        agendamento(maria, lavagem, "AGENDADO", DENTRO);
        agendamento(joao, lavagem, "CONCLUIDO", DENTRO);
        agendamento(joao, polimento, "CANCELADO", DENTRO);
        agendamento(maria, polimento, "AGENDADO", FORA);

        Produto cera = produto("Cera");
        movimentacao(cera, TipoMovimentacao.ENTRADA, 10.0, DENTRO);
        movimentacao(cera, TipoMovimentacao.SAIDA, 2.0, DENTRO);
        movimentacao(cera, TipoMovimentacao.SAIDA, 1.0, DENTRO);
        movimentacao(cera, TipoMovimentacao.SAIDA, 50.0, FORA);
    }

    @Test
    void financeiroContaSoOsPagamentosDoPeriodo() {
        assertThat(relatorioDAO.quantidadePagamentos(DE, ATE)).isEqualTo(3L);
        assertThat(relatorioDAO.totalRecebido(DE, ATE)).isEqualTo(180.0);
    }

    @Test
    void totalRecebidoSemPagamentosVemNull() {
        LocalDateTime ano2020 = LocalDateTime.of(2020, 1, 1, 0, 0);
        assertThat(relatorioDAO.totalRecebido(ano2020, ano2020.plusDays(1))).isNull();
    }

    @Test
    void agendamentosPorStatusAgrupa() {
        assertThat(relatorioDAO.agendamentosPorStatus(DE, ATE))
                .extracting(linha -> linha[0] + "=" + linha[1])
                .containsExactlyInAnyOrder("AGENDADO=1", "CONCLUIDO=1", "CANCELADO=1");
    }

    @Test
    void servicosMaisAgendadosIgnoraCancelados() {
        List<Object[]> linhas = relatorioDAO.servicosMaisAgendados(DE, ATE);

        assertThat(linhas).extracting(linha -> linha[0] + "=" + linha[1])
                .containsExactly("Lavagem=2");
    }

    @Test
    void movimentacoesSomamPorProdutoETipo() {
        assertThat(relatorioDAO.movimentacoesEstoque(DE, ATE))
                .extracting(linha -> linha[0] + ":" + linha[1] + "=" + linha[2])
                .containsExactlyInAnyOrder("Cera:ENTRADA=10.0", "Cera:SAIDA=3.0");
    }

    @Test
    void clientesQueMaisPagaramVemEmOrdem() {
        assertThat(relatorioDAO.clientesQueMaisPagaram(DE, ATE))
                .extracting(linha -> linha[0] + "=" + linha[1])
                .containsExactly("Maria=150.0", "Joao=30.0");
    }

    @Test
    void pagamentoDAOFiltraPorClienteEPeriodo() {
        assertThat(pagamentoDAO.findByClienteId(maria.getId(), PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(2);
        assertThat(pagamentoDAO.findByDataBetween(DE, ATE, PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(3);
    }

    private Cliente cliente(String nome) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone("11999999999");
        return clienteDAO.save(cliente);
    }

    private void pagamento(Cliente cliente, double valor, LocalDateTime data) {
        Pagamento pagamento = new Pagamento();
        pagamento.setCliente(cliente);
        pagamento.setValor(valor);
        pagamento.setData(data);
        pagamentoDAO.save(pagamento);
    }

    private Servico servico(String descricao) {
        Servico servico = new Servico();
        servico.setDescricao(descricao);
        servico.setPreco(100.0);
        servico.setConfiguracao(Configuracao.SEDAN);
        return servicoDAO.save(servico);
    }

    private void agendamento(Cliente cliente, Servico servico, String status, LocalDateTime dataHora) {
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setStatus(status);
        agendamento.setDataHora(dataHora);
        agendamentoDAO.save(agendamento);
    }

    private Produto produto(String nome) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setEstoque(100.0);
        produto.setCategoria("Limpeza");
        produto.setUnidadeMedida(UnidadeMedida.UNIDADE);
        return produtoDAO.save(produto);
    }

    private void movimentacao(Produto produto, TipoMovimentacao tipo, double quantidade, LocalDateTime data) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(produto);
        movimentacao.setTipoMovimentacao(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setData(data);
        movimentacaoEstoqueDAO.save(movimentacao);
    }
}
