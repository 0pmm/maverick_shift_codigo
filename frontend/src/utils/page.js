// O Spring pode serializar o Page "direto" ({content, totalPages, ...})
// ou via DTO ({content, page: {totalPages, ...}}). Aqui os dois viram o mesmo formato.
export function normalizePage(body) {
  if (Array.isArray(body)) {
    return { items: body, page: 0, totalPages: 1, totalElements: body.length }
  }
  const meta = body?.page && typeof body.page === 'object' ? body.page : body ?? {}
  return {
    items: body?.content ?? [],
    page: meta.number ?? 0,
    totalPages: meta.totalPages ?? 0,
    totalElements: meta.totalElements ?? 0
  }
}

export const contentOf = (body) => normalizePage(body).items
