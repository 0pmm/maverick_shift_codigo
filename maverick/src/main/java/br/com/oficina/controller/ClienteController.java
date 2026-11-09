package br.com.oficina.controller;

import br.com.oficina.modelo.Cliente;
import br.com.oficina.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "UC07 - Gerenciar Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Lista paginada (?page=0&size=10&sort=nome,asc)")
    @GetMapping
    public Page<Cliente> listar(@PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return clienteService.listar(pageable);
    }

    @Operation(summary = "Busca clientes pelo nome")
    @GetMapping("/busca")
    public Page<Cliente> buscarPorNome(@RequestParam String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return clienteService.buscarPorNome(nome, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable UUID id) {
        return clienteService.buscarPorId(id);
    }

    @Operation(summary = "Cadastra um cliente")
    @PostMapping
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(cliente));
    }

    @Operation(summary = "Atualiza um cliente")
    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable UUID id, @Valid @RequestBody Cliente cliente) {
        return clienteService.atualizar(id, cliente);
    }

    @Operation(summary = "Remove um cliente")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        clienteService.deletar(id);
    }
}
