package br.com.oficina.service;

import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario(UUID id, String login, String senha) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Maria Oliveira");
        usuario.setLogin(login);
        usuario.setSenha(senha);
        usuario.setCargo(Cargo.FUNCIONARIO);
        return usuario;
    }

    @Test
    void criarIgnoraIdECodificaSenha() {
        Usuario novo = usuario(UUID.randomUUID(), "maria@oficina.com", "segredo");
        when(usuarioDAO.findByLoginIgnoreCase("maria@oficina.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("segredo")).thenReturn("$2a$10$hash");
        when(usuarioDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = usuarioService.criar(novo);

        assertThat(salvo.getId()).isNull();
        assertThat(salvo.getSenha()).isEqualTo("$2a$10$hash");
    }

    @Test
    void criarComLoginDuplicadoEhRecusado() {
        UUID id = UUID.randomUUID();
        when(usuarioDAO.findByLoginIgnoreCase("maria@oficina.com"))
                .thenReturn(Optional.of(usuario(id, "maria@oficina.com", "$2a$10$hash")));

        assertThatThrownBy(() -> usuarioService.criar(usuario(null, "maria@oficina.com", "segredo")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está em uso");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    void criarSemSenhaEhRecusado() {
        assertThatThrownBy(() -> usuarioService.criar(usuario(null, "maria@oficina.com", " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("senha");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    void atualizarSemSenhaMantemOHashAtual() {
        UUID id = UUID.randomUUID();
        Usuario existente = usuario(id, "maria@oficina.com", "$2a$10$hashAtual");
        Usuario dados = usuario(null, "maria@oficina.com", null);
        dados.setNome("Maria Souza");
        dados.setCargo(Cargo.GESTOR);

        when(usuarioDAO.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioDAO.findByLoginIgnoreCase("maria@oficina.com")).thenReturn(Optional.of(existente));
        when(usuarioDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario atualizado = usuarioService.atualizar(id, dados);

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getNome()).isEqualTo("Maria Souza");
        assertThat(atualizado.getCargo()).isEqualTo(Cargo.GESTOR);
        assertThat(atualizado.getSenha()).isEqualTo("$2a$10$hashAtual");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void atualizarComSenhaNovaCodifica() {
        UUID id = UUID.randomUUID();
        Usuario existente = usuario(id, "maria@oficina.com", "$2a$10$hashAtual");
        Usuario dados = usuario(null, "maria@oficina.com", "novaSenha");

        when(usuarioDAO.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioDAO.findByLoginIgnoreCase("maria@oficina.com")).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("novaSenha")).thenReturn("$2a$10$hashNovo");
        when(usuarioDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario atualizado = usuarioService.atualizar(id, dados);

        assertThat(atualizado.getSenha()).isEqualTo("$2a$10$hashNovo");
    }

    @Test
    void atualizarComLoginDeOutroFuncionarioEhRecusado() {
        UUID id = UUID.randomUUID();
        Usuario existente = usuario(id, "maria@oficina.com", "$2a$10$hashAtual");
        Usuario outro = usuario(UUID.randomUUID(), "outro@oficina.com", "$2a$10$hash");

        when(usuarioDAO.findById(id)).thenReturn(Optional.of(existente));
        when(usuarioDAO.findByLoginIgnoreCase("outro@oficina.com")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> usuarioService.atualizar(id, usuario(null, "outro@oficina.com", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está em uso");

        verify(usuarioDAO, never()).save(any());
    }

    @Test
    void buscarInexistenteLancaEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(usuarioDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deletarInexistenteNaoChamaDelete() {
        UUID id = UUID.randomUUID();
        when(usuarioDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.deletar(id))
                .isInstanceOf(EntityNotFoundException.class);
        verify(usuarioDAO, never()).delete(any());
    }
}
