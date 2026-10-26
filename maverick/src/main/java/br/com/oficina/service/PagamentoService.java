package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.dao.OrdemServicoDAO;
import br.com.oficina.dao.PagamentoDAO;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.OrdemServico;
import br.com.oficina.modelo.Pagamento;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PagamentoService {

    private final PagamentoDAO pagamentoDAO;
    private final ClienteDAO clienteDAO;
    private final OrdemServicoDAO ordemServicoDAO;

    public PagamentoService(PagamentoDAO pagamentoDAO, ClienteDAO clienteDAO, OrdemServicoDAO ordemServicoDAO) {
        this.pagamentoDAO = pagamentoDAO;
        this.clienteDAO = clienteDAO;
        this.ordemServicoDAO = ordemServicoDAO;
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listar(Pageable pageable) {
        return pagamentoDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listarPorPeriodo(LocalDate inicio, LocalDate fim, Pageable pageable) {
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final não pode ser antes da data inicial");
        }
        return pagamentoDAO.findByDataBetween(inicio.atStartOfDay(), fim.atTime(23, 59, 59), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listarPorCliente(UUID clienteId, Pageable pageable) {
        return pagamentoDAO.findByClienteId(clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarPorOrdemServico(UUID ordemServicoId) {
        return pagamentoDAO.findByOrdemServicoId(ordemServicoId);
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPorId(UUID id) {
        return pagamentoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado: id=" + id));
    }

    public Pagamento criar(Pagamento pagamento) {
        pagamento.setId(null);
        if (pagamento.getData() == null) {
            pagamento.setData(LocalDateTime.now());
        }
        pagamento.setCliente(buscarCliente(pagamento.getCliente().getId()));
        pagamento.setOrdemServico(buscarOrdemServico(pagamento.getOrdemServico()));
        validar(pagamento, null);
        return pagamentoDAO.save(pagamento);
    }

    public Pagamento atualizar(UUID id, Pagamento dados) {
        Pagamento existente = buscarPorId(id);
        // se nao mandar a data mantem a que ja estava
        if (dados.getData() != null) {
            existente.setData(dados.getData());
        }
        existente.setValor(dados.getValor());
        existente.setDescricao(dados.getDescricao());
        existente.setCliente(buscarCliente(dados.getCliente().getId()));
        existente.setOrdemServico(buscarOrdemServico(dados.getOrdemServico()));
        validar(existente, existente.getId());
        return pagamentoDAO.save(existente);
    }

    public void deletar(UUID id) {
        pagamentoDAO.delete(buscarPorId(id));
    }

    // idIgnorado = o proprio pagamento quando for alteracao, pra nao somar o valor antigo dele
    private void validar(Pagamento pagamento, UUID idIgnorado) {
        if (pagamento.getData().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data do pagamento não pode ser no futuro");
        }

        OrdemServico ordemServico = pagamento.getOrdemServico();
        if (ordemServico == null) {
            return;
        }
        if (!ordemServico.getAgendamento().getCliente().equals(pagamento.getCliente())) {
            throw new IllegalArgumentException("Essa ordem de serviço é de outro cliente");
        }

        double jaPago = 0;
        for (Pagamento outro : pagamentoDAO.findByOrdemServicoId(ordemServico.getId())) {
            if (!outro.getId().equals(idIgnorado)) {
                jaPago += outro.getValor();
            }
        }
        double saldo = ordemServico.getValorTotal() - jaPago;
        // compara em centavos por causa do arredondamento do double
        if (Math.round(pagamento.getValor() * 100) > Math.round(saldo * 100)) {
            throw new IllegalArgumentException(
                    String.format("Valor maior que o saldo da ordem de serviço (saldo: R$ %.2f)", saldo));
        }
    }

    private Cliente buscarCliente(UUID id) {
        return clienteDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: id=" + id));
    }

    // a ordem de servico e opcional no pagamento
    private OrdemServico buscarOrdemServico(OrdemServico ordemServico) {
        if (ordemServico == null) {
            return null;
        }
        return ordemServicoDAO.findById(ordemServico.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordem de serviço não encontrada: id=" + ordemServico.getId()));
    }
}
