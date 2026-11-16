<template>
  <BaseModal :open="!!state" :title="state?.title" size="sm" @close="ui.answerConfirm(false)">
    <p class="message">{{ state?.message }}</p>
    <template #footer>
      <button class="btn" @click="ui.answerConfirm(false)">Voltar</button>
      <button
        class="btn"
        :class="state?.danger ? 'btn-danger-solid' : 'btn-primary'"
        @click="ui.answerConfirm(true)"
      >
        {{ state?.confirmText }}
      </button>
    </template>
  </BaseModal>
</template>

<script setup>
import { computed } from 'vue'
import { useUiStore } from '@/stores/ui'
import BaseModal from './BaseModal.vue'

const ui = useUiStore()
const state = computed(() => ui.confirmState)
</script>

<style scoped>
.message {
  margin: 0;
  color: #344054;
}
</style>
