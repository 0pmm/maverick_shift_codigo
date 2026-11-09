package br.com.oficina.dao;

import br.com.oficina.modelo.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovimentacaoEstoqueDAO extends JpaRepository<MovimentacaoEstoque, UUID> {

    List<MovimentacaoEstoque> findByOrdemServicoId(UUID ordemServicoId);
}
