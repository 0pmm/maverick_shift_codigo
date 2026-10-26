package br.com.oficina.dao;

import br.com.oficina.modelo.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AgendamentoDAO extends JpaRepository<Agendamento, UUID> {

    Page<Agendamento> findByClienteId(UUID clienteId, Pageable pageable);

    Page<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    boolean existsByDataHoraAndStatusNot(LocalDateTime dataHora, String status);
}
