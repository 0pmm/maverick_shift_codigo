package br.com.oficina.controller;

import br.com.oficina.modelo.Usuario;
import br.com.oficina.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(name = "Funcionários", description = "UC09 - Gerenciar Funcionários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Lista paginada (?page=0&size=10&sort=nome,asc)")
    @GetMapping
    public Page<Usuario> listar(@PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @Operation(summary = "Busca funcionários pelo nome")
    @GetMapping("/busca")
    public Page<Usuario> buscarPorNome(@RequestParam String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return usuarioService.buscarPorNome(nome, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @Operation(summary = "Cadastra um funcionário")
    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(usuario));
    }

    @Operation(summary = "Atualiza um funcionário (senha em branco mantém a atual)")
    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable UUID id, @RequestBody Usuario usuario) {
        return usuarioService.atualizar(id, usuario);
    }

    @Operation(summary = "Remove um funcionário")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        usuarioService.deletar(id);
    }
}
