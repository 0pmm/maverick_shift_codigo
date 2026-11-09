package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.ProdutoService;
import br.com.oficina.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AuthController.class, ProdutoController.class })
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private ProdutoService produtoService;

    private Usuario usuario(String login, String senha, Cargo cargo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin(login);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setCargo(cargo);
        return usuario;
    }

    @Test
    void loginValidoDevolveToken() throws Exception {
        when(usuarioDAO.findByLogin("gestor")).thenReturn(Optional.of(usuario("gestor", "segredo", Cargo.GESTOR)));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"gestor\",\"senha\":\"segredo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.cargo").value("GESTOR"));
    }

    @Test
    void senhaErradaDevolve401() throws Exception {
        when(usuarioDAO.findByLogin("gestor")).thenReturn(Optional.of(usuario("gestor", "segredo", Cargo.GESTOR)));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"gestor\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Login ou senha inválidos"));
    }

    @Test
    void loginInexistenteDevolve401() throws Exception {
        when(usuarioDAO.findByLogin("ninguem")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"ninguem\",\"senha\":\"qualquer\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaSemTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioAcessaProdutos() throws Exception {
        when(produtoService.listar(any())).thenReturn(Page.empty());
        String token = tokenService.gerar(usuario("func", "x", Cargo.FUNCIONARIO)).valor();

        mockMvc.perform(get("/api/produtos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void funcionarioNaoAcessaRotaDeGestor() throws Exception {
        String token = tokenService.gerar(usuario("func", "x", Cargo.FUNCIONARIO)).valor();

        mockMvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void gestorHerdaPermissoesDeFuncionario() throws Exception {
        when(produtoService.listar(any())).thenReturn(Page.empty());
        String token = tokenService.gerar(usuario("gestor", "x", Cargo.GESTOR)).valor();

        mockMvc.perform(get("/api/produtos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
