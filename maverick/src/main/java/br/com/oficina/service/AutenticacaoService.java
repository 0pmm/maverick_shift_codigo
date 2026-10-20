package br.com.oficina.service;

import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega o usuário pelo login para o Spring Security conferir a senha.
 * O cargo vira a role (ROLE_FUNCIONARIO / ROLE_GESTOR).
 */
@Service
@Transactional(readOnly = true)
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioDAO usuarioDAO;

    public AutenticacaoService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        Usuario usuario = buscarPorLogin(login);
        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenha())
                .roles(usuario.getCargo().name())
                .build();
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioDAO.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
    }
}
