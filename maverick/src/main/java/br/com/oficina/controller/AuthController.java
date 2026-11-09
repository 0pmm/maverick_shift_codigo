package br.com.oficina.controller;

import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import br.com.oficina.service.AutenticacaoService;
import br.com.oficina.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "UC01 - Realizar Login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AutenticacaoService autenticacaoService;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager,
            AutenticacaoService autenticacaoService, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.autenticacaoService = autenticacaoService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Realiza o login e devolve o token JWT")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.senha()));

        Usuario usuario = autenticacaoService.buscarPorLogin(request.login());
        TokenService.Token token = tokenService.gerar(usuario);
        return new LoginResponse(token.valor(), token.expiraEm(), usuario.getNome(), usuario.getCargo());
    }

    // Mensagem genérica: não revela se o erro foi no login ou na senha
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> credenciaisInvalidas() {
        return Map.of("erro", "Login ou senha inválidos");
    }

    public record LoginRequest(
            @NotBlank(message = "O login é obrigatório") String login,
            @NotBlank(message = "A senha é obrigatória") String senha) {
    }

    public record LoginResponse(String token, Instant expiraEm, String nome, Cargo cargo) {
    }
}
