package br.com.oficina.config;

import br.com.oficina.dao.UsuarioDAO;
import br.com.oficina.modelo.Usuario;
import br.com.oficina.modelo.enuns.Cargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cria um Gestor inicial quando não existe nenhum usuário,
 * para ser possível entrar no sistema antes do UC09 (Gerenciar Funcionários).
 */
@Configuration
public class UsuarioInicialConfig {

    private static final Logger log = LoggerFactory.getLogger(UsuarioInicialConfig.class);

    @Bean
    public CommandLineRunner criarGestorInicial(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder,
            @Value("${oficina.gestor-inicial.login}") String login,
            @Value("${oficina.gestor-inicial.senha}") String senha) {
        return args -> {
            if (usuarioDAO.count() > 0) {
                return;
            }
            Usuario gestor = new Usuario();
            gestor.setNome("Gestor");
            gestor.setLogin(login);
            gestor.setSenha(passwordEncoder.encode(senha));
            gestor.setCargo(Cargo.GESTOR);
            usuarioDAO.save(gestor);
            log.info("Gestor inicial criado com login '{}'. Troque a senha assim que possível.", login);
        };
    }
}
