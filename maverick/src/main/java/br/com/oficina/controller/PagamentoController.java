package br.com.oficina.controller;

import br.com.oficina.modelo.Pagamento;
import br.com.oficina.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "UC06 - Gerenciar Gastos e Pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Operation(summary = "Lista paginada, mais recentes primeiro (?page=0&size=10&sort=data,desc)")
    @GetMapping
    public Page<Pagamento> listar(
            @PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagamentoService.listar(pageable);
    }

    @Operation(summary = "Lista os pagamentos de um periodo (?inicio=2026-11-01&fim=2026-11-30)")
    @GetMapping("/periodo")
    public Page<Pagamento> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagamentoService.listarPorPeriodo(inicio, fim, pageable);
    }

    @Operation(summary = "Lista os pagamentos de um cliente")
    @GetMapping("/cliente/{clienteId}")
    public Page<Pagamento> listarPorCliente(@PathVariable UUID clienteId,
            @PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagamentoService.listarPorCliente(clienteId, pageable);
    }

    @Operation(summary = "Lista os pagamentos de uma ordem de servico")
    @GetMapping("/ordem-servico/{ordemServicoId}")
    public List<Pagamento> listarPorOrdemServico(@PathVariable UUID ordemServicoId) {
        return pagamentoService.listarPorOrdemServico(ordemServicoId);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Pagamento buscar(@PathVariable UUID id) {
        return pagamentoService.buscarPorId(id);
    }

    @Operation(summary = "Registra um pagamento (cliente e ordem de servico pelo id)")
    @PostMapping
    public ResponseEntity<Pagamento> criar(@Valid @RequestBody Pagamento pagamento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoService.criar(pagamento));
    }

    @Operation(summary = "Atualiza um pagamento")
    @PutMapping("/{id}")
    public Pagamento atualizar(@PathVariable UUID id, @Valid @RequestBody Pagamento pagamento) {
        return pagamentoService.atualizar(id, pagamento);
    }

    @Operation(summary = "Remove um pagamento")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        pagamentoService.deletar(id);
    }
}
