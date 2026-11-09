package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.modelo.Cliente;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteDAO clienteDAO;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente(UUID id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Maria Oliveira");
        cliente.setTelefone("(11) 98765-4321");
        cliente.setEmail("maria@email.com");
        return cliente;
    }

    @Test
    void criarIgnoraIdEnviadoPeloCliente() {
        when(clienteDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente salvo = clienteService.criar(cliente(UUID.randomUUID()));

        assertThat(salvo.getId()).isNull();
    }

    @Test
    void buscarInexistenteLancaEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(clienteDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void atualizarCopiaOsDadosMantendoOId() {
        UUID id = UUID.randomUUID();
        when(clienteDAO.findById(id)).thenReturn(Optional.of(cliente(id)));
        when(clienteDAO.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Cliente dados = new Cliente();
        dados.setId(UUID.randomUUID());
        dados.setNome("Maria Souza");
        dados.setTelefone("(11) 91111-2222");

        Cliente atualizado = clienteService.atualizar(id, dados);

        assertThat(atualizado.getId()).isEqualTo(id);
        assertThat(atualizado.getNome()).isEqualTo("Maria Souza");
        assertThat(atualizado.getTelefone()).isEqualTo("(11) 91111-2222");
        assertThat(atualizado.getEmail()).isNull();
    }

    @Test
    void deletarInexistenteNaoChamaDelete() {
        UUID id = UUID.randomUUID();
        when(clienteDAO.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.deletar(id))
                .isInstanceOf(EntityNotFoundException.class);
        verify(clienteDAO, never()).delete(any());
    }
}
