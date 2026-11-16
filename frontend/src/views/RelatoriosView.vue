<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Relatórios</h1>
        <p>Indicadores do período. Cada relatório gerado fica registrado no histórico.</p>
      </div>
    </div>

    <div class="card card-pad">
      <form class="gerar" novalidate @submit.prevent="gerar">
        <div class="field">
          <label for="rel-tipo">Tipo</label>
          <select id="rel-tipo" v-model="tipo" class="input">
            <option v-for="o in tipoOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>
        <div class="field">
          <label for="rel-inicio">De</label>
          <input id="rel-inicio" v-model="inicio" type="date" class="input" />
        </div>
        <div class="field">
          <label for="rel-fim">Até</label>
          <input id="rel-fim" v-model="fim" type="date" class="input" />
        </div>
        <button class="btn btn-primary" :disabled="gerando"><AppIcon name="chart" /> {{ gerando ? 'Gerando...' : 'Gerar relatório' }}</button>
      </form>
    </div>

    <RelatorioResultado v-if="resultado" :resultado="resultado" class="resultado" />

    <div class="card historico">
      <div class="toolbar"><strong>Histórico</strong></div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Gerado em</th><th>Tipo</th><th>Gerado por</th></tr>
          </thead>
          <tbody>
            <TableState
              :colspan="3"
              :loading="historico.loading.value"
              :error="historico.error.value"
              :empty="!historico.items.value.length"
              empty-text="Nenhum relatório gerado ainda."
              icon="chart"
              @retry="historico.reload"
            />
            <template v-if="!historico.loading.value">
              <tr v-for="r in historico.items.value" :key="r.id">
                <td>{{ formatDateTime(r.dataGeracao) }}</td>
                <td>{{ TIPOS_RELATORIO[r.tipoRelatorio] ?? r.tipoRelatorio }}</td>
                <td>{{ r.usuario?.nome ?? '—' }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <PaginationBar
        :page="historico.page.value"
        :total-pages="historico.totalPages.value"
        :total-elements="historico.totalElements.value"
        @change="historico.load"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { relatoriosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { errorMessage } from '@/utils/errors'
import { firstDayOfMonth, formatDateTime, lastDayOfMonth } from '@/utils/format'
import { TIPOS_RELATORIO, toOptions } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'
import RelatorioResultado from './relatorios/RelatorioResultado.vue'

const tipoOptions = toOptions(TIPOS_RELATORIO)
const ui = useUiStore()
const tipo = ref('GERAL')
const inicio = ref(firstDayOfMonth())
const fim = ref(lastDayOfMonth())
const gerando = ref(false)
const resultado = ref(null)

const historico = usePagedList((params) => relatoriosApi.listar(params))
onMounted(() => historico.load())

async function gerar() {
  if (!inicio.value || !fim.value) {
    ui.error('Informe o período.')
    return
  }
  if (fim.value < inicio.value) {
    ui.error('A data final não pode ser antes da data inicial.')
    return
  }
  gerando.value = true
  try {
    resultado.value = await relatoriosApi.gerar(tipo.value, inicio.value, fim.value)
    historico.reset()
  } catch (e) {
    ui.error(errorMessage(e, 'Não foi possível gerar o relatório.'))
  } finally {
    gerando.value = false
  }
}
</script>

<style scoped>
.gerar {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr auto;
  align-items: end;
  gap: 14px;
}
@media (max-width: 760px) {
  .gerar {
    grid-template-columns: 1fr 1fr;
  }
  .gerar .field:first-child,
  .gerar .btn {
    grid-column: 1 / -1;
  }
}
.resultado,
.historico {
  margin-top: 20px;
}
</style>
