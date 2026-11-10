package br.com.oficina.service;

import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(Pageable pageable) {
        return usuarioDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorNome(String nome, Pageable pageable) {
        return usuarioDAO.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado: id=" + id));
    }

    public Usuario criar(Usuario usuario) {
        validar(usuario, true);

        usuario.setId(null);
        usuario.setLogin(usuario.getLogin().trim());
        validarLoginUnico(usuario.getLogin(), null);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioDAO.save(usuario);
    }

    public Usuario atualizar(UUID id, Usuario dados) {
        Usuario existente = buscarPorId(id);
        validar(dados, false);

        String login = dados.getLogin().trim();
        validarLoginUnico(login, id);

        existente.setNome(dados.getNome());
        existente.setLogin(login);
        existente.setCargo(dados.getCargo());
        // Na edição a senha é opcional: em branco mantém o hash atual
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        return usuarioDAO.save(existente);
    }

    public void deletar(UUID id) {
        usuarioDAO.delete(buscarPorId(id));
    }

    // Regra do UC09 - caso de teste 02: impedir login (e-mail) duplicado
    private void validarLoginUnico(String login, UUID idAtual) {
        usuarioDAO.findByLoginIgnoreCase(login).ifPresent(outro -> {
            if (idAtual == null || !outro.getId().equals(idAtual)) {
                throw new IllegalArgumentException("Este login já está em uso por outro funcionário.");
            }
        });
    }

    private void validar(Usuario usuario, boolean senhaObrigatoria) {
        if (usuario == null) {
            throw new IllegalArgumentException("Os dados do funcionário são obrigatórios.");
        }
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do funcionário é obrigatório.");
        }
        if (usuario.getLogin() == null || usuario.getLogin().isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        if (usuario.getCargo() == null) {
            throw new IllegalArgumentException("O perfil é obrigatório.");
        }
        if (senhaObrigatoria && (usuario.getSenha() == null || usuario.getSenha().isBlank())) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
    }
}
