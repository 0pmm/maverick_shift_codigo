package br.com.oficina.controller;

import br.com.oficina.modelo.ItemServicoAgendamento;
import br.com.oficina.modelo.MovimentacaoEstoque;
import br.com.oficina.modelo.OrdemServico;
import br.com.oficina.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordens-servico")
@Tag(name = "Ordens de Serviço", description = "UC05 - Gerenciar Ordens de Serviço")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @Operation(summary = "Lista paginada, mais recentes primeiro")
    @GetMapping
    public Page<OrdemServico> listar(
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ordemServicoService.listar(pageable);
    }

    @Operation(summary = "Lista por status (ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA)")
    @GetMapping("/status/{status}")
    public Page<OrdemServico> listarPorStatus(@PathVariable String status,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ordemServicoService.listarPorStatus(status, pageable);
    }

    @Operation(summary = "Busca por ID")
    @GetMapping("/{id}")
    public OrdemServico buscar(@PathVariable UUID id) {
        return ordemServicoService.buscarPorId(id);
    }

    @Operation(summary = "Serviços executados na ordem (vindos do agendamento)")
    @GetMapping("/{id}/servicos")
    public List<ItemServicoAgendamento> listarServicos(@PathVariable UUID id) {
        return ordemServicoService.listarServicos(id);
    }

    @Operation(summary = "Baixas de estoque geradas pela ordem")
    @GetMapping("/{id}/movimentacoes")
    public List<MovimentacaoEstoque> listarMovimentacoes(@PathVariable UUID id) {
        return ordemServicoService.listarMovimentacoes(id);
    }

    @Operation(summary = "Abre uma OS a partir de um agendamento (sem funcionarioId, o responsável é o usuário logado)")
    @PostMapping
    public ResponseEntity<OrdemServico> abrir(@Valid @RequestBody AberturaRequest request,
            @AuthenticationPrincipal Jwt usuarioLogado) {
        OrdemServico ordem = ordemServicoService.abrir(request.agendamentoId(), request.funcionarioId(),
                usuarioLogado.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ordem);
    }

    @Operation(summary = "Troca o funcionário responsável")
    @PutMapping("/{id}")
    public OrdemServico trocarFuncionario(@PathVariable UUID id, @Valid @RequestBody FuncionarioRequest request) {
        return ordemServicoService.trocarFuncionario(id, request.funcionarioId());
    }

    @Operation(summary = "Inicia a execução (ABERTA -> EM_ANDAMENTO)")
    @PatchMapping("/{id}/iniciar")
    public OrdemServico iniciar(@PathVariable UUID id) {
        return ordemServicoService.iniciar(id);
    }

    @Operation(summary = "Conclui a OS e dá baixa no estoque dos produtos usados")
    @PatchMapping("/{id}/concluir")
    public OrdemServico concluir(@PathVariable UUID id) {
        return ordemServicoService.concluir(id);
    }

    @Operation(summary = "Cancela a OS e o agendamento")
    @PatchMapping("/{id}/cancelar")
    public OrdemServico cancelar(@PathVariable UUID id) {
        return ordemServicoService.cancelar(id);
    }

    public record AberturaRequest(
            @NotNull(message = "O agendamento é obrigatório") UUID agendamentoId,
            UUID funcionarioId) {
    }

    public record FuncionarioRequest(
            @NotNull(message = "O funcionário é obrigatório") UUID funcionarioId) {
    }
}
