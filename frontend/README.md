# Maverick Shift — Front-end (Vue 3)

Interface web do sistema da oficina, integrada à API Spring Boot da pasta `maverick/`.

**Stack:** Vue 3 (Composition API, `<script setup>`), Vue Router, Pinia, Axios e Vite.

## Como rodar

Pré-requisitos: Node.js 20+ e o backend rodando na porta 8080.

```bash
# 1) backend (em outro terminal, dentro de maverick/)
./mvnw spring-boot:run                                   # com MySQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo   # sem MySQL (H2 em memória)

# 2) front-end
cd frontend
npm install
npm run dev
```

Acesse http://localhost:5173. Login inicial do gestor: `admin` / `admin123`
(definido em `application.properties`, variáveis `GESTOR_LOGIN` e `GESTOR_SENHA`).

## Integração com o backend

- O Vite repassa toda chamada `/api/*` para `http://localhost:8080` (proxy em `vite.config.js`),
  então o navegador não faz requisição cross-origin e o backend não precisa de CORS.
- Para apontar para outro endereço de backend, crie `.env.local` com `BACKEND_URL=http://host:porta`.
- O login (`POST /api/auth/login`) devolve um JWT, que fica salvo no navegador e vai no
  cabeçalho `Authorization: Bearer ...` de todas as requisições (`src/api/http.js`).
  Se o token expirar (401), o usuário volta para a tela de login.
- As telas de **Relatórios** e **Funcionários** só aparecem para o Gestor, seguindo as
  mesmas regras do `SecurityConfig` do backend.

## Telas (casos de uso)

| Tela | Caso de uso | Endpoints |
|---|---|---|
| Login | UC01 | `/api/auth/login` |
| Produtos | UC02 | `/api/produtos` |
| Serviços (+ produtos do serviço) | UC03 | `/api/servicos`, `/api/servicos/{id}/produtos` |
| Agendamentos | UC04 | `/api/agendamentos` |
| Ordens de serviço | UC05 | `/api/ordens-servico` |
| Pagamentos | UC06 | `/api/pagamentos` |
| Clientes (+ histórico) | UC07 | `/api/clientes` |
| Relatórios | UC08 | `/api/relatorios` |
| Funcionários | UC09 | `/api/usuarios` |
| Início | — | painel do dia (agenda, OS em execução, estoque baixo) |

## Estrutura

```
src/
  api/          chamadas HTTP (axios + um módulo por recurso)
  stores/       Pinia: sessão do usuário (auth) e avisos/confirmações (ui)
  composables/  listagem paginada e busca com debounce
  components/   layout, modal, tabela, paginação, ícones...
  views/        uma tela por caso de uso (subpastas = modais da tela)
  utils/        formatação (moeda/data), textos dos enums, tratamento de erros
```

## Build de produção

```bash
npm run build     # gera dist/
npm run preview   # serve o dist/ em http://localhost:4173 (com o mesmo proxy)
```
