<template>
  <BaseModal :open="open" :title="agendamento ? 'Editar agendamento' : 'Novo agendamento'" @close="$emit('close')">
    <div v-if="carregando" class="muted">Carregando...</div>
    <form v-else id="agendamento-form" class="form-grid" novalidate @submit.prevent="save">
      <FormField v-slot="{ id, invalid }" label="Cliente" required full :error="errors.cliente">
        <select :id="id" v-model="form.clienteId" class="input" :class="{ 'has-error': invalid }">
          <option value="" disabled>Selecione o cliente</option>
          <option v-for="c in clientes" :key="c.id" :value="c.id">{{ c.nome }} — {{ c.telefone }}</option>
        </select>
      </FormField>
      <FormField v-slot="{ id, invalid }" label="Serviço" required full :error="errors.servico">
        <select :id="id" v-model="form.servicoId" class="input" :class="{ 'has-error': invalid }">
          <option value="" disabled>Selecione o serviço</option>
          <option v-for="s in servicos" :key="s.id" :value="s.id">
            {{ s.descricao }} ({{ CONFIGURACOES[s.configuracao] }}) — {{ formatMoney(s.preco) }}
          </option>
        </select>
      </FormField>
      <FormField
        v-slot="{ id, invalid }"
        label="Data e hora"
        required
        full
        :error="errors.dataHora"
        hint="Não é possível agendar no passado nem em um horário já ocupado."
      >
        <input :id="id" v-model="form.dataHora" type="datetime-local" class="input" :class="{ 'has-error': invalid }" :min="agora" />
      </FormField>
      <p v-if="!clientes.length || !servicos.length" class="alert alert-info full">
        Cadastre pelo menos um cliente e um serviço antes de agendar.
      </p>
    </form>
    <template #footer>
      <button class="btn" @click="$emit('close')">Cancelar</button>
      <button class="btn btn-primary" form="agendamento-form" :disabled="saving || carregando">
        {{ saving ? 'Salvando...' : 'Salvar' }}
      </button>
    </template>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { agendamentosApi, clientesApi, servicosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { contentOf } from '@/utils/page'
import { errorMessage, fieldErrors } from '@/utils/errors'
import { formatMoney, fromInputDateTime, toInputDateTime } from '@/utils/format'
import { CONFIGURACOES } from '@/utils/labels'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  agendamento: { type: Object, default: null }
})
const emit = defineEmits(['close', 'saved'])

const ui = useUiStore()
const clientes = ref([])
const servicos = ref([])
const carregando = ref(false)
const saving = ref(false)
const errors = ref({})
const agora = ref('')
const form = ref({ clienteId: '', servicoId: '', dataHora: '' })

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    const a = props.agendamento
    form.value = {
      clienteId: a?.cliente?.id ?? '',
      servicoId: a?.servico?.id ?? '',
      dataHora: a ? toInputDateTime(a.dataHora) : ''
    }
    errors.value = {}
    agora.value = toInputDateTime(new Date())
    carregando.value = true
    try {
      const [cs, ss] = await Promise.all([clientesApi.todos('nome'), servicosApi.todos('descricao')])
      clientes.value = contentOf(cs)
      servicos.value = contentOf(ss)
    } catch (e) {
      ui.error(errorMessage(e, 'Não foi possível carregar clientes e serviços.'))
    } finally {
      carregando.value = false
    }
  }
)

function validate() {
  const e = {}
  if (!form.value.clienteId) e.cliente = 'Selecione o cliente.'
  if (!form.value.servicoId) e.servico = 'Selecione o serviço.'
  if (!form.value.dataHora) e.dataHora = 'Informe a data e a hora.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    dataHora: fromInputDateTime(form.value.dataHora),
    cliente: { id: form.value.clienteId },
    servico: { id: form.value.servicoId }
  }
  try {
    if (props.agendamento) {
      await agendamentosApi.atualizar(props.agendamento.id, body)
      ui.success('Agendamento atualizado.')
    } else {
      await agendamentosApi.criar(body)
      ui.success('Agendamento criado.')
    }
    emit('saved')
  } catch (e) {
    errors.value = fieldErrors(e)
    ui.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}
</script>
