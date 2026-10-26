package br.com.oficina.controller;

import br.com.oficina.modelo.Agendamento;
import br.com.oficina.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@Tag(name = "Agendamentos", description = "UC04 - Gerenciar Agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Operation(summary = "Lista paginada (?page=0&size=10&sort=dataHora,asc)")
    @GetMapping
    public Page<Agendamento> listar(@PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return agendamentoService.listar(pageable);
    }

    @Operation(summary = "Lista os agendamentos de um periodo (?inicio=2026-11-01&fim=2026-11-30)")
    @GetMapping("/periodo")
    public Page<Agendamento> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return agendamentoService.listarPorPeriodo(inicio, fim, pageable);
    }

    @Operation(summary = "Lista os agendamentos de um cliente")
    @GetMapping("/cliente/{clienteId}")
    public Page<Agendamento> listarPorCliente(@PathVariable UUID clienteId,
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return agendamentoService.listarPorCliente(clienteId, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Agendamento buscar(@PathVariable UUID id) {
        return agendamentoService.buscarPorId(id);
    }

    @Operation(summary = "Cria um agendamento (cliente e servico pelo id)")
    @PostMapping
    public ResponseEntity<Agendamento> criar(@Valid @RequestBody Agendamento agendamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criar(agendamento));
    }

    @Operation(summary = "Atualiza um agendamento")
    @PutMapping("/{id}")
    public Agendamento atualizar(@PathVariable UUID id, @Valid @RequestBody Agendamento agendamento) {
        return agendamentoService.atualizar(id, agendamento);
    }

    @Operation(summary = "Conclui um agendamento")
    @PatchMapping("/{id}/concluir")
    public Agendamento concluir(@PathVariable UUID id) {
        return agendamentoService.concluir(id);
    }

    @Operation(summary = "Cancela um agendamento")
    @PatchMapping("/{id}/cancelar")
    public Agendamento cancelar(@PathVariable UUID id) {
        return agendamentoService.cancelar(id);
    }
}
