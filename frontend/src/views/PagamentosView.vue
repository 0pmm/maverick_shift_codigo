<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Pagamentos</h1>
        <p>Recebimentos dos clientes, avulsos ou vinculados a uma ordem de serviço.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()"><AppIcon name="plus" /> Registrar pagamento</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="field inline">
          <label for="pg-inicio">De</label>
          <input id="pg-inicio" v-model="inicio" type="date" class="input" />
        </div>
        <div class="field inline">
          <label for="pg-fim">Até</label>
          <input id="pg-fim" v-model="fim" type="date" class="input" />
        </div>
        <button class="btn btn-sm" @click="setMes">Este mês</button>
        <button v-if="inicio || fim" class="btn btn-sm btn-ghost" @click="inicio = fim = ''">Limpar</button>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Data</th>
              <th>Cliente</th>
              <th>Descrição</th>
              <th>OS</th>
              <th class="num">Valor</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="6"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum pagamento encontrado."
              icon="card"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="p in list.items.value" :key="p.id">
                <td>{{ formatDateTime(p.data) }}</td>
                <td><strong>{{ p.cliente?.nome }}</strong></td>
                <td>{{ p.descricao || '—' }}</td>
                <td>
                  <span v-if="p.ordemServico" class="mono">{{ shortId(p.ordemServico.id) }}</span>
                  <span v-else class="muted">Avulso</span>
                </td>
                <td class="num"><strong>{{ formatMoney(p.valor) }}</strong></td>
                <td class="actions">
                  <button class="btn btn-sm" @click="openForm(p)"><AppIcon name="edit" :size="15" /> Editar</button>
                  <button class="btn btn-sm btn-danger" @click="remove(p)"><AppIcon name="trash" :size="15" /></button>
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

    <PagamentoFormModal :open="formOpen" :pagamento="editando" @close="formOpen = false" @saved="onSaved" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { pagamentosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { deleteErrorMessage } from '@/utils/errors'
import { firstDayOfMonth, formatDateTime, formatMoney, lastDayOfMonth, shortId } from '@/utils/format'
import AppIcon from '@/components/AppIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'
import PagamentoFormModal from './pagamentos/PagamentoFormModal.vue'

const ui = useUiStore()
const inicio = ref('')
const fim = ref('')
const formOpen = ref(false)
const editando = ref(null)

const list = usePagedList((params) =>
  inicio.value && fim.value
    ? pagamentosApi.listarPorPeriodo(inicio.value, fim.value, params)
    : pagamentosApi.listar(params)
)
watch([inicio, fim], ([i, f]) => {
  if ((i && f) || (!i && !f)) list.reset()
})
onMounted(() => list.load())

function setMes() {
  inicio.value = firstDayOfMonth()
  fim.value = lastDayOfMonth()
}

function openForm(pagamento = null) {
  editando.value = pagamento
  formOpen.value = true
}

function onSaved() {
  formOpen.value = false
  list.reload()
}

async function remove(pagamento) {
  const ok = await ui.confirm({
    title: 'Excluir pagamento',
    message: `Excluir o pagamento de ${formatMoney(pagamento.valor)} de ${pagamento.cliente?.nome}?`,
    confirmText: 'Excluir',
    danger: true
  })
  if (!ok) return
  try {
    await pagamentosApi.excluir(pagamento.id)
    ui.success('Pagamento excluído.')
    list.reload()
  } catch (e) {
    ui.error(deleteErrorMessage(e, 'pagamento'))
  }
}
</script>
