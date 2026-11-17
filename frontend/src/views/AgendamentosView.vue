<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Agendamentos</h1>
        <p>Horários marcados pelos clientes. Um agendamento vira ordem de serviço quando o carro chega.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()"><AppIcon name="plus" /> Novo agendamento</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="field inline">
          <label for="ag-inicio">De</label>
          <input id="ag-inicio" v-model="inicio" type="date" class="input" />
        </div>
        <div class="field inline">
          <label for="ag-fim">Até</label>
          <input id="ag-fim" v-model="fim" type="date" class="input" />
        </div>
        <button class="btn btn-sm" @click="setHoje">Hoje</button>
        <button class="btn btn-sm" @click="setMes">Este mês</button>
        <button v-if="inicio || fim" class="btn btn-sm btn-ghost" @click="limparFiltro">Limpar</button>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Data e hora</th>
              <th>Cliente</th>
              <th>Serviço</th>
              <th>Status</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="5"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum agendamento no período."
              icon="calendar"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="a in list.items.value" :key="a.id">
                <td>
                  <strong>{{ formatDate(a.dataHora) }}</strong>
                  <span class="cell-sub">{{ formatTime(a.dataHora) }}</span>
                </td>
                <td>
                  {{ a.cliente?.nome }}
                  <span class="cell-sub">{{ a.cliente?.telefone }}</span>
                </td>
                <td>
                  {{ a.servico?.descricao }}
                  <span class="cell-sub">{{ formatMoney(a.servico?.preco) }}</span>
                </td>
                <td>
                  <StatusBadge :status="a.status" :map="STATUS_AGENDAMENTO" />
                  <span v-if="comOrdem.has(a.id)" class="cell-sub">Possui ordem de serviço</span>
                </td>
                <td class="actions">
                  <template v-if="a.status === 'AGENDADO'">
                    <button v-if="!comOrdem.has(a.id)" class="btn btn-sm btn-primary" @click="abrirOrdemDe = a">
                      <AppIcon name="clipboard" :size="15" /> Abrir OS
                    </button>
                    <button v-if="!comOrdem.has(a.id)" class="btn btn-sm" @click="openForm(a)">
                      <AppIcon name="edit" :size="15" /> Editar
                    </button>
                    <button class="btn btn-sm" title="Concluir sem ordem de serviço" @click="mudarStatus(a, 'concluir')">
                      <AppIcon name="check" :size="15" />
                    </button>
                    <button class="btn btn-sm btn-danger" title="Cancelar" @click="mudarStatus(a, 'cancelar')">
                      <AppIcon name="x" :size="15" />
                    </button>
                  </template>
                  <span v-else class="muted">—</span>
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

    <AgendamentoFormModal :open="formOpen" :agendamento="editando" @close="formOpen = false" @saved="onSaved" />
    <AbrirOrdemModal :open="!!abrirOrdemDe" :agendamento="abrirOrdemDe" @close="abrirOrdemDe = null" @opened="onOrdemAberta" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { agendamentosApi, ordensApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { contentOf } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { firstDayOfMonth, formatDate, formatMoney, formatTime, lastDayOfMonth, toIsoDate } from '@/utils/format'
import { STATUS_AGENDAMENTO } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import TableState from '@/components/TableState.vue'
import AbrirOrdemModal from '@/components/AbrirOrdemModal.vue'
import AgendamentoFormModal from './agendamentos/AgendamentoFormModal.vue'

const ui = useUiStore()
const router = useRouter()

const inicio = ref('')
const fim = ref('')
const comOrdem = ref(new Set())
const formOpen = ref(false)
const editando = ref(null)
const abrirOrdemDe = ref(null)

const list = usePagedList((params) =>
  inicio.value && fim.value
    ? agendamentosApi.listarPorPeriodo(inicio.value, fim.value, { ...params, sort: 'dataHora,asc' })
    : agendamentosApi.listar({ ...params, sort: 'dataHora,desc' })
)

// agendamentos que ja viraram OS (para esconder "Abrir OS" e "Editar")
async function loadOrdens() {
  try {
    const ordens = contentOf(await ordensApi.todas())
    comOrdem.value = new Set(ordens.map((o) => o.agendamento?.id))
  } catch {
    comOrdem.value = new Set()
  }
}

const refresh = () => Promise.all([list.reload(), loadOrdens()])

watch([inicio, fim], ([i, f]) => {
  if ((i && f) || (!i && !f)) list.reset()
})
onMounted(() => Promise.all([list.load(), loadOrdens()]))

function setHoje() {
  inicio.value = toIsoDate()
  fim.value = toIsoDate()
}
function setMes() {
  inicio.value = firstDayOfMonth()
  fim.value = lastDayOfMonth()
}
function limparFiltro() {
  inicio.value = ''
  fim.value = ''
}

function openForm(agendamento = null) {
  editando.value = agendamento
  formOpen.value = true
}

function onSaved() {
  formOpen.value = false
  refresh()
}

function onOrdemAberta() {
  abrirOrdemDe.value = null
  router.push({ name: 'ordens' })
}

async function mudarStatus(agendamento, acao) {
  const concluir = acao === 'concluir'
  const ok = await ui.confirm({
    title: concluir ? 'Concluir agendamento' : 'Cancelar agendamento',
    message: concluir
      ? 'Marcar como concluído sem abrir ordem de serviço? Nenhum produto será baixado do estoque.'
      : `Cancelar o agendamento de ${agendamento.cliente?.nome}? O horário ficará livre.`,
    confirmText: concluir ? 'Concluir' : 'Cancelar agendamento',
    danger: !concluir
  })
  if (!ok) return
  try {
    await (concluir ? agendamentosApi.concluir(agendamento.id) : agendamentosApi.cancelar(agendamento.id))
    ui.success(concluir ? 'Agendamento concluído.' : 'Agendamento cancelado.')
    refresh()
  } catch (e) {
    ui.error(errorMessage(e))
  }
}
</script>
