package br.com.oficina.dao;

import br.com.oficina.modelo.ItemServicoAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ItemServicoAgendamentoDAO extends JpaRepository<ItemServicoAgendamento, UUID> {

    List<ItemServicoAgendamento> findByAgendamentoId(UUID agendamentoId);
}
