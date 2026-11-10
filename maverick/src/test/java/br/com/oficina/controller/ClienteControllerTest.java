package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Cliente;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.ClienteService;
import br.com.oficina.service.TokenService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private ClienteService clienteService;

    private String token;

    @BeforeEach
    void gerarTokenDeFuncionario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin("func");
        usuario.setCargo(Cargo.FUNCIONARIO);
        token = "Bearer " + tokenService.gerar(usuario).valor();
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNome("Maria Oliveira");
        cliente.setTelefone("(11) 98765-4321");
        cliente.setEmail("maria@email.com");
        return cliente;
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioBuscaClientesPeloNome() throws Exception {
        when(clienteService.buscarPorNome(eq("maria"), any())).thenReturn(new PageImpl<>(List.of(cliente())));

        mockMvc.perform(get("/api/clientes/busca").param("nome", "maria").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria Oliveira"));
    }

    @Test
    void criarDevolve201() throws Exception {
        when(clienteService.criar(any())).thenReturn(cliente());

        mockMvc.perform(post("/api/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria Oliveira\",\"telefone\":\"(11) 98765-4321\",\"email\":\"maria@email.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Maria Oliveira"));
    }

    @Test
    void criarSemTelefoneOuComEmailInvalidoDevolve400() throws Exception {
        mockMvc.perform(post("/api/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Maria\",\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).criar(any());
    }

    @Test
    void deletarDevolve204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/clientes/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(clienteService).deletar(id);
    }
}
