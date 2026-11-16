import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOAST_MS = 4000
let nextId = 1

// Avisos (toasts) e dialogo de confirmacao usados pelas telas
export const useUiStore = defineStore('ui', () => {
  const toasts = ref([])
  const confirmState = ref(null)

  function notify(message, type = 'success') {
    const id = nextId++
    toasts.value = [...toasts.value, { id, message, type }]
    setTimeout(() => dismiss(id), TOAST_MS)
  }

  function dismiss(id) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  const success = (message) => notify(message, 'success')
  const error = (message) => notify(message, 'error')

  // uso: if (await ui.confirm({ title, message })) { ... }
  function confirm(options) {
    return new Promise((resolve) => {
      confirmState.value = {
        title: 'Confirmar',
        confirmText: 'Confirmar',
        danger: false,
        ...options,
        resolve
      }
    })
  }

  function answerConfirm(answer) {
    confirmState.value?.resolve(answer)
    confirmState.value = null
  }

  return { toasts, confirmState, notify, dismiss, success, error, confirm, answerConfirm }
})
