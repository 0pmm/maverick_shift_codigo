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
import br.com.oficina.modelo.Servico;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoDAO ordemServicoDAO;
    @Mock
    private AgendamentoDAO agendamentoDAO;
    @Mock
    private ItemServicoAgendamentoDAO itemServicoAgendamentoDAO;
    @Mock
    private ItemProdutoServicoDAO itemProdutoServicoDAO;
    @Mock
    private MovimentacaoEstoqueDAO movimentacaoEstoqueDAO;
    @Mock
    private ProdutoDAO produtoDAO;
    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private OrdemServicoService service;

    private Agendamento agendamento(String status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        agendamento.setStatus(status);
        return agendamento;
    }

    private Servico servico(double preco) {
        Servico servico = new Servico();
        servico.setId(UUID.randomUUID());
        servico.setPreco(preco);
        return servico;
    }

    private ItemServicoAgendamento itemServico(Agendamento agendamento, Servico servico) {
        ItemServicoAgendamento item = new ItemServicoAgendamento();
        item.setAgendamento(agendamento);
        item.setServico(servico);
        return item;
    }

    private ItemProdutoServico itemProduto(Servico servico, Produto produto, int quantidade) {
        ItemProdutoServico item = new ItemProdutoServico();
        item.setServico(servico);
        item.setProduto(produto);
        item.setQuantidadeNecessaria(quantidade);
        return item;
    }

    private Produto produto(String nome, double estoque) {
        Produto produto = new Produto();
        produto.setId(UUID.randomUUID());
        produto.setNome(nome);
        produto.setEstoque(estoque);
        return produto;
    }

    private OrdemServico ordem(String status, Agendamento agendamento) {
        OrdemServico ordem = new OrdemServico();
        ordem.setId(UUID.randomUUID());
        ordem.setStatus(status);
        ordem.setAgendamento(agendamento);
        when(ordemServicoDAO.findById(ordem.getId())).thenReturn(Optional.of(ordem));
        return ordem;
    }

    // ---- abrir ----

    @Test
    void abrirSomaOsPrecosDosServicosEUsaOUsuarioLogado() {
        Agendamento agendamento = agendamento("AGENDADO");
        Usuario logado = new Usuario();
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(ordemServicoDAO.existsByAgendamentoId(agendamento.getId())).thenReturn(false);
        when(itemServicoAgendamentoDAO.findByAgendamentoId(agendamento.getId())).thenReturn(List.of(
                itemServico(agendamento, servico(120.0)), itemServico(agendamento, servico(80.0))));
        when(usuarioDAO.findByLogin("func")).thenReturn(Optional.of(logado));
        when(ordemServicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemServico ordem = service.abrir(agendamento.getId(), null, "func");

        assertThat(ordem.getValorTotal()).isEqualTo(200.0);
        assertThat(ordem.getStatus()).isEqualTo("ABERTA");
        assertThat(ordem.getFuncionario()).isSameAs(logado);
        assertThat(ordem.getAgendamento()).isSameAs(agendamento);
        assertThat(ordem.getDataAbertura()).isNotNull();
    }

    @Test
    void abrirComFuncionarioInformadoUsaEsseFuncionario() {
        Agendamento agendamento = agendamento("AGENDADO");
        Usuario outro = new Usuario();
        UUID outroId = UUID.randomUUID();
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(itemServicoAgendamentoDAO.findByAgendamentoId(agendamento.getId()))
                .thenReturn(List.of(itemServico(agendamento, servico(50.0))));
        when(usuarioDAO.findById(outroId)).thenReturn(Optional.of(outro));
        when(ordemServicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemServico ordem = service.abrir(agendamento.getId(), outroId, "func");

        assertThat(ordem.getFuncionario()).isSameAs(outro);
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    void naoAbreOsParaAgendamentoCancelado() {
        Agendamento agendamento = agendamento("CANCELADO");
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> service.abrir(agendamento.getId(), null, "func"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AGENDADO");
        verify(ordemServicoDAO, never()).save(any());
    }

    @Test
    void naoAbreDuasOsParaOMesmoAgendamento() {
        Agendamento agendamento = agendamento("AGENDADO");
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(ordemServicoDAO.existsByAgendamentoId(agendamento.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.abrir(agendamento.getId(), null, "func"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já possui");
    }

    @Test
    void naoAbreOsParaAgendamentoSemServicos() {
        Agendamento agendamento = agendamento("AGENDADO");
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        when(itemServicoAgendamentoDAO.findByAgendamentoId(agendamento.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> service.abrir(agendamento.getId(), null, "func"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não possui serviços");
    }

    // ---- ciclo de vida ----

    @Test
    void iniciarPassaDeAbertaParaEmAndamento() {
        OrdemServico ordem = ordem("ABERTA", agendamento("AGENDADO"));
        when(ordemServicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertThat(service.iniciar(ordem.getId()).getStatus()).isEqualTo("EM_ANDAMENTO");
    }

    @Test
    void naoConcluiOsQueNaoFoiIniciada() {
        OrdemServico ordem = ordem("ABERTA", agendamento("AGENDADO"));

        assertThatThrownBy(() -> service.concluir(ordem.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(produtoDAO, never()).save(any());
    }

    @Test
    void cancelarCancelaTambemOAgendamento() {
        Agendamento agendamento = agendamento("AGENDADO");
        OrdemServico ordem = ordem("EM_ANDAMENTO", agendamento);
        when(ordemServicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemServico cancelada = service.cancelar(ordem.getId());

        assertThat(cancelada.getStatus()).isEqualTo("CANCELADA");
        assertThat(agendamento.getStatus()).isEqualTo("CANCELADO");
        verify(movimentacaoEstoqueDAO, never()).save(any());
    }

    @Test
    void naoCancelaOsConcluida() {
        OrdemServico ordem = ordem("CONCLUIDA", agendamento("CONCLUIDO"));

        assertThatThrownBy(() -> service.cancelar(ordem.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- conclusão e baixa de estoque ----

    @Test
    void concluirDaBaixaNoEstoqueSomandoProdutoRepetidoEntreServicos() {
        Agendamento agendamento = agendamento("AGENDADO");
        OrdemServico ordem = ordem("EM_ANDAMENTO", agendamento);
        Servico trocaOleo = servico(120.0);
        Servico revisao = servico(300.0);
        Produto oleo = produto("Óleo 5W30", 10.0);
        Produto filtro = produto("Filtro de óleo", 3.0);
        when(itemServicoAgendamentoDAO.findByAgendamentoId(agendamento.getId())).thenReturn(List.of(
                itemServico(agendamento, trocaOleo), itemServico(agendamento, revisao)));
        when(itemProdutoServicoDAO.findByServicoId(trocaOleo.getId())).thenReturn(List.of(
                itemProduto(trocaOleo, oleo, 4), itemProduto(trocaOleo, filtro, 1)));
        when(itemProdutoServicoDAO.findByServicoId(revisao.getId())).thenReturn(List.of(
                itemProduto(revisao, oleo, 5)));
        when(ordemServicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        OrdemServico concluida = service.concluir(ordem.getId());

        assertThat(concluida.getStatus()).isEqualTo("CONCLUIDA");
        assertThat(agendamento.getStatus()).isEqualTo("CONCLUIDO");
        assertThat(oleo.getEstoque()).isEqualTo(1.0);
        assertThat(filtro.getEstoque()).isEqualTo(2.0);

        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueDAO, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allSatisfy(mov -> {
                    assertThat(mov.getTipoMovimentacao()).isEqualTo(TipoMovimentacao.SAIDA);
                    assertThat(mov.getOrdemServico()).isSameAs(ordem);
                    assertThat(mov.getData()).isNotNull();
                })
                .extracting(MovimentacaoEstoque::getQuantidade)
                .containsExactlyInAnyOrder(9.0, 1.0);
    }

    @Test
    void estoqueInsuficienteNaoAlteraNada() {
        Agendamento agendamento = agendamento("AGENDADO");
        OrdemServico ordem = ordem("EM_ANDAMENTO", agendamento);
        Servico servico = servico(120.0);
        Produto oleo = produto("Óleo 5W30", 10.0);
        Produto filtro = produto("Filtro de óleo", 0.0);
        when(itemServicoAgendamentoDAO.findByAgendamentoId(agendamento.getId()))
                .thenReturn(List.of(itemServico(agendamento, servico)));
        when(itemProdutoServicoDAO.findByServicoId(servico.getId())).thenReturn(List.of(
                itemProduto(servico, oleo, 4), itemProduto(servico, filtro, 1)));

        assertThatThrownBy(() -> service.concluir(ordem.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estoque insuficiente de Filtro de óleo");

        assertThat(oleo.getEstoque()).isEqualTo(10.0);
        assertThat(ordem.getStatus()).isEqualTo("EM_ANDAMENTO");
        assertThat(agendamento.getStatus()).isEqualTo("AGENDADO");
        verify(produtoDAO, never()).save(any());
        verify(movimentacaoEstoqueDAO, never()).save(any());
    }
}
