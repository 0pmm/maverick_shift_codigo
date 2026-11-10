package br.com.oficina;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// sobe a aplicacao inteira usando o H2 do perfil "test"
@SpringBootTest
@ActiveProfiles("test")
class OficinaApplicationTests {

	@Test
	void contextLoads() {
	}

}
