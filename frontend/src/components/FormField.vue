<template>
  <div class="field" :class="{ full }">
    <label :for="id">{{ label }}<span v-if="required" class="req"> *</span></label>
    <slot :id="id" :invalid="!!error" />
    <span v-if="error" class="error-text">{{ error }}</span>
    <span v-else-if="hint" class="hint">{{ hint }}</span>
  </div>
</template>

<script setup>
import { useId } from 'vue'

defineProps({
  label: { type: String, required: true },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
  full: { type: Boolean, default: false }
})

const id = useId()
</script>

<style scoped>
.req {
  color: var(--danger);
}
</style>
