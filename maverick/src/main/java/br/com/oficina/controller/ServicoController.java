package br.com.oficina.controller;

import br.com.oficina.modelo.ItemProdutoServico;
import br.com.oficina.modelo.Servico;
import br.com.oficina.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servicos")
@Tag(name = "Serviços", description = "UC03 - Gerenciar Serviços")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @Operation(summary = "Lista paginada (?page=0&size=10&sort=descricao,asc)")
    @GetMapping
    public Page<Servico> listar(@PageableDefault(size = 10, sort = "descricao") Pageable pageable) {
        return servicoService.listar(pageable);
    }

    @Operation(summary = "Busca serviços pela descrição")
    @GetMapping("/busca")
    public Page<Servico> buscarPorDescricao(@RequestParam String descricao,
            @PageableDefault(size = 10) Pageable pageable) {
        return servicoService.buscarPorDescricao(descricao, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Servico buscar(@PathVariable UUID id) {
        return servicoService.buscarPorId(id);
    }

    @Operation(summary = "Cria um serviço")
    @PostMapping
    public ResponseEntity<Servico> criar(@Valid @RequestBody Servico servico) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.criar(servico));
    }

    @Operation(summary = "Atualiza um serviço")
    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable UUID id, @Valid @RequestBody Servico servico) {
        return servicoService.atualizar(id, servico);
    }

    @Operation(summary = "Remove um serviço e os produtos vinculados a ele")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        servicoService.deletar(id);
    }

    // ---- Produtos necessários para executar o serviço ----

    @Operation(summary = "Lista os produtos necessários para o serviço")
    @GetMapping("/{id}/produtos")
    public List<ItemProdutoServico> listarProdutos(@PathVariable UUID id) {
        return servicoService.listarProdutos(id);
    }

    @Operation(summary = "Vincula um produto ao serviço")
    @PostMapping("/{id}/produtos")
    public ResponseEntity<ItemProdutoServico> adicionarProduto(@PathVariable UUID id,
            @Valid @RequestBody ItemProdutoRequest request) {
        ItemProdutoServico item = servicoService.adicionarProduto(id, request.produtoId(),
                request.quantidadeNecessaria());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @Operation(summary = "Altera a quantidade necessária de um produto vinculado")
    @PutMapping("/{id}/produtos/{itemId}")
    public ItemProdutoServico atualizarProduto(@PathVariable UUID id, @PathVariable UUID itemId,
            @Valid @RequestBody QuantidadeRequest request) {
        return servicoService.atualizarProduto(id, itemId, request.quantidadeNecessaria());
    }

    @Operation(summary = "Desvincula um produto do serviço")
    @DeleteMapping("/{id}/produtos/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerProduto(@PathVariable UUID id, @PathVariable UUID itemId) {
        servicoService.removerProduto(id, itemId);
    }

    public record ItemProdutoRequest(
            @NotNull(message = "O produto é obrigatório") UUID produtoId,
            @NotNull(message = "A quantidade necessária é obrigatória")
            @Positive(message = "A quantidade necessária deve ser maior que zero") Integer quantidadeNecessaria) {
    }

    public record QuantidadeRequest(
            @NotNull(message = "A quantidade necessária é obrigatória")
            @Positive(message = "A quantidade necessária deve ser maior que zero") Integer quantidadeNecessaria) {
    }
}
