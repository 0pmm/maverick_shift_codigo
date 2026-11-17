<template>
  <span class="badge" :class="`tone-${info.tone}`">{{ info.label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: '' },
  map: { type: Object, required: true }
})

const info = computed(() => props.map[props.status] ?? { label: props.status || '—', tone: 'neutral' })
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 9px 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
.badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.tone-info {
  background: var(--info-soft);
  color: var(--info);
}
.tone-success {
  background: var(--success-soft);
  color: var(--success);
}
.tone-warning {
  background: var(--warning-soft);
  color: var(--warning);
}
.tone-danger {
  background: var(--danger-soft);
  color: var(--danger);
}
.tone-neutral {
  background: var(--neutral-soft);
  color: #4b5563;
}
</style>
