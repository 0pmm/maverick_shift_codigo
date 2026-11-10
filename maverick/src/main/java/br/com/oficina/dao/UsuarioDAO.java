package br.com.oficina.dao;

import br.com.oficina.modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioDAO extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByLoginIgnoreCase(String login);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
