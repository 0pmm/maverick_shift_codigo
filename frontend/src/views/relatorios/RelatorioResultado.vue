<template>
  <div class="stack">
    <div class="card card-pad head">
      <div>
        <h2>Relatório {{ TIPOS_RELATORIO[resultado.tipoRelatorio] ?? resultado.tipoRelatorio }}</h2>
        <p class="muted">
          {{ formatDate(resultado.inicio) }} a {{ formatDate(resultado.fim) }} · gerado em
          {{ formatDateTime(resultado.dataGeracao) }}<template v-if="resultado.geradoPor"> por {{ resultado.geradoPor }}</template>
        </p>
      </div>
    </div>

    <section v-if="financeiro" class="tiles">
      <div class="tile card">
        <span class="muted">Total recebido</span>
        <strong>{{ formatMoney(financeiro.totalRecebido) }}</strong>
      </div>
      <div class="tile card">
        <span class="muted">Pagamentos</span>
        <strong>{{ financeiro.quantidadePagamentos }}</strong>
      </div>
      <div class="tile card">
        <span class="muted">Ticket médio</span>
        <strong>{{ formatMoney(financeiro.ticketMedio) }}</strong>
      </div>
      <div v-if="clientes" class="tile card">
        <span class="muted">Clientes cadastrados</span>
        <strong>{{ clientes.totalClientes }}</strong>
      </div>
    </section>
    <section v-else-if="clientes" class="tiles">
      <div class="tile card">
        <span class="muted">Clientes cadastrados</span>
        <strong>{{ clientes.totalClientes }}</strong>
      </div>
    </section>

    <div v-if="servicos || clientes" class="grid-2">
      <RankList
        v-if="servicos"
        title="Serviços mais agendados"
        :rows="toRows(servicos.servicosMaisAgendados)"
        empty-text="Nenhum agendamento no período."
      />
      <RankList
        v-if="servicos"
        title="Agendamentos por status"
        :rows="toRows(servicos.agendamentosPorStatus, (k) => STATUS_AGENDAMENTO[k]?.label ?? k)"
        empty-text="Nenhum agendamento no período."
      />
      <RankList
        v-if="clientes"
        title="Clientes que mais pagaram"
        :rows="toRows(clientes.clientesQueMaisPagaram)"
        :format="formatMoney"
        empty-text="Nenhum pagamento no período."
      />
    </div>

    <div v-if="produtos" class="card">
      <div class="toolbar"><strong>Estoque e movimentações no período</strong></div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Categoria</th>
              <th class="num">Entradas</th>
              <th class="num">Saídas</th>
              <th class="num">Estoque atual</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!produtos.estoqueAtual.length">
              <td colspan="5" class="muted">Nenhum produto cadastrado.</td>
            </tr>
            <tr v-for="p in produtos.estoqueAtual" :key="p.id">
              <td><strong>{{ p.nome }}</strong></td>
              <td>{{ p.categoria }}</td>
              <td class="num">{{ formatNumber(mov(p.nome, 'ENTRADA')) }}</td>
              <td class="num">{{ formatNumber(mov(p.nome, 'SAIDA')) }}</td>
              <td class="num">
                <strong>{{ formatNumber(p.estoque) }}</strong> {{ UNIDADES_CURTAS[p.unidadeMedida] }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate, formatDateTime, formatMoney, formatNumber } from '@/utils/format'
import { STATUS_AGENDAMENTO, TIPOS_RELATORIO, UNIDADES_CURTAS } from '@/utils/labels'
import RankList from './RankList.vue'

const props = defineProps({ resultado: { type: Object, required: true } })

const financeiro = computed(() => props.resultado.financeiro)
const servicos = computed(() => props.resultado.servicos)
const produtos = computed(() => props.resultado.produtos)
const clientes = computed(() => props.resultado.clientes)

// o backend devolve mapas {nome: valor}
const toRows = (map = {}, label = (k) => k) =>
  Object.entries(map)
    .map(([key, value]) => ({ label: label(key), value: Number(value) }))
    .sort((a, b) => b.value - a.value)

const mov = (produto, tipo) => produtos.value?.movimentacoesNoPeriodo?.[produto]?.[tipo] ?? 0
</script>

<style scoped>
.head h2 {
  font-size: 18px;
}
.head p {
  margin: 4px 0 0;
}
.tiles {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}
.tile {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 18px;
}
.tile strong {
  font-size: 24px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;
}
</style>
