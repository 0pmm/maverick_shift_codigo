<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Olá, {{ primeiroNome }}</h1>
        <p>{{ hojeExtenso }}</p>
      </div>
      <div class="row">
        <RouterLink to="/agendamentos" class="btn"><AppIcon name="calendar" /> Agendar</RouterLink>
        <RouterLink to="/ordens-servico" class="btn btn-primary"><AppIcon name="clipboard" /> Ordens de serviço</RouterLink>
      </div>
    </div>

    <div v-if="erro" class="alert alert-error">{{ erro }}</div>

    <section class="tiles">
      <RouterLink v-for="t in tiles" :key="t.label" :to="t.to" class="tile card">
        <span class="tile-icon"><AppIcon :name="t.icon" /></span>
        <span class="muted">{{ t.label }}</span>
        <strong>{{ loading ? '…' : t.value }}</strong>
      </RouterLink>
    </section>

    <div class="grid-2 panels">
      <section class="card">
        <div class="toolbar">
          <strong>Agenda de hoje</strong>
          <span class="spacer" />
          <RouterLink to="/agendamentos" class="link">Ver todos</RouterLink>
        </div>
        <p v-if="!loading && !agendaHoje.length" class="vazio muted">Nenhum agendamento para hoje.</p>
        <ul class="list">
          <li v-for="a in agendaHoje" :key="a.id">
            <span class="hora">{{ formatTime(a.dataHora) }}</span>
            <div class="info">
              <strong>{{ a.cliente?.nome }}</strong>
              <span class="muted">{{ a.servico?.descricao }}</span>
            </div>
            <StatusBadge :status="a.status" :map="STATUS_AGENDAMENTO" />
          </li>
        </ul>
      </section>

      <section class="card">
        <div class="toolbar">
          <strong>Ordens em execução</strong>
          <span class="spacer" />
          <RouterLink to="/ordens-servico" class="link">Ver todas</RouterLink>
        </div>
        <p v-if="!loading && !emExecucao.length" class="vazio muted">Nenhuma ordem aberta ou em andamento.</p>
        <ul class="list">
          <li v-for="o in emExecucao" :key="o.id">
            <span class="mono hora">{{ shortId(o.id) }}</span>
            <div class="info">
              <strong>{{ o.agendamento?.cliente?.nome }}</strong>
              <span class="muted">{{ o.agendamento?.servico?.descricao }} · {{ o.funcionario?.nome }}</span>
            </div>
            <StatusBadge :status="o.status" :map="STATUS_ORDEM" />
          </li>
        </ul>
      </section>
    </div>

    <section v-if="estoqueBaixo.length" class="card alerta-estoque">
      <div class="toolbar">
        <AppIcon name="alert" />
        <strong>Estoque baixo</strong>
        <span class="spacer" />
        <RouterLink to="/produtos" class="link">Ver produtos</RouterLink>
      </div>
      <ul class="chips">
        <li v-for="p in estoqueBaixo" :key="p.id">
          {{ p.nome }} <strong>{{ formatNumber(p.estoque) }} {{ UNIDADES_CURTAS[p.unidadeMedida] }}</strong>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { agendamentosApi, ordensApi, pagamentosApi, produtosApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { normalizePage } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { firstDayOfMonth, formatMoney, formatNumber, formatTime, lastDayOfMonth, shortId, toIsoDate } from '@/utils/format'
import { STATUS_AGENDAMENTO, STATUS_ORDEM, UNIDADES_CURTAS } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import StatusBadge from '@/components/StatusBadge.vue'

const ESTOQUE_BAIXO = 5
const LISTA = 6

const auth = useAuthStore()
const loading = ref(true)
const erro = ref('')
const agendaHoje = ref([])
const emExecucao = ref([])
const estoqueBaixo = ref([])
const totais = ref({ hoje: 0, abertas: 0, andamento: 0, recebido: 0 })

const primeiroNome = computed(() => auth.nome.split(' ')[0])
const hojeExtenso = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })

const tiles = computed(() => [
  { label: 'Agendamentos hoje', value: totais.value.hoje, icon: 'calendar', to: '/agendamentos' },
  { label: 'OS abertas', value: totais.value.abertas, icon: 'clipboard', to: '/ordens-servico' },
  { label: 'OS em andamento', value: totais.value.andamento, icon: 'wrench', to: '/ordens-servico' },
  { label: 'Recebido no mês', value: formatMoney(totais.value.recebido), icon: 'money', to: '/pagamentos' }
])

onMounted(async () => {
  const hoje = toIsoDate()
  try {
    const [agenda, abertas, andamento, pagamentosMes, produtos] = await Promise.all([
      agendamentosApi.listarPorPeriodo(hoje, hoje, { page: 0, size: 50, sort: 'dataHora,asc' }),
      ordensApi.listarPorStatus('ABERTA', { page: 0, size: LISTA }),
      ordensApi.listarPorStatus('EM_ANDAMENTO', { page: 0, size: LISTA }),
      pagamentosApi.listarPorPeriodo(firstDayOfMonth(), lastDayOfMonth(), { page: 0, size: 1000 }),
      produtosApi.todos('estoque,asc')
    ])
    const ag = normalizePage(agenda)
    const ab = normalizePage(abertas)
    const em = normalizePage(andamento)
    agendaHoje.value = ag.items
    emExecucao.value = [...em.items, ...ab.items].slice(0, LISTA)
    estoqueBaixo.value = normalizePage(produtos).items.filter((p) => p.estoque <= ESTOQUE_BAIXO)
    totais.value = {
      hoje: ag.totalElements,
      abertas: ab.totalElements,
      andamento: em.totalElements,
      recebido: normalizePage(pagamentosMes).items.reduce((s, p) => s + p.valor, 0)
    }
  } catch (e) {
    erro.value = errorMessage(e, 'Não foi possível carregar o painel.')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-header p::first-letter {
  text-transform: uppercase;
}
.alert {
  margin-bottom: 16px;
}
.tiles {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}
@media (max-width: 1000px) {
  .tiles {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
.tile {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 16px 18px;
  text-decoration: none;
  transition: border-color 0.15s;
}
.tile:hover {
  border-color: var(--border-strong);
}
.tile strong {
  font-size: 26px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;
}
.tile-icon {
  position: absolute;
  top: 14px;
  right: 14px;
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--accent-soft);
  color: var(--accent);
}
.panels {
  margin-top: 20px;
}
.link {
  color: var(--accent);
  font-weight: 600;
  font-size: 13px;
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}
.vazio {
  margin: 0;
  padding: 28px 16px;
  text-align: center;
}
.list {
  margin: 0;
  padding: 0;
  list-style: none;
}
.list li {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 11px 16px;
  border-bottom: 1px solid var(--border);
}
.list li:last-child {
  border-bottom: none;
}
.hora {
  width: 76px;
  flex-shrink: 0;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.info {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}
.info span {
  font-size: 12.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.alerta-estoque {
  margin-top: 20px;
}
.alerta-estoque .toolbar {
  color: var(--warning);
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0;
  padding: 14px 16px;
  list-style: none;
}
.chips li {
  padding: 4px 10px;
  border: 1px solid #fedf89;
  border-radius: 999px;
  background: var(--warning-soft);
  font-size: 13px;
}
</style>
