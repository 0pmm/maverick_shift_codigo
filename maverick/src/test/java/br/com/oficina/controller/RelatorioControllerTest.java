package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.modelo.enuns.TipoRelatorio;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.RelatorioService;
import br.com.oficina.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RelatorioController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class RelatorioControllerTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private RelatorioService relatorioService;

    private String token(String login, Cargo cargo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin(login);
        usuario.setCargo(cargo);
        return "Bearer " + tokenService.gerar(usuario).valor();
    }

    @Test
    void gestorGeraRelatorioComOLoginDoToken() throws Exception {
        when(relatorioService.gerar(TipoRelatorio.FINANCEIRO, INICIO, FIM, "gestor"))
                .thenReturn(Map.of("tipoRelatorio", TipoRelatorio.FINANCEIRO));

        mockMvc.perform(post("/api/relatorios").header("Authorization", token("gestor", Cargo.GESTOR))
                .param("tipo", "FINANCEIRO").param("inicio", "2026-11-01").param("fim", "2026-11-30"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoRelatorio").value("FINANCEIRO"));
    }

    @Test
    void funcionarioNaoPodeGerarRelatorio() throws Exception {
        mockMvc.perform(post("/api/relatorios").header("Authorization", token("func", Cargo.FUNCIONARIO))
                .param("tipo", "GERAL").param("inicio", "2026-11-01").param("fim", "2026-11-30"))
                .andExpect(status().isForbidden());

        verify(relatorioService, never()).gerar(any(), any(), any(), any());
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(post("/api/relatorios")
                .param("tipo", "GERAL").param("inicio", "2026-11-01").param("fim", "2026-11-30"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void periodoInvalidoDevolve400() throws Exception {
        when(relatorioService.gerar(TipoRelatorio.GERAL, FIM, INICIO, "gestor"))
                .thenThrow(new IllegalArgumentException("A data final não pode ser antes da data inicial"));

        mockMvc.perform(post("/api/relatorios").header("Authorization", token("gestor", Cargo.GESTOR))
                .param("tipo", "GERAL").param("inicio", "2026-11-30").param("fim", "2026-11-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A data final não pode ser antes da data inicial"));
    }
}
