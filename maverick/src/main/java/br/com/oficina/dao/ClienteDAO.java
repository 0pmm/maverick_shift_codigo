package br.com.oficina.dao;

import br.com.oficina.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClienteDAO extends JpaRepository<Cliente, UUID> {
}
