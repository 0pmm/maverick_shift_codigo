package br.com.oficina.controller;

import br.com.oficina.config.SecurityConfig;
import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.ItemProdutoServico;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Servico;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.modelo.enuns.Configuracao;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.ServicoService;
import br.com.oficina.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
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

@WebMvcTest(controllers = ServicoController.class)
@Import({ SecurityConfig.class, AutenticacaoService.class, TokenService.class })
class ServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UsuarioDAO usuarioDAO;

    @MockitoBean
    private ServicoService servicoService;

    private String token;

    @BeforeEach
    void gerarTokenDeFuncionario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste");
        usuario.setLogin("func");
        usuario.setCargo(Cargo.FUNCIONARIO);
        token = "Bearer " + tokenService.gerar(usuario).valor();
    }

    private Servico servico() {
        Servico servico = new Servico();
        servico.setId(UUID.randomUUID());
        servico.setDescricao("Troca de óleo");
        servico.setPreco(120.0);
        servico.setConfiguracao(Configuracao.SEDAN);
        return servico;
    }

    @Test
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioListaServicos() throws Exception {
        when(servicoService.listar(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/servicos").header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void criarServicoValidoDevolve201() throws Exception {
        when(servicoService.criar(any())).thenReturn(servico());

        mockMvc.perform(post("/api/servicos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"Troca de óleo\",\"preco\":120.0,\"configuracao\":\"SEDAN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.configuracao").value("SEDAN"));
    }

    @Test
    void criarServicoSemCamposObrigatoriosDevolve400() throws Exception {
        mockMvc.perform(post("/api/servicos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"descricao\":\"\",\"preco\":0}"))
                .andExpect(status().isBadRequest());

        verify(servicoService, never()).criar(any());
    }

    @Test
    void deletarServicoDevolve204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/servicos/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(servicoService).deletar(id);
    }

    @Test
    void vincularProdutoDevolve201SemExporOServico() throws Exception {
        UUID servicoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        Produto produto = new Produto();
        produto.setId(produtoId);
        produto.setNome("Óleo 5W30");
        ItemProdutoServico item = new ItemProdutoServico();
        item.setServico(servico());
        item.setProduto(produto);
        item.setQuantidadeNecessaria(4);
        when(servicoService.adicionarProduto(servicoId, produtoId, 4)).thenReturn(item);

        mockMvc.perform(post("/api/servicos/" + servicoId + "/produtos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":\"" + produtoId + "\",\"quantidadeNecessaria\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produto.nome").value("Óleo 5W30"))
                .andExpect(jsonPath("$.quantidadeNecessaria").value(4))
                .andExpect(jsonPath("$.servico").doesNotExist());
    }

    @Test
    void vincularProdutoComQuantidadeZeroDevolve400() throws Exception {
        UUID servicoId = UUID.randomUUID();

        mockMvc.perform(post("/api/servicos/" + servicoId + "/produtos").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":\"" + UUID.randomUUID() + "\",\"quantidadeNecessaria\":0}"))
                .andExpect(status().isBadRequest());

        verify(servicoService, never()).adicionarProduto(eq(servicoId), any(), any());
    }

    @Test
    void listarProdutosDoServico() throws Exception {
        UUID servicoId = UUID.randomUUID();
        when(servicoService.listarProdutos(servicoId)).thenReturn(List.of());

        mockMvc.perform(get("/api/servicos/" + servicoId + "/produtos").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
