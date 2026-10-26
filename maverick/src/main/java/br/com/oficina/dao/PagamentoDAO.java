package br.com.oficina.dao;

import br.com.oficina.modelo.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PagamentoDAO extends JpaRepository<Pagamento, UUID> {

    Page<Pagamento> findByClienteId(UUID clienteId, Pageable pageable);

    Page<Pagamento> findByDataBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    List<Pagamento> findByOrdemServicoId(UUID ordemServicoId);
}
