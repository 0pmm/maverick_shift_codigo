import http from './http'

const data = (promise) => promise.then((res) => res.data)

// Lista grande para preencher selects (cliente, servico, etc.)
const ALL = { page: 0, size: 1000 }

// Operacoes basicas de CRUD iguais em varios controllers
function crud(path) {
  return {
    listar: (params) => data(http.get(path, { params })),
    buscar: (id) => data(http.get(`${path}/${id}`)),
    criar: (body) => data(http.post(path, body)),
    atualizar: (id, body) => data(http.put(`${path}/${id}`, body)),
    excluir: (id) => data(http.delete(`${path}/${id}`)),
    todos: (sort) => data(http.get(path, { params: { ...ALL, sort } }))
  }
}

export const authApi = {
  login: (login, senha) => data(http.post('/auth/login', { login, senha }))
}

export const clientesApi = {
  ...crud('/clientes'),
  buscarPorNome: (nome, params) => data(http.get('/clientes/busca', { params: { ...params, nome } }))
}

export const produtosApi = {
  ...crud('/produtos'),
  buscarPorNome: (nome, params) => data(http.get('/produtos/busca', { params: { ...params, nome } }))
}

export const servicosApi = {
  ...crud('/servicos'),
  buscarPorDescricao: (descricao, params) =>
    data(http.get('/servicos/busca', { params: { ...params, descricao } })),
  listarProdutos: (id) => data(http.get(`/servicos/${id}/produtos`)),
  adicionarProduto: (id, produtoId, quantidadeNecessaria) =>
    data(http.post(`/servicos/${id}/produtos`, { produtoId, quantidadeNecessaria })),
  atualizarProduto: (id, itemId, quantidadeNecessaria) =>
    data(http.put(`/servicos/${id}/produtos/${itemId}`, { quantidadeNecessaria })),
  removerProduto: (id, itemId) => data(http.delete(`/servicos/${id}/produtos/${itemId}`))
}

export const agendamentosApi = {
  ...crud('/agendamentos'),
  listarPorPeriodo: (inicio, fim, params) =>
    data(http.get('/agendamentos/periodo', { params: { ...params, inicio, fim } })),
  listarPorCliente: (clienteId, params) => data(http.get(`/agendamentos/cliente/${clienteId}`, { params })),
  concluir: (id) => data(http.patch(`/agendamentos/${id}/concluir`)),
  cancelar: (id) => data(http.patch(`/agendamentos/${id}/cancelar`))
}

export const ordensApi = {
  listar: (params) => data(http.get('/ordens-servico', { params })),
  listarPorStatus: (status, params) => data(http.get(`/ordens-servico/status/${status}`, { params })),
  todas: () => data(http.get('/ordens-servico', { params: ALL })),
  buscar: (id) => data(http.get(`/ordens-servico/${id}`)),
  movimentacoes: (id) => data(http.get(`/ordens-servico/${id}/movimentacoes`)),
  abrir: (agendamentoId, funcionarioId) =>
    data(http.post('/ordens-servico', { agendamentoId, funcionarioId: funcionarioId || null })),
  trocarFuncionario: (id, funcionarioId) => data(http.put(`/ordens-servico/${id}`, { funcionarioId })),
  iniciar: (id) => data(http.patch(`/ordens-servico/${id}/iniciar`)),
  concluir: (id) => data(http.patch(`/ordens-servico/${id}/concluir`)),
  cancelar: (id) => data(http.patch(`/ordens-servico/${id}/cancelar`))
}

export const pagamentosApi = {
  ...crud('/pagamentos'),
  listarPorPeriodo: (inicio, fim, params) =>
    data(http.get('/pagamentos/periodo', { params: { ...params, inicio, fim } })),
  listarPorCliente: (clienteId, params) => data(http.get(`/pagamentos/cliente/${clienteId}`, { params })),
  listarPorOrdem: (ordemId) => data(http.get(`/pagamentos/ordem-servico/${ordemId}`))
}

export const usuariosApi = {
  ...crud('/usuarios'),
  buscarPorNome: (nome, params) => data(http.get('/usuarios/busca', { params: { ...params, nome } }))
}

export const relatoriosApi = {
  gerar: (tipo, inicio, fim) => data(http.post('/relatorios', null, { params: { tipo, inicio, fim } })),
  listar: (params) => data(http.get('/relatorios', { params }))
}
