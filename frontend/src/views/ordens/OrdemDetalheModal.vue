<template>
  <BaseModal
    :open="!!ordemId"
    :title="ordem ? `Ordem de serviço ${shortId(ordem.id)}` : 'Ordem de serviço'"
    :subtitle="ordem ? `Aberta em ${formatDateTime(ordem.dataAbertura)}` : ''"
    size="lg"
    @close="$emit('close')"
  >
    <div v-if="loading" class="muted">Carregando...</div>
    <div v-else-if="erro" class="alert alert-error">{{ erro }}</div>
    <div v-else-if="ordem" class="stack">
      <div class="summary">
        <div>
          <span class="muted">Status</span>
          <StatusBadge :status="ordem.status" :map="STATUS_ORDEM" />
        </div>
        <div>
          <span class="muted">Cliente</span>
          <strong>{{ cliente?.nome }}</strong>
          <small class="muted">{{ cliente?.telefone }}</small>
        </div>
        <div>
          <span class="muted">Serviço</span>
          <strong>{{ servico?.descricao }}</strong>
          <small class="muted">{{ CONFIGURACOES[servico?.configuracao] }} · agendado {{ formatDateTime(ordem.agendamento?.dataHora) }}</small>
        </div>
        <div>
          <span class="muted">Responsável</span>
          <div v-if="podeTrocar" class="row">
            <select v-model="funcionarioId" class="input input-sm" aria-label="Funcionário responsável">
              <option v-for="f in funcionarios" :key="f.id" :value="f.id">{{ f.nome }}</option>
            </select>
            <button class="btn btn-sm" :disabled="funcionarioId === ordem.funcionario?.id" @click="trocarFuncionario">Trocar</button>
          </div>
          <strong v-else>{{ ordem.funcionario?.nome }}</strong>
        </div>
      </div>

      <div v-if="emAberto" class="row">
        <button v-if="ordem.status === 'ABERTA'" class="btn btn-primary" @click="$emit('acao', ordem, 'iniciar')">
          <AppIcon name="play" :size="14" /> Iniciar execução
        </button>
        <button v-if="ordem.status === 'EM_ANDAMENTO'" class="btn btn-primary" @click="$emit('acao', ordem, 'concluir')">
          <AppIcon name="check" /> Concluir e baixar estoque
        </button>
        <button class="btn btn-danger" @click="$emit('acao', ordem, 'cancelar')"><AppIcon name="x" /> Cancelar OS</button>
      </div>

      <div class="grid-2">
        <section class="block">
          <h3 class="card-title">{{ ordem.status === 'CONCLUIDA' ? 'Baixas de estoque' : 'Produtos que serão usados' }}</h3>
          <template v-if="ordem.status === 'CONCLUIDA'">
            <p v-if="!movimentacoes.length" class="muted">Nenhum produto foi baixado.</p>
            <ul v-else class="lines">
              <li v-for="m in movimentacoes" :key="m.id">
                <span>{{ m.produto?.nome }}</span>
                <strong>−{{ formatNumber(m.quantidade) }} {{ UNIDADES_CURTAS[m.produto?.unidadeMedida] }}</strong>
              </li>
            </ul>
          </template>
          <template v-else>
            <p v-if="!itensServico.length" class="muted">O serviço não consome produtos.</p>
            <ul v-else class="lines">
              <li v-for="i in itensServico" :key="i.id">
                <span>{{ i.produto.nome }}</span>
                <span :class="{ falta: i.produto.estoque < i.quantidadeNecessaria }">
                  {{ i.quantidadeNecessaria }} {{ UNIDADES_CURTAS[i.produto.unidadeMedida] }}
                  <small class="muted">(estoque {{ formatNumber(i.produto.estoque) }})</small>
                </span>
              </li>
            </ul>
          </template>
        </section>

        <section class="block">
          <div class="row between">
            <h3 class="card-title">Pagamentos</h3>
            <button v-if="saldo > 0 && ordem.status !== 'CANCELADA'" class="btn btn-sm" @click="pagamentoOpen = true">
              <AppIcon name="plus" :size="15" /> Registrar
            </button>
          </div>
          <ul class="lines">
            <li v-for="p in pagamentos" :key="p.id">
              <span>{{ formatDate(p.data) }} <small class="muted">{{ p.descricao }}</small></span>
              <strong>{{ formatMoney(p.valor) }}</strong>
            </li>
            <li class="total first-total"><span>Valor da OS</span><strong>{{ formatMoney(ordem.valorTotal) }}</strong></li>
            <li class="total"><span>Pago</span><strong>{{ formatMoney(pago) }}</strong></li>
            <li class="total" :class="{ pendente: saldo > 0 }"><span>Saldo</span><strong>{{ formatMoney(saldo) }}</strong></li>
          </ul>
        </section>
      </div>
    </div>

    <PagamentoFormModal
      :open="pagamentoOpen"
      :preset="ordem ? { clienteId: cliente?.id, ordemServicoId: ordem.id, valor: saldo } : null"
      @close="pagamentoOpen = false"
      @saved="onPagamento"
    />
  </BaseModal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ordensApi, pagamentosApi, servicosApi, usuariosApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { contentOf } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { formatDate, formatDateTime, formatMoney, formatNumber, shortId } from '@/utils/format'
import { CONFIGURACOES, STATUS_ORDEM, UNIDADES_CURTAS } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import PagamentoFormModal from '../pagamentos/PagamentoFormModal.vue'

const props = defineProps({
  ordemId: { type: String, default: null },
  version: { type: Number, default: 0 }
})
const emit = defineEmits(['close', 'changed', 'acao'])

const auth = useAuthStore()
const ui = useUiStore()
const ordem = ref(null)
const itensServico = ref([])
const movimentacoes = ref([])
const pagamentos = ref([])
const funcionarios = ref([])
const funcionarioId = ref('')
const loading = ref(false)
const erro = ref('')
const pagamentoOpen = ref(false)

const cliente = computed(() => ordem.value?.agendamento?.cliente)
const servico = computed(() => ordem.value?.agendamento?.servico)
const emAberto = computed(() => ['ABERTA', 'EM_ANDAMENTO'].includes(ordem.value?.status))
const podeTrocar = computed(() => auth.isGestor && emAberto.value && funcionarios.value.length > 0)
const pago = computed(() => pagamentos.value.reduce((s, p) => s + (p.valor ?? 0), 0))
const saldo = computed(() => Math.max(0, Math.round(((ordem.value?.valorTotal ?? 0) - pago.value) * 100) / 100))

async function load(id) {
  loading.value = true
  erro.value = ''
  try {
    const os = await ordensApi.buscar(id)
    const [itens, movs, pags, usuarios] = await Promise.all([
      servicosApi.listarProdutos(os.agendamento.servico.id),
      os.status === 'CONCLUIDA' ? ordensApi.movimentacoes(id) : [],
      pagamentosApi.listarPorOrdem(id),
      auth.isGestor ? usuariosApi.todos('nome') : null
    ])
    ordem.value = os
    itensServico.value = itens
    movimentacoes.value = movs
    pagamentos.value = pags
    funcionarios.value = usuarios ? contentOf(usuarios) : []
    funcionarioId.value = os.funcionario?.id ?? ''
  } catch (e) {
    erro.value = errorMessage(e, 'Não foi possível carregar a ordem de serviço.')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.ordemId,
  (id) => {
    ordem.value = null
    if (id) load(id)
  }
)
watch(
  () => props.version,
  () => {
    if (props.ordemId) load(props.ordemId)
  }
)

async function trocarFuncionario() {
  try {
    ordem.value = await ordensApi.trocarFuncionario(ordem.value.id, funcionarioId.value)
    ui.success('Responsável alterado.')
    emit('changed')
  } catch (e) {
    ui.error(errorMessage(e))
  }
}

function onPagamento() {
  pagamentoOpen.value = false
  load(ordem.value.id)
}
</script>

<style scoped>
.summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.summary > div {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--surface-muted);
  min-width: 0;
}
.summary .muted {
  font-size: 12px;
}
@media (max-width: 760px) {
  .summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
.input-sm {
  height: 30px;
  max-width: 150px;
  padding: 0 8px;
}
.block {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}
.between {
  justify-content: space-between;
}
.lines {
  margin: 0;
  padding: 0;
  list-style: none;
}
.lines li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed var(--border);
}
.lines li:last-child {
  border-bottom: none;
}
.lines .total {
  border-bottom: none;
  padding: 3px 0;
}
.lines .first-total {
  margin-top: 6px;
}
.pendente strong {
  color: var(--warning);
}
.falta {
  color: var(--danger);
  font-weight: 600;
}
</style>
