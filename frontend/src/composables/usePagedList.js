import { ref } from 'vue'
import { normalizePage } from '@/utils/page'
import { errorMessage } from '@/utils/errors'

/**
 * Estado de uma listagem paginada.
 * fetcher recebe { page, size } e devolve o Page do backend.
 */
export function usePagedList(fetcher, { size = 10 } = {}) {
  const items = ref([])
  const page = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const loading = ref(false)
  const error = ref('')

  let requestSeq = 0

  async function load(targetPage = page.value) {
    const seq = ++requestSeq
    loading.value = true
    error.value = ''
    try {
      const result = normalizePage(await fetcher({ page: targetPage, size }))
      if (seq !== requestSeq) return // resposta antiga, outra busca ja foi feita
      // se a pagina ficou vazia (ex.: apagou o ultimo item), volta uma
      if (result.items.length === 0 && targetPage > 0 && result.totalPages > 0) {
        return load(result.totalPages - 1)
      }
      items.value = result.items
      page.value = result.page
      totalPages.value = result.totalPages
      totalElements.value = result.totalElements
    } catch (e) {
      if (seq !== requestSeq) return
      items.value = []
      error.value = errorMessage(e, 'Não foi possível carregar a lista.')
    } finally {
      if (seq === requestSeq) loading.value = false
    }
  }

  const reload = () => load(page.value)
  const reset = () => load(0)

  return { items, page, totalPages, totalElements, loading, error, load, reload, reset }
}
