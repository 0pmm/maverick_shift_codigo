<template>
  <div v-if="totalElements > 0" class="pagination">
    <span class="muted">
      {{ totalElements }} {{ totalElements === 1 ? 'registro' : 'registros' }}
    </span>
    <div v-if="totalPages > 1" class="row">
      <button class="btn btn-sm" :disabled="page === 0" aria-label="Página anterior" @click="$emit('change', page - 1)">
        <AppIcon name="chevronLeft" :size="16" />
      </button>
      <span class="muted">Página {{ page + 1 }} de {{ totalPages }}</span>
      <button
        class="btn btn-sm"
        :disabled="page >= totalPages - 1"
        aria-label="Próxima página"
        @click="$emit('change', page + 1)"
      >
        <AppIcon name="chevronRight" :size="16" />
      </button>
    </div>
  </div>
</template>

<script setup>
import AppIcon from './AppIcon.vue'

defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, required: true }
})
defineEmits(['change'])
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  font-size: 13px;
}
</style>
