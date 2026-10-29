package br.com.oficina.dao;

import br.com.oficina.modelo.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoDAO extends JpaRepository<Produto, UUID> {

    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}