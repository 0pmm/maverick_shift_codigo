package br.com.oficina.service;

import br.com.oficina.dao.AgendamentoDAO;
import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.ServicoDAO;
import br.com.oficina.modelo.Agendamento;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.Servico;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    private static final LocalDateTime AMANHA = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

    @Mock
    private AgendamentoDAO agendamentoDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private ServicoDAO servicoDAO;

    @InjectMocks
    private AgendamentoService service;

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        return cliente;
    }

    private Servico servico() {
        Servico servico = new Servico();
        servico.setId(UUID.randomUUID());
        servico.setPreco(80.0);
        return servico;
    }

    private Agendamento pedido(LocalDateTime dataHora, Cliente cliente, Servico servico) {
        Agendamento agendamento = new Agendamento();
        agendamento.setDataHora(dataHora);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        return agendamento;
    }

    private Agendamento existente(String status) {
        Agendamento agendamento = pedido(AMANHA, cliente(), servico());
        agendamento.setId(UUID.randomUUID());
        agendamento.setStatus(status);
        when(agendamentoDAO.findById(agendamento.getId())).thenReturn(Optional.of(agendamento));
        return agendamento;
    }

    // ---- criar ----

    @Test
    void criarSalvaComoAgendadoComClienteEServicoDoBanco() {
        Cliente cliente = cliente();
        Servico servico = servico();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(servicoDAO.findById(servico.getId())).thenReturn(Optional.of(servico));
        when(agendamentoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Agendamento criado = service.criar(pedido(AMANHA.withSecond(42), cliente, servico));

        assertThat(criado.getStatus()).isEqualTo(AgendamentoService.AGENDADO);
        assertThat(criado.getCliente()).isSameAs(cliente);
        assertThat(criado.getServico()).isSameAs(servico);
        assertThat(criado.getDataHora()).isEqualTo(AMANHA.withSecond(0).withNano(0));
    }

    @Test
    void naoAgendaNoPassado() {
        Agendamento pedido = pedido(LocalDateTime.now().minusDays(1), cliente(), servico());

        assertThatThrownBy(() -> service.criar(pedido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já passou");
        verify(agendamentoDAO, never()).save(any());
    }

    @Test
    void naoAgendaDoisNoMesmoHorario() {
        LocalDateTime horario = AMANHA.withSecond(0).withNano(0);
        when(agendamentoDAO.existsByDataHoraAndStatusNot(horario, AgendamentoService.CANCELADO)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(pedido(horario, cliente(), servico())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe");
        verify(agendamentoDAO, never()).save(any());
    }

    @Test
    void criarComServicoInexistenteFalha() {
        Cliente cliente = cliente();
        when(clienteDAO.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(servicoDAO.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(pedido(AMANHA, cliente, servico())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Serviço não encontrado");
    }

    // ---- atualizar ----

    @Test
    void atualizarNoMesmoHorarioNaoContaComoConflito() {
        Agendamento agendamento = existente(AgendamentoService.AGENDADO);
        agendamento.setDataHora(AMANHA.withSecond(0).withNano(0));
        Servico outroServico = servico();
        when(clienteDAO.findById(agendamento.getCliente().getId())).thenReturn(Optional.of(agendamento.getCliente()));
        when(servicoDAO.findById(outroServico.getId())).thenReturn(Optional.of(outroServico));
        when(agendamentoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Agendamento atualizado = service.atualizar(agendamento.getId(),
                pedido(agendamento.getDataHora(), agendamento.getCliente(), outroServico));

        assertThat(atualizado.getServico()).isSameAs(outroServico);
        verify(agendamentoDAO, never()).existsByDataHoraAndStatusNot(any(), eq(AgendamentoService.CANCELADO));
    }

    @Test
    void naoAtualizaAgendamentoConcluido() {
        Agendamento agendamento = existente(AgendamentoService.CONCLUIDO);

        assertThatThrownBy(() -> service.atualizar(agendamento.getId(), pedido(AMANHA, cliente(), servico())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CONCLUIDO");
    }

    // ---- concluir / cancelar ----

    @Test
    void concluirMudaStatus() {
        Agendamento agendamento = existente(AgendamentoService.AGENDADO);
        when(agendamentoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertThat(service.concluir(agendamento.getId()).getStatus()).isEqualTo(AgendamentoService.CONCLUIDO);
    }

    @Test
    void naoCancelaAgendamentoJaCancelado() {
        Agendamento agendamento = existente(AgendamentoService.CANCELADO);

        assertThatThrownBy(() -> service.cancelar(agendamento.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(agendamentoDAO, never()).save(any());
    }

    // ---- consultas ----

    @Test
    void periodoComFimAntesDoInicioFalha() {
        LocalDate hoje = LocalDate.now();

        assertThatThrownBy(() -> service.listarPorPeriodo(hoje, hoje.minusDays(1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buscarInexistenteFalha() {
        UUID id = UUID.randomUUID();
        when(agendamentoDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
