package br.com.oficina.controller;

import br.com.oficina.modelo.Relatorio;
import br.com.oficina.modelo.enuns.TipoRelatorio;
import br.com.oficina.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "UC08 - Gerar Relatórios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Gera um relatorio do periodo (?tipo=GERAL&inicio=2026-11-01&fim=2026-11-30)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> gerar(@RequestParam TipoRelatorio tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Principal usuarioLogado) {
        // usuario logado vem do token do login (UC01)
        String login = usuarioLogado != null ? usuarioLogado.getName() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(relatorioService.gerar(tipo, inicio, fim, login));
    }

    @Operation(summary = "Historico dos relatorios gerados")
    @GetMapping
    public Page<Relatorio> listar(
            @PageableDefault(size = 10, sort = "dataGeracao", direction = Sort.Direction.DESC) Pageable pageable) {
        return relatorioService.listar(pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public Relatorio buscar(@PathVariable UUID id) {
        return relatorioService.buscarPorId(id);
    }
}
