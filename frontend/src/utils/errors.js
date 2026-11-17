// Converte o erro do axios em mensagem para o usuario.
// O backend responde {erro: "..."} em regras de negocio e {campo: "mensagem"} na validacao (@Valid).

const isSpringDefaultError = (body) => body && 'status' in body && 'path' in body

export function fieldErrors(error) {
  const body = error?.response?.data
  if (error?.response?.status !== 400 || !body || typeof body !== 'object') return {}
  if ('erro' in body || isSpringDefaultError(body)) return {}
  return { ...body }
}

export function errorMessage(error, fallback = 'Não foi possível concluir a operação.') {
  if (!error?.response) {
    return 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.'
  }
  const { status, data: body } = error.response

  if (body && typeof body === 'object' && typeof body.erro === 'string') return body.erro

  if (status === 400 && body && typeof body === 'object' && !isSpringDefaultError(body)) {
    const mensagens = Object.values(body).filter((v) => typeof v === 'string')
    if (mensagens.length) return mensagens.join(' • ')
  }
  if (status === 401) return 'Sua sessão expirou. Entre novamente.'
  if (status === 403) return 'Você não tem permissão para esta ação.'
  if (status === 404) return 'Registro não encontrado.'
  return fallback
}

// Exclusao bloqueada por chave estrangeira volta como 500 do banco
export function deleteErrorMessage(error, entidade = 'registro') {
  if (error?.response?.status === 500) {
    return `Não é possível excluir este ${entidade} porque ele está vinculado a outros cadastros.`
  }
  return errorMessage(error, `Não foi possível excluir o ${entidade}.`)
}
