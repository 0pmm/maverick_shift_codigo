<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Ordens de serviço</h1>
        <p>Aberta → Em andamento → Concluída. Ao concluir, os produtos do serviço saem do estoque.</p>
      </div>
      <button class="btn btn-primary" @click="abrirOpen = true"><AppIcon name="plus" /> Abrir OS</button>
    </div>

    <div class="card">
      <div class="toolbar tabs" role="tablist">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab"
          :class="{ active: status === tab.value }"
          role="tab"
          :aria-selected="status === tab.value"
          @click="status = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>OS</th>
              <th>Cliente / serviço</th>
              <th>Responsável</th>
              <th class="num">Valor</th>
              <th>Status</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="6"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhuma ordem de serviço."
              icon="clipboard"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="o in list.items.value" :key="o.id">
                <td>
                  <span class="mono">{{ shortId(o.id) }}</span>
                  <span class="cell-sub">{{ formatDateTime(o.dataAbertura) }}</span>
                </td>
                <td>
                  <strong>{{ o.agendamento?.cliente?.nome }}</strong>
                  <span class="cell-sub">{{ o.agendamento?.servico?.descricao }}</span>
                </td>
                <td>{{ o.funcionario?.nome }}</td>
                <td class="num">{{ formatMoney(o.valorTotal) }}</td>
                <td><StatusBadge :status="o.status" :map="STATUS_ORDEM" /></td>
                <td class="actions">
                  <button v-if="o.status === 'ABERTA'" class="btn btn-sm btn-primary" @click="acao(o, 'iniciar')">
                    <AppIcon name="play" :size="14" /> Iniciar
                  </button>
                  <button v-if="o.status === 'EM_ANDAMENTO'" class="btn btn-sm btn-primary" @click="acao(o, 'concluir')">
                    <AppIcon name="check" :size="15" /> Concluir
                  </button>
                  <button class="btn btn-sm" @click="detalheId = o.id"><AppIcon name="eye" :size="15" /> Detalhes</button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="list.page.value"
        :total-pages="list.totalPages.value"
        :total-elements="list.totalElements.value"
        @change="list.load"
      />
    </div>

    <AbrirOrdemModal :open="abrirOpen" @close="abrirOpen = false" @opened="onAberta" />
    <OrdemDetalheModal :ordem-id="detalheId" :version="detalheVersion" @close="detalheId = null" @changed="list.reload" @acao="acao" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ordensApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { errorMessage } from '@/utils/errors'
import { formatDateTime, formatMoney, shortId } from '@/utils/format'
import { STATUS_ORDEM } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import TableState from '@/components/TableState.vue'
import AbrirOrdemModal from '@/components/AbrirOrdemModal.vue'
import OrdemDetalheModal from './ordens/OrdemDetalheModal.vue'

const tabs = [
  { value: '', label: 'Todas' },
  ...Object.entries(STATUS_ORDEM).map(([value, info]) => ({ value, label: info.label }))
]

const ui = useUiStore()
const status = ref('')
const abrirOpen = ref(false)
const detalheId = ref(null)
const detalheVersion = ref(0) // muda para o modal de detalhes recarregar

const list = usePagedList((params) =>
  status.value ? ordensApi.listarPorStatus(status.value, params) : ordensApi.listar(params)
)
watch(status, () => list.reset())
onMounted(() => list.load())

function onAberta(ordem) {
  abrirOpen.value = false
  status.value === '' ? list.reset() : (status.value = '')
  detalheId.value = ordem.id
}

const ACOES = {
  iniciar: {
    title: 'Iniciar execução',
    message: 'A ordem passa para "Em andamento".',
    confirmText: 'Iniciar',
    done: 'Execução iniciada.'
  },
  concluir: {
    title: 'Concluir ordem de serviço',
    message: 'Os produtos usados pelo serviço serão baixados do estoque e o agendamento será concluído.',
    confirmText: 'Concluir',
    done: 'Ordem concluída e estoque atualizado.'
  },
  cancelar: {
    title: 'Cancelar ordem de serviço',
    message: 'A ordem e o agendamento serão cancelados. O estoque não é alterado.',
    confirmText: 'Cancelar OS',
    danger: true,
    done: 'Ordem cancelada.'
  }
}

async function acao(ordem, tipo) {
  const config = ACOES[tipo]
  const ok = await ui.confirm({ ...config, message: `${shortId(ordem.id)} — ${config.message}` })
  if (!ok) return
  try {
    await ordensApi[tipo](ordem.id)
    ui.success(config.done)
    list.reload()
    detalheVersion.value++
  } catch (e) {
    ui.error(errorMessage(e))
  }
}
</script>

<style scoped>
.tabs {
  gap: 4px;
  padding: 0 12px;
  overflow-x: auto;
  overflow-y: hidden;
  flex-wrap: nowrap;
}
.tab {
  position: relative;
  padding: 13px 10px;
  border: 0;
  background: none;
  color: var(--text-muted);
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.tab:hover {
  color: var(--text);
}
.tab.active {
  color: var(--text);
}
.tab.active::after {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  bottom: 0;
  height: 2px;
  background: var(--accent-bright);
}
</style>
