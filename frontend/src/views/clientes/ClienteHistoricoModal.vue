<template>
  <BaseModal :open="!!cliente" :title="cliente?.nome ?? ''" subtitle="Histórico do cliente" size="lg" @close="$emit('close')">
    <div v-if="loading" class="muted">Carregando...</div>
    <div v-else-if="erro" class="alert alert-error">{{ erro }}</div>
    <div v-else class="stack">
      <div class="totais">
        <div><span class="muted">Agendamentos</span><strong>{{ agendamentos.length }}</strong></div>
        <div><span class="muted">Pagamentos</span><strong>{{ pagamentos.length }}</strong></div>
        <div><span class="muted">Total pago</span><strong>{{ formatMoney(totalPago) }}</strong></div>
      </div>

      <section>
        <h3 class="card-title">Agendamentos</h3>
        <p v-if="!agendamentos.length" class="muted">Nenhum agendamento.</p>
        <table v-else class="table bordered">
          <thead><tr><th>Data</th><th>Serviço</th><th>Status</th></tr></thead>
          <tbody>
            <tr v-for="a in agendamentos" :key="a.id">
              <td>{{ formatDateTime(a.dataHora) }}</td>
              <td>{{ a.servico?.descricao }}</td>
              <td><StatusBadge :status="a.status" :map="STATUS_AGENDAMENTO" /></td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h3 class="card-title">Pagamentos</h3>
        <p v-if="!pagamentos.length" class="muted">Nenhum pagamento.</p>
        <table v-else class="table bordered">
          <thead><tr><th>Data</th><th>Descrição</th><th class="num">Valor</th></tr></thead>
          <tbody>
            <tr v-for="p in pagamentos" :key="p.id">
              <td>{{ formatDateTime(p.data) }}</td>
              <td>{{ p.descricao || '—' }}</td>
              <td class="num">{{ formatMoney(p.valor) }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </BaseModal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { agendamentosApi, pagamentosApi } from '@/api'
import { contentOf } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { formatDateTime, formatMoney } from '@/utils/format'
import { STATUS_AGENDAMENTO } from '@/utils/labels'
import BaseModal from '@/components/BaseModal.vue'
import StatusBadge from '@/components/StatusBadge.vue'

const props = defineProps({ cliente: { type: Object, default: null } })
defineEmits(['close'])

const HISTORICO = { page: 0, size: 100 }
const agendamentos = ref([])
const pagamentos = ref([])
const loading = ref(false)
const erro = ref('')

const totalPago = computed(() => pagamentos.value.reduce((soma, p) => soma + (p.valor ?? 0), 0))

watch(
  () => props.cliente,
  async (cliente) => {
    if (!cliente) return
    loading.value = true
    erro.value = ''
    try {
      const [ags, pags] = await Promise.all([
        agendamentosApi.listarPorCliente(cliente.id, { ...HISTORICO, sort: 'dataHora,desc' }),
        pagamentosApi.listarPorCliente(cliente.id, HISTORICO)
      ])
      agendamentos.value = contentOf(ags)
      pagamentos.value = contentOf(pags)
    } catch (e) {
      erro.value = errorMessage(e, 'Não foi possível carregar o histórico.')
    } finally {
      loading.value = false
    }
  }
)
</script>

<style scoped>
.totais {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.totais > div {
  display: flex;
  flex-direction: column;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--surface-muted);
}
.totais strong {
  font-size: 18px;
}
.bordered {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}
</style>
