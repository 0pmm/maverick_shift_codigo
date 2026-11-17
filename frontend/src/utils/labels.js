// Textos exibidos para os enums do backend

export const CONFIGURACOES = {
  HATCH: 'Hatch',
  SUV: 'SUV',
  SEDAN: 'Sedan',
  PICKUP: 'Picape',
  MINIVAN: 'Minivan',
  PERUA: 'Perua',
  COUPE: 'Cupê',
  CONVERSIVEL: 'Conversível'
}

export const UNIDADES = {
  MILILITRO: 'Mililitro (ml)',
  LITRO: 'Litro (L)',
  GALAO: 'Galão',
  GRAMA: 'Grama (g)',
  QUILOGRAMA: 'Quilograma (kg)',
  UNIDADE: 'Unidade (un)'
}

export const UNIDADES_CURTAS = {
  MILILITRO: 'ml',
  LITRO: 'L',
  GALAO: 'gl',
  GRAMA: 'g',
  QUILOGRAMA: 'kg',
  UNIDADE: 'un'
}

export const CARGOS = {
  FUNCIONARIO: 'Funcionário',
  GESTOR: 'Gestor'
}

export const TIPOS_RELATORIO = {
  GERAL: 'Geral',
  FINANCEIRO: 'Financeiro',
  SERVICOS: 'Serviços',
  PRODUTOS: 'Produtos',
  CLIENTES: 'Clientes'
}

// tone = cor do StatusBadge
export const STATUS_AGENDAMENTO = {
  AGENDADO: { label: 'Agendado', tone: 'info' },
  CONCLUIDO: { label: 'Concluído', tone: 'success' },
  CANCELADO: { label: 'Cancelado', tone: 'neutral' }
}

export const STATUS_ORDEM = {
  ABERTA: { label: 'Aberta', tone: 'info' },
  EM_ANDAMENTO: { label: 'Em andamento', tone: 'warning' },
  CONCLUIDA: { label: 'Concluída', tone: 'success' },
  CANCELADA: { label: 'Cancelada', tone: 'neutral' }
}

export const toOptions = (map) => Object.entries(map).map(([value, label]) => ({ value, label }))
