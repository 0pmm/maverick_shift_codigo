package br.com.oficina.dao;

import br.com.oficina.modelo.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrdemServicoDAO extends JpaRepository<OrdemServico, UUID> {
}
