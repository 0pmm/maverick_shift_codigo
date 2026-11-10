package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Pagamento;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.PagamentoService;
import br.com.oficina.service.TokenService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(controllers = PagamentoController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private PagamentoService pagamentoService;

    private String token;

    @BeforeEach
    void gerarTokenDeFuncionario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin("func");
        usuario.setCargo(Cargo.FUNCIONARIO);
        token = "Bearer " + tokenService.gerar(usuario).valor();
    }

    private Pagamento pagamento(double valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setId(UUID.randomUUID());
        pagamento.setValor(valor);
        return pagamento;
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/pagamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarDevolve201() throws Exception {
        when(pagamentoService.criar(any())).thenReturn(pagamento(150.0));

        mockMvc.perform(post("/api/pagamentos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":150.0,\"cliente\":{\"id\":\"" + UUID.randomUUID() + "\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(150.0));
    }

    @Test
    void criarSemClienteOuComValorNegativoDevolve400() throws Exception {
        mockMvc.perform(post("/api/pagamentos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cliente").exists())
                .andExpect(jsonPath("$.valor").exists());

        verify(pagamentoService, never()).criar(any());
    }

    @Test
    void regraDeNegocioDoServiceVira400() throws Exception {
        when(pagamentoService.criar(any()))
                .thenThrow(new IllegalArgumentException("Valor maior que o saldo da ordem de serviço"));

        mockMvc.perform(post("/api/pagamentos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":999.0,\"cliente\":{\"id\":\"" + UUID.randomUUID() + "\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Valor maior que o saldo da ordem de serviço"));
    }

    @Test
    void buscarInexistenteDevolve404() throws Exception {
        UUID id = UUID.randomUUID();
        when(pagamentoService.buscarPorId(id)).thenThrow(new EntityNotFoundException("Pagamento não encontrado"));

        mockMvc.perform(get("/api/pagamentos/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void periodoRepassaAsDatas() throws Exception {
        when(pagamentoService.listarPorPeriodo(any(), any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/pagamentos/periodo").header("Authorization", token)
                .param("inicio", "2026-11-01").param("fim", "2026-11-30"))
                .andExpect(status().isOk());

        verify(pagamentoService).listarPorPeriodo(eq(LocalDate.of(2026, 11, 1)), eq(LocalDate.of(2026, 11, 30)),
                any());
    }

    @Test
    void deletarDevolve204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/pagamentos/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(pagamentoService).deletar(id);
    }
}
