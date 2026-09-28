# maverick-shift

Sistema de gerenciamento para estética automotiva (JSF + PrimeFaces + CDI/Weld + JPA/Hibernate + MySQL).

## Estrutura (MVC)

| Camada   | Pacote / pasta                          | Classes                     |
|----------|-----------------------------------------|-----------------------------|
| View     | `src/main/webapp/gerenciamento`         | `ProdutoCRUD.xhtml`         |
| View     | `br.com.estetica.controller`            | `ProdutoBean`               |
| Control  | `br.com.estetica.service`               | `ProdutoService`            |
| Model    | `br.com.estetica.dao`                   | `ProdutoDAO`                |
| Model    | `br.com.estetica.modelo`                | `Produto`, `Cliente`, `Usuario`, `Servico`, `ItemServico` (+ `ItemServicoId`), `Agendamento`, `OrdemServico`, `Pagamento`, `MovimentacaoEstoque`, `Relatorio` |
| Model    | `br.com.estetica.modelo.enums`          | `UnidadeMedida`, `Cargo`, `Configuracao`, `TipoMovimentacao`, `TipoRelatorio` |

> O pacote do diagrama se chama `enum`, mas `enum` é palavra reservada em Java, por isso o pacote é `enums`.

## Como executar

1. MySQL rodando em `localhost:3306` (usuário/senha em `src/main/resources/META-INF/persistence.xml`).
   O banco `produtos` e as tabelas de todas as entidades (`produto`, `cliente`, `usuario`, `servico`,
   `item_servico`, `agendamento`, `ordem_servico`, `pagamento`, `movimentacao_estoque`, `relatorio`)
   são criados automaticamente.
2. `mvn clean package`
3. Copiar `target/maverick-shift.war` para a pasta `webapps` do Tomcat 9.
4. Acessar `http://localhost:8080/maverick-shift/`
