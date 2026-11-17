import { onBeforeUnmount, watch } from 'vue'

const DEBOUNCE_MS = 350

// Executa callback so depois que o usuario para de digitar
export function useDebouncedWatch(source, callback, delay = DEBOUNCE_MS) {
  let timer = null
  watch(source, () => {
    clearTimeout(timer)
    timer = setTimeout(callback, delay)
  })
  onBeforeUnmount(() => clearTimeout(timer))
}
