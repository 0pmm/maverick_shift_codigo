<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="overlay" :style="{ zIndex }" @mousedown.self="close">
        <div class="modal" :class="`modal-${size}`" role="dialog" aria-modal="true" :aria-label="title">
          <header class="modal-header">
            <div>
              <h2>{{ title }}</h2>
              <p v-if="subtitle">{{ subtitle }}</p>
            </div>
            <button class="btn btn-ghost btn-sm" aria-label="Fechar" @click="close">
              <AppIcon name="x" />
            </button>
          </header>
          <div class="modal-body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
// compartilhado entre todas as instancias
const openModals = []
const BASE_Z = 50
</script>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import AppIcon from './AppIcon.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  size: { type: String, default: 'md' } // sm | md | lg
})
const emit = defineEmits(['close'])

const close = () => emit('close')

// Um modal pode abrir outro (ex.: confirmacao sobre os detalhes da OS).
// O ultimo aberto fica por cima e so ele fecha com ESC.
const handle = { close }
const zIndex = ref(BASE_Z)
const onKey = (e) => {
  if (e.key === 'Escape' && openModals.at(-1) === handle) close()
}

function register() {
  openModals.push(handle)
  zIndex.value = BASE_Z + openModals.length
  document.addEventListener('keydown', onKey)
}
function unregister() {
  const i = openModals.indexOf(handle)
  if (i >= 0) openModals.splice(i, 1)
  document.removeEventListener('keydown', onKey)
}

watch(
  () => props.open,
  (open) => (open ? register() : unregister()),
  { immediate: true }
)
onBeforeUnmount(unregister)
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 7vh 16px 16px;
  background: rgba(15, 18, 24, 0.5);
  overflow-y: auto;
}
.modal {
  width: 100%;
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
}
.modal-sm {
  max-width: 420px;
}
.modal-md {
  max-width: 580px;
}
.modal-lg {
  max-width: 860px;
}
.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--border);
}
.modal-header h2 {
  font-size: 17px;
  font-weight: 650;
}
.modal-header p {
  margin: 3px 0 0;
  color: var(--text-muted);
  font-size: 13px;
}
.modal-body {
  padding: 20px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--border);
  background: var(--surface-muted);
  border-radius: 0 0 12px 12px;
}
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.15s ease;
}
.modal-enter-active .modal,
.modal-leave-active .modal {
  transition: transform 0.15s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal,
.modal-leave-to .modal {
  transform: translateY(8px) scale(0.99);
}
</style>
