package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.OrdemServico;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.OrdemServicoService;
import br.com.oficina.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrdemServicoController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    private String token;

    @BeforeEach
    void gerarTokenDeFuncionario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin("func");
        usuario.setCargo(Cargo.FUNCIONARIO);
        token = "Bearer " + tokenService.gerar(usuario).valor();
    }

    private OrdemServico ordem(String status) {
        OrdemServico ordem = new OrdemServico();
        ordem.setId(UUID.randomUUID());
        ordem.setStatus(status);
        ordem.setValorTotal(200.0);
        return ordem;
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/ordens-servico"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void abrirUsaOLoginDoToken() throws Exception {
        UUID agendamentoId = UUID.randomUUID();
        when(ordemServicoService.abrir(eq(agendamentoId), isNull(), eq("func"))).thenReturn(ordem("ABERTA"));

        mockMvc.perform(post("/api/ordens-servico").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"agendamentoId\":\"" + agendamentoId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorTotal").value(200.0));
    }

    @Test
    void abrirSemAgendamentoDevolve400() throws Exception {
        mockMvc.perform(post("/api/ordens-servico").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(ordemServicoService, never()).abrir(any(), any(), any());
    }

    @Test
    void concluirDevolveAOrdemConcluida() throws Exception {
        UUID id = UUID.randomUUID();
        when(ordemServicoService.concluir(id)).thenReturn(ordem("CONCLUIDA"));

        mockMvc.perform(patch("/api/ordens-servico/" + id + "/concluir").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void iniciarECancelarUsamPatch() throws Exception {
        UUID id = UUID.randomUUID();
        when(ordemServicoService.iniciar(id)).thenReturn(ordem("EM_ANDAMENTO"));
        when(ordemServicoService.cancelar(id)).thenReturn(ordem("CANCELADA"));

        mockMvc.perform(patch("/api/ordens-servico/" + id + "/iniciar").header("Authorization", token))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mockMvc.perform(patch("/api/ordens-servico/" + id + "/cancelar").header("Authorization", token))
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }
}
