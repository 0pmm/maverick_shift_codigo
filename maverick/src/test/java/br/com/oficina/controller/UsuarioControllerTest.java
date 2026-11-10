package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.TokenService;
import br.com.oficina.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private UsuarioService usuarioService;

    private String tokenGestor;

    @BeforeEach
    void gerarTokenDeGestor() {
        tokenGestor = "Bearer " + tokenService.gerar(usuario("gestor", Cargo.GESTOR)).valor();
    }

    private Usuario usuario(String login, Cargo cargo) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Maria Oliveira");
        usuario.setLogin(login);
        usuario.setSenha("$2a$10$hash");
        usuario.setCargo(cargo);
        return usuario;
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioNaoAcessaFuncionarios() throws Exception {
        String tokenFuncionario = "Bearer " + tokenService.gerar(usuario("func", Cargo.FUNCIONARIO)).valor();

        mockMvc.perform(get("/api/usuarios").header("Authorization", tokenFuncionario))
                .andExpect(status().isForbidden());
    }

    @Test
    void gestorListaFuncionarios() throws Exception {
        when(usuarioService.listar(any())).thenReturn(new PageImpl<>(List.of(usuario("maria@oficina.com", Cargo.FUNCIONARIO))));

        mockMvc.perform(get("/api/usuarios").header("Authorization", tokenGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria Oliveira"))
                .andExpect(jsonPath("$.content[0].senha").doesNotExist());
    }

    @Test
    void gestorBuscaFuncionariosPeloNome() throws Exception {
        when(usuarioService.buscarPorNome(eq("maria"), any()))
                .thenReturn(new PageImpl<>(List.of(usuario("maria@oficina.com", Cargo.FUNCIONARIO))));

        mockMvc.perform(get("/api/usuarios/busca").param("nome", "maria").header("Authorization", tokenGestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].login").value("maria@oficina.com"));
    }

    @Test
    void criarDevolve201SemExporSenha() throws Exception {
        when(usuarioService.criar(any())).thenReturn(usuario("maria@oficina.com", Cargo.FUNCIONARIO));

        mockMvc.perform(post("/api/usuarios").header("Authorization", tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria Oliveira\",\"login\":\"maria@oficina.com\","
                        + "\"senha\":\"segredo\",\"cargo\":\"FUNCIONARIO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Maria Oliveira"))
                .andExpect(jsonPath("$.cargo").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void criarSemCamposObrigatoriosDevolve400() throws Exception {
        mockMvc.perform(post("/api/usuarios").header("Authorization", tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"\",\"login\":\"maria@oficina.com\",\"cargo\":\"FUNCIONARIO\"}"))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).criar(any());
    }

    @Test
    void atualizarDevolve200() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioService.atualizar(eq(id), any()))
                .thenReturn(usuario("maria@oficina.com", Cargo.GESTOR));

        mockMvc.perform(put("/api/usuarios/" + id).header("Authorization", tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria Oliveira\",\"login\":\"maria@oficina.com\",\"cargo\":\"GESTOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo").value("GESTOR"));
    }

    @Test
    void deletarDevolve204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/usuarios/" + id).header("Authorization", tokenGestor))
                .andExpect(status().isNoContent());

        verify(usuarioService).deletar(id);
    }

    @Test
    void loginDuplicadoDevolve400ComMensagem() throws Exception {
        when(usuarioService.criar(any()))
                .thenThrow(new IllegalArgumentException("Este login já está em uso por outro funcionário."));

        mockMvc.perform(post("/api/usuarios").header("Authorization", tokenGestor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria Oliveira\",\"login\":\"maria@oficina.com\","
                        + "\"senha\":\"segredo\",\"cargo\":\"FUNCIONARIO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Este login já está em uso por outro funcionário."));
    }
}
