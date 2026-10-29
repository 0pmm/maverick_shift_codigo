package br.com.oficina.service;

import br.com.oficina.modelo.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoHoras;

    public TokenService(JwtEncoder jwtEncoder,
            @Value("${oficina.jwt.expiracao-horas}") long expiracaoHoras) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoHoras = expiracaoHoras;
    }

    public Token gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracaoHoras, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("oficina")
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getLogin())
                .claim("nome", usuario.getNome())
                .claim("cargo", usuario.getCargo().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String valor = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(valor, expiraEm);
    }

    public record Token(String valor, Instant expiraEm) {
    }
}
