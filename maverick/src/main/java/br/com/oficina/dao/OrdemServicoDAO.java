package br.com.oficina.dao;

import br.com.oficina.modelo.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrdemServicoDAO extends JpaRepository<OrdemServico, UUID> {

    Page<OrdemServico> findByStatus(String status, Pageable pageable);

    boolean existsByAgendamentoId(UUID agendamentoId);
}
