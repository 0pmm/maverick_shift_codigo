package br.com.oficina.service;

import br.com.oficina.dao.ClienteDAO;
import br.com.oficina.modelo.Cliente;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listar(Pageable pageable) {
        return clienteDAO.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Cliente> buscarPorNome(String nome, Pageable pageable) {
        return clienteDAO.findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(UUID id) {
        return clienteDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: id=" + id));
    }

    public Cliente criar(Cliente cliente) {
        cliente.setId(null);
        return clienteDAO.save(cliente);
    }

    public Cliente atualizar(UUID id, Cliente dados) {
        Cliente existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setTelefone(dados.getTelefone());
        existente.setEmail(dados.getEmail());
        return clienteDAO.save(existente);
    }

    public void deletar(UUID id) {
        clienteDAO.delete(buscarPorId(id));
    }
}
