<template>
  <tr v-if="loading || error || empty" class="state-row">
    <td :colspan="colspan">
      <div v-if="loading" class="state">
        <span class="spinner" />
        Carregando...
      </div>
      <div v-else-if="error" class="state error">
        <AppIcon name="alert" />
        {{ error }}
        <button class="btn btn-sm" @click="$emit('retry')">Tentar de novo</button>
      </div>
      <div v-else class="state">
        <AppIcon :name="icon" :size="22" />
        <span>{{ emptyText }}</span>
      </div>
    </td>
  </tr>
</template>

<script setup>
import AppIcon from './AppIcon.vue'

defineProps({
  colspan: { type: Number, required: true },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  empty: { type: Boolean, default: false },
  emptyText: { type: String, default: 'Nenhum registro encontrado.' },
  icon: { type: String, default: 'file' }
})
defineEmits(['retry'])
</script>

<style scoped>
.state-row:hover {
  background: none !important;
}
.state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 36px 16px;
  color: var(--text-muted);
}
.state.error {
  color: var(--danger);
}
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-strong);
  border-top-color: var(--accent-bright);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
