package br.com.oficina.dao;

import br.com.oficina.modelo.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoDAO extends JpaRepository<Servico, UUID> {

    Page<Servico> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);
}
