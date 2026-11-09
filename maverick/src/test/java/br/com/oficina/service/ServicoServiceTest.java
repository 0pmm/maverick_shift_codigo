package br.com.oficina.service;

import br.com.oficina.dao.ItemProdutoServicoDAO;
import br.com.oficina.dao.ServicoDAO;
import br.com.oficina.modelo.ItemProdutoServico;
import br.com.oficina.modelo.Produto;
import br.com.oficina.modelo.Servico;
import br.com.oficina.modelo.enuns.Configuracao;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoDAO servicoDAO;

    @Mock
    private ItemProdutoServicoDAO itemDAO;

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servico(UUID id) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setDescricao("Alinhamento");
        servico.setPreco(90.0);
        servico.setConfiguracao(Configuracao.HATCH);
        return servico;
    }

    @Test
    void criarIgnoraIdEnviadoPeloCliente() {
        Servico novo = servico(UUID.randomUUID());
        when(servicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Servico salvo = servicoService.criar(novo);

        assertThat(salvo.getId()).isNull();
    }

    @Test
    void atualizarCopiaOsCampos() {
        UUID id = UUID.randomUUID();
        when(servicoDAO.findById(id)).thenReturn(Optional.of(servico(id)));
        when(servicoDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));
        Servico dados = servico(null);
        dados.setDescricao("Balanceamento");
        dados.setPreco(60.0);
        dados.setConfiguracao(Configuracao.SUV);

        Servico atualizado = servicoService.atualizar(id, dados);

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getDescricao()).isEqualTo("Balanceamento");
        assertThat(atualizado.getPreco()).isEqualTo(60.0);
        assertThat(atualizado.getConfiguracao()).isEqualTo(Configuracao.SUV);
    }

    @Test
    void buscarServicoInexistenteLancaEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(servicoDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.buscarPorId(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deletarRemoveOsProdutosVinculadosAntesDoServico() {
        UUID id = UUID.randomUUID();
        Servico servico = servico(id);
        when(servicoDAO.findById(id)).thenReturn(Optional.of(servico));

        servicoService.deletar(id);

        InOrder ordem = inOrder(itemDAO, servicoDAO);
        ordem.verify(itemDAO).deleteByServicoId(id);
        ordem.verify(servicoDAO).delete(servico);
    }

    @Test
    void adicionarProdutoVinculaServicoEProduto() {
        UUID servicoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        Servico servico = servico(servicoId);
        Produto produto = new Produto();
        produto.setId(produtoId);
        when(servicoDAO.findById(servicoId)).thenReturn(Optional.of(servico));
        when(itemDAO.existsByServicoIdAndProdutoId(servicoId, produtoId)).thenReturn(false);
        when(produtoService.buscarPorId(produtoId)).thenReturn(produto);
        when(itemDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        ItemProdutoServico item = servicoService.adicionarProduto(servicoId, produtoId, 4);

        assertThat(item.getServico()).isSameAs(servico);
        assertThat(item.getProduto()).isSameAs(produto);
        assertThat(item.getQuantidadeNecessaria()).isEqualTo(4);
    }

    @Test
    void adicionarProdutoRepetidoNoMesmoServicoEhRecusado() {
        UUID servicoId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        when(servicoDAO.findById(servicoId)).thenReturn(Optional.of(servico(servicoId)));
        when(itemDAO.existsByServicoIdAndProdutoId(servicoId, produtoId)).thenReturn(true);

        assertThatThrownBy(() -> servicoService.adicionarProduto(servicoId, produtoId, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está vinculado");
        verify(itemDAO, never()).save(any());
    }

    @Test
    void itemDeOutroServicoNaoPodeSerAlterado() {
        UUID servicoId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(itemDAO.findByIdAndServicoId(itemId, servicoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicoService.atualizarProduto(servicoId, itemId, 3))
                .isInstanceOf(EntityNotFoundException.class);
        verify(itemDAO, never()).save(any());
    }
}
