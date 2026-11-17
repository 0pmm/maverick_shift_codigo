<template>
  <div class="toasts" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="t in ui.toasts" :key="t.id" class="toast" :class="t.type" role="status">
        <AppIcon :name="t.type === 'error' ? 'alert' : 'check'" />
        <span>{{ t.message }}</span>
        <button aria-label="Fechar aviso" @click="ui.dismiss(t.id)">
          <AppIcon name="x" :size="15" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useUiStore } from '@/stores/ui'
import AppIcon from './AppIcon.vue'

const ui = useUiStore()
</script>

<style scoped>
.toasts {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: min(400px, calc(100vw - 40px));
}
.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 12px 12px 14px;
  border-radius: 9px;
  background: #1b1f27;
  color: #fff;
  box-shadow: var(--shadow-lg);
  font-size: 13.5px;
}
.toast svg {
  flex-shrink: 0;
  margin-top: 1px;
}
.toast.success svg:first-child {
  color: #4ade80;
}
.toast.error svg:first-child {
  color: #fb7185;
}
.toast span {
  flex: 1;
}
.toast button {
  border: 0;
  background: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
}
.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
