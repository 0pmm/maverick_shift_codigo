<template>
  <BaseModal :open="open" title="Abrir ordem de serviço" subtitle="O valor da OS é o preço do serviço agendado." @close="$emit('close')">
    <div v-if="carregando" class="muted">Carregando...</div>
    <form v-else id="abrir-os-form" class="stack" novalidate @submit.prevent="submit">
      <FormField v-slot="{ id }" label="Agendamento" required :error="erroAgendamento">
        <select :id="id" v-model="agendamentoId" class="input" :disabled="!!agendamento">
          <option value="" disabled>Selecione um agendamento</option>
          <option v-for="a in agendamentos" :key="a.id" :value="a.id">
            {{ formatDateTime(a.dataHora) }} — {{ a.cliente?.nome }} — {{ a.servico?.descricao }}
          </option>
        </select>
      </FormField>
      <p v-if="!agendamento && !agendamentos.length" class="alert alert-info">
        Não há agendamentos pendentes sem ordem de serviço.
      </p>

      <FormField
        v-if="auth.isGestor"
        v-slot="{ id }"
        label="Funcionário responsável"
        hint="Em branco, você fica como responsável."
      >
        <select :id="id" v-model="funcionarioId" class="input">
          <option value="">Eu mesmo ({{ auth.nome }})</option>
          <option v-for="f in funcionarios" :key="f.id" :value="f.id">{{ f.nome }} — {{ CARGOS[f.cargo] }}</option>
        </select>
      </FormField>
      <p v-else class="muted">Você ficará como funcionário responsável.</p>
    </form>
    <template #footer>
      <button class="btn" @click="$emit('close')">Cancelar</button>
      <button class="btn btn-primary" form="abrir-os-form" :disabled="saving || carregando">
        {{ saving ? 'Abrindo...' : 'Abrir OS' }}
      </button>
    </template>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { agendamentosApi, ordensApi, usuariosApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { contentOf } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { formatDateTime } from '@/utils/format'
import { CARGOS } from '@/utils/labels'
import BaseModal from './BaseModal.vue'
import FormField from './FormField.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  agendamento: { type: Object, default: null } // quando vem da tela de agendamentos
})
const emit = defineEmits(['close', 'opened'])

const auth = useAuthStore()
const ui = useUiStore()
const agendamentos = ref([])
const funcionarios = ref([])
const agendamentoId = ref('')
const funcionarioId = ref('')
const erroAgendamento = ref('')
const carregando = ref(false)
const saving = ref(false)

// agendamentos AGENDADO que ainda nao tem OS
async function loadPendentes() {
  const [ags, ordens] = await Promise.all([
    agendamentosApi.listar({ page: 0, size: 1000, sort: 'dataHora,asc' }),
    ordensApi.todas()
  ])
  const usados = new Set(contentOf(ordens).map((o) => o.agendamento?.id))
  return contentOf(ags).filter((a) => a.status === 'AGENDADO' && !usados.has(a.id))
}

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    agendamentoId.value = props.agendamento?.id ?? ''
    funcionarioId.value = ''
    erroAgendamento.value = ''
    carregando.value = true
    try {
      const [pendentes, usuarios] = await Promise.all([
        props.agendamento ? [props.agendamento] : loadPendentes(),
        auth.isGestor ? usuariosApi.todos('nome') : null
      ])
      agendamentos.value = pendentes
      funcionarios.value = usuarios ? contentOf(usuarios).filter((u) => u.login !== auth.login) : []
    } catch (e) {
      ui.error(errorMessage(e, 'Não foi possível carregar os dados.'))
    } finally {
      carregando.value = false
    }
  }
)

async function submit() {
  if (!agendamentoId.value) {
    erroAgendamento.value = 'Selecione o agendamento.'
    return
  }
  saving.value = true
  try {
    const ordem = await ordensApi.abrir(agendamentoId.value, funcionarioId.value)
    ui.success('Ordem de serviço aberta.')
    emit('opened', ordem)
  } catch (e) {
    ui.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}
</script>
