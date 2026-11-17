<template>
  <section class="card card-pad">
    <h3 class="card-title">{{ title }}</h3>
    <p v-if="!rows.length" class="muted">{{ emptyText }}</p>
    <ol v-else class="rank">
      <li v-for="row in rows" :key="row.label" :title="`${row.label}: ${format(row.value)}`">
        <div class="line">
          <span class="label">{{ row.label }}</span>
          <span class="value">{{ format(row.value) }}</span>
        </div>
        <!-- barra proporcional ao maior valor (serie unica, uma cor so) -->
        <div class="track"><div class="bar" :style="{ width: `${pct(row.value)}%` }" /></div>
      </li>
    </ol>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  rows: { type: Array, required: true },
  format: { type: Function, default: (v) => String(v) },
  emptyText: { type: String, default: 'Sem dados no período.' }
})

const max = computed(() => Math.max(...props.rows.map((r) => r.value), 0))
const pct = (value) => (max.value > 0 ? Math.max(2, (value / max.value) * 100) : 0)
</script>

<style scoped>
.rank {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 5px;
}
.label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.value {
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.track {
  height: 8px;
  border-radius: 4px;
  background: var(--neutral-soft);
}
.bar {
  height: 100%;
  border-radius: 4px;
  background: var(--accent-bright);
}
</style>
