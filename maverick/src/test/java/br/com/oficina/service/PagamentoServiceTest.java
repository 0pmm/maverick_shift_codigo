package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.OrdemServicoDAO;
import br.com.oficina.dao.PagamentoDAO;
import br.com.oficina.modelo.Agendamento;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.OrdemServico;
import br.com.oficina.modelo.Pagamento;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoDAO pagamentoDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private OrdemServicoDAO ordemServicoDAO;

    @InjectMocks
    private PagamentoService service;

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Maria");
        return cliente;
    }

    private OrdemServico ordem(Cliente dono, double valorTotal) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(UUID.randomUUID());
        agendamento.setCliente(dono);

        OrdemServico ordem = new OrdemServico();
        ordem.setId(UUID.randomUUID());
        ordem.setValorTotal(valorTotal);
        ordem.setAgendamento(agendamento);
        return ordem;
    }

    private Pagamento pagamento(Cliente cliente, OrdemServico ordem, double valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setCliente(cliente);
        pagamento.setOrdemServico(ordem);
        pagamento.setValor(valor);
        return pagamento;
    }

    // pagamento ja salvo antes (com id)
    private Pagamento existente(Cliente cliente, OrdemServico ordem, double valor) {
        Pagamento pagamento = pagamento(cliente, ordem, valor);
        pagamento.setId(UUID.randomUUID());
        pagamento.setData(LocalDateTime.now().minusDays(1));
        return pagamento;
    }

    private void saveDevolveOProprioPagamento() {
        when(pagamentoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @Test
    void criarSemOrdemDeServicoUsaADataAtual() {
        Cliente cliente = cliente();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        saveDevolveOProprioPagamento();

        Pagamento salvo = service.criar(pagamento(cliente, null, 80.0));

        assertThat(salvo.getData()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(salvo.getCliente()).isSameAs(cliente);
        assertThat(salvo.getOrdemServico()).isNull();
    }

    @Test
    void criarIgnoraOIdQueVemNoCorpo() {
        Cliente cliente = cliente();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        saveDevolveOProprioPagamento();
        Pagamento novo = pagamento(cliente, null, 80.0);
        novo.setId(UUID.randomUUID());

        assertThat(service.criar(novo).getId()).isNull();
    }

    @Test
    void criarComClienteInexistenteDaErro() {
        Cliente cliente = cliente();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(pagamento(cliente, null, 80.0)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cliente");
        verify(pagamentoDAO, never()).save(any());
    }

    @Test
    void criarComOrdemInexistenteDaErro() {
        Cliente cliente = cliente();
        OrdemServico ordem = ordem(cliente, 200.0);
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ordemServicoDAO.findById(ordem.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(pagamento(cliente, ordem, 80.0)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Ordem");
        verify(pagamentoDAO, never()).save(any());
    }

    @Test
    void criarComDataNoFuturoDaErro() {
        Cliente cliente = cliente();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        Pagamento pagamento = pagamento(cliente, null, 80.0);
        pagamento.setData(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> service.criar(pagamento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("futuro");
        verify(pagamentoDAO, never()).save(any());
    }

    @Test
    void criarComOrdemDeOutroClienteDaErro() {
        Cliente cliente = cliente();
        OrdemServico ordemDeOutro = ordem(cliente(), 200.0);
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ordemServicoDAO.findById(ordemDeOutro.getId())).thenReturn(Optional.of(ordemDeOutro));

        assertThatThrownBy(() -> service.criar(pagamento(cliente, ordemDeOutro, 80.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outro cliente");
        verify(pagamentoDAO, never()).save(any());
    }

    @Test
    void criarComValorMaiorQueOSaldoDaErro() {
        Cliente cliente = cliente();
        OrdemServico ordem = ordem(cliente, 200.0);
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ordemServicoDAO.findById(ordem.getId())).thenReturn(Optional.of(ordem));
        when(pagamentoDAO.findByOrdemServicoId(ordem.getId()))
                .thenReturn(List.of(existente(cliente, ordem, 150.0)));

        assertThatThrownBy(() -> service.criar(pagamento(cliente, ordem, 60.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("saldo");
        verify(pagamentoDAO, never()).save(any());
    }

    @Test
    void criarPagandoExatamenteOSaldoFunciona() {
        Cliente cliente = cliente();
        OrdemServico ordem = ordem(cliente, 200.0);
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ordemServicoDAO.findById(ordem.getId())).thenReturn(Optional.of(ordem));
        // 0.1 + 0.2 nao da 0.3 certinho em double, por isso o service compara em centavos
        when(pagamentoDAO.findByOrdemServicoId(ordem.getId())).thenReturn(List.of(
                existente(cliente, ordem, 0.1), existente(cliente, ordem, 0.2), existente(cliente, ordem, 199.4)));
        saveDevolveOProprioPagamento();

        Pagamento salvo = service.criar(pagamento(cliente, ordem, 0.3));

        assertThat(salvo.getOrdemServico()).isSameAs(ordem);
    }

    @Test
    void atualizarNaoSomaOValorAntigoDoProprioPagamento() {
        Cliente cliente = cliente();
        OrdemServico ordem = ordem(cliente, 200.0);
        Pagamento atual = existente(cliente, ordem, 150.0);
        LocalDateTime dataOriginal = atual.getData();
        when(pagamentoDAO.findById(atual.getId())).thenReturn(Optional.of(atual));
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(ordemServicoDAO.findById(ordem.getId())).thenReturn(Optional.of(ordem));
        when(pagamentoDAO.findByOrdemServicoId(ordem.getId())).thenReturn(List.of(atual));
        saveDevolveOProprioPagamento();

        // sem data no corpo: mantem a data que ja estava
        Pagamento salvo = service.atualizar(atual.getId(), pagamento(cliente, ordem, 200.0));

        assertThat(salvo.getValor()).isEqualTo(200.0);
        assertThat(salvo.getData()).isEqualTo(dataOriginal);
    }

    @Test
    void atualizarPagamentoInexistenteDaErro() {
        UUID id = UUID.randomUUID();
        when(pagamentoDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, pagamento(cliente(), null, 10.0)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deletarRemoveOPagamentoEncontrado() {
        Pagamento atual = existente(cliente(), null, 50.0);
        when(pagamentoDAO.findById(atual.getId())).thenReturn(Optional.of(atual));

        service.deletar(atual.getId());

        verify(pagamentoDAO).delete(atual);
    }

    @Test
    void listarPorPeriodoPegaODiaInteiro() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);
        when(pagamentoDAO.findByDataBetween(any(), any(), any())).thenReturn(Page.empty());

        service.listarPorPeriodo(inicio, fim, Pageable.unpaged());

        verify(pagamentoDAO).findByDataBetween(
                LocalDateTime.of(2026, 11, 1, 0, 0), LocalDateTime.of(2026, 11, 30, 23, 59, 59), Pageable.unpaged());
    }

    @Test
    void listarPorPeriodoComFimAntesDoInicioDaErro() {
        assertThatThrownBy(() -> service.listarPorPeriodo(
                LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), Pageable.unpaged()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(pagamentoDAO, never()).findByDataBetween(any(), any(), any());
    }
}
