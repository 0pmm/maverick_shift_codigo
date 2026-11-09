package br.com.oficina.dao;

import br.com.oficina.modelo.ItemProdutoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemProdutoServicoDAO extends JpaRepository<ItemProdutoServico, UUID> {

    List<ItemProdutoServico> findByServicoId(UUID servicoId);

    Optional<ItemProdutoServico> findByIdAndServicoId(UUID id, UUID servicoId);

    boolean existsByServicoIdAndProdutoId(UUID servicoId, UUID produtoId);

    void deleteByServicoId(UUID servicoId);
}
