package br.com.oficina.controller;

import br.com.oficina.modelo.Produto;
import br.com.oficina.service.ProdutoService;
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
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "CRUD de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(summary = "Lista paginada (?page=0&size=10&sort=nome,asc)")
    @GetMapping
    public Page<Produto> listar(@PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return produtoService.listar(pageable);
    }

    @Operation(summary = "Busca produtos pelo nome")
    @GetMapping("/busca")
    public Page<Produto> buscarPorNome(@RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        return produtoService.buscarPorNome(nome, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Produto buscar(@PathVariable UUID id) {
        return produtoService.buscarPorId(id);
    }

    @Operation(summary = "Cria um produto")
    @PostMapping
    public ResponseEntity<Produto> criar(@Valid @RequestBody Produto produto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(produto));
    }

    @Operation(summary = "Atualiza um produto")
    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable UUID id, @Valid @RequestBody Produto produto) {
        return produtoService.atualizar(id, produto);
    }

    @Operation(summary = "Remove um produto")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        produtoService.deletar(id);
    }
}