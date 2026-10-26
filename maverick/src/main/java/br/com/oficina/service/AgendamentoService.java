package br.com.oficina.service;

import br.com.oficina.dao.AgendamentoDAO;
import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.ServicoDAO;
import br.com.oficina.modelo.Agendamento;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.Servico;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AgendamentoService {

    public static final String AGENDADO = "AGENDADO";
    public static final String CONCLUIDO = "CONCLUIDO";
    public static final String CANCELADO = "CANCELADO";

    private final AgendamentoDAO agendamentoDAO;
    private final ClienteDAO clienteDAO;
    private final ServicoDAO servicoDAO;

    public AgendamentoService(AgendamentoDAO agendamentoDAO, ClienteDAO clienteDAO, ServicoDAO servicoDAO) {
        this.agendamentoDAO = agendamentoDAO;
        this.clienteDAO = clienteDAO;
        this.servicoDAO = servicoDAO;
    }

    @Transactional(readOnly = true)
    public Page<Agendamento> listar(Pageable pageable) {
        return agendamentoDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Agendamento> listarPorPeriodo(LocalDate inicio, LocalDate fim, Pageable pageable) {
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        return agendamentoDAO.findByDataHoraBetween(inicio.atStartOfDay(), fim.atTime(23, 59, 59), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Agendamento> listarPorCliente(UUID clienteId, Pageable pageable) {
        return agendamentoDAO.findByClienteId(clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public Agendamento buscarPorId(UUID id) {
        return agendamentoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado: id=" + id));
    }

    public Agendamento criar(Agendamento agendamento) {
        agendamento.setId(null);
        agendamento.setDataHora(agendamento.getDataHora().withSecond(0).withNano(0));
        validarHorario(agendamento.getDataHora());

        agendamento.setCliente(buscarCliente(agendamento.getCliente().getId()));
        agendamento.setServico(buscarServico(agendamento.getServico().getId()));
        agendamento.setStatus(AGENDADO);
        return agendamentoDAO.save(agendamento);
    }

    public Agendamento atualizar(UUID id, Agendamento dados) {
        Agendamento existente = buscarPorId(id);
        verificarSeEstaAgendado(existente);

        LocalDateTime novoHorario = dados.getDataHora().withSecond(0).withNano(0);
        // so valida se mudou o horario, senao o proprio agendamento conta como conflito
        if (!novoHorario.equals(existente.getDataHora())) {
            validarHorario(novoHorario);
        }

        existente.setDataHora(novoHorario);
        existente.setCliente(buscarCliente(dados.getCliente().getId()));
        existente.setServico(buscarServico(dados.getServico().getId()));
        return agendamentoDAO.save(existente);
    }

    public Agendamento concluir(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        verificarSeEstaAgendado(agendamento);
        agendamento.setStatus(CONCLUIDO);
        return agendamentoDAO.save(agendamento);
    }

    public Agendamento cancelar(UUID id) {
        Agendamento agendamento = buscarPorId(id);
        verificarSeEstaAgendado(agendamento);
        agendamento.setStatus(CANCELADO);
        return agendamentoDAO.save(agendamento);
    }

    // concluido ou cancelado nao pode mais mexer
    private void verificarSeEstaAgendado(Agendamento agendamento) {
        if (!AGENDADO.equals(agendamento.getStatus())) {
            throw new IllegalArgumentException(
                    "Agendamento com status " + agendamento.getStatus() + " não pode ser alterado");
        }
    }

    private void validarHorario(LocalDateTime dataHora) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar em uma data que já passou");
        }
        // cancelado libera o horario
        if (agendamentoDAO.existsByDataHoraAndStatusNot(dataHora, CANCELADO)) {
            throw new IllegalArgumentException("Já existe um agendamento nesse horário");
        }
    }

    private Cliente buscarCliente(UUID id) {
        return clienteDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: id=" + id));
    }

    private Servico buscarServico(UUID id) {
        return servicoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado: id=" + id));
    }
}
