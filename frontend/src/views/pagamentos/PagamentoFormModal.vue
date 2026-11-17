<template>
  <BaseModal :open="open" :title="pagamento ? 'Editar pagamento' : 'Registrar pagamento'" @close="$emit('close')">
    <div v-if="carregando" class="muted">Carregando...</div>
    <form v-else id="pagamento-form" class="form-grid" novalidate @submit.prevent="save">
      <FormField v-slot="{ id, invalid }" label="Cliente" required full :error="errors.cliente">
        <select :id="id" v-model="form.clienteId" class="input" :class="{ 'has-error': invalid }" :disabled="travado">
          <option value="" disabled>Selecione o cliente</option>
          <option v-for="c in clientes" :key="c.id" :value="c.id">{{ c.nome }}</option>
        </select>
      </FormField>
      <FormField
        v-slot="{ id }"
        label="Ordem de serviço"
        full
        :hint="saldoTexto || 'Opcional. Ao vincular, o valor não pode passar do saldo da OS.'"
      >
        <select :id="id" v-model="form.ordemServicoId" class="input" :disabled="travado || !form.clienteId">
          <option value="">Sem ordem de serviço</option>
          <option v-for="o in ordensDoCliente" :key="o.id" :value="o.id">
            {{ shortId(o.id) }} — {{ o.agendamento?.servico?.descricao }} — {{ formatMoney(o.valorTotal) }} ({{ STATUS_ORDEM[o.status]?.label }})
          </option>
        </select>
      </FormField>
      <FormField v-slot="{ id, invalid }" label="Valor (R$)" required :error="errors.valor">
        <input :id="id" v-model.number="form.valor" type="number" min="0.01" step="0.01" class="input" :class="{ 'has-error': invalid }" />
      </FormField>
      <FormField v-slot="{ id, invalid }" label="Data" :error="errors.data" hint="Não pode ser no futuro.">
        <input :id="id" v-model="form.data" type="datetime-local" class="input" :class="{ 'has-error': invalid }" :max="agora" />
      </FormField>
      <FormField v-slot="{ id, invalid }" label="Descrição" full :error="errors.descricao">
        <input :id="id" v-model="form.descricao" class="input" :class="{ 'has-error': invalid }" maxlength="255" placeholder="Pagamento via Pix" />
      </FormField>
    </form>
    <template #footer>
      <button class="btn" @click="$emit('close')">Cancelar</button>
      <button class="btn btn-primary" form="pagamento-form" :disabled="saving || carregando">
        {{ saving ? 'Salvando...' : 'Salvar' }}
      </button>
    </template>
  </BaseModal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { clientesApi, ordensApi, pagamentosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { contentOf } from '@/utils/page'
import { errorMessage, fieldErrors } from '@/utils/errors'
import { formatMoney, fromInputDateTime, shortId, toInputDateTime } from '@/utils/format'
import { STATUS_ORDEM } from '@/utils/labels'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  pagamento: { type: Object, default: null },
  // vindo da OS: { clienteId, ordemServicoId, valor }
  preset: { type: Object, default: null }
})
const emit = defineEmits(['close', 'saved'])

const ui = useUiStore()
const clientes = ref([])
const ordens = ref([])
const saldo = ref(null)
const carregando = ref(false)
const saving = ref(false)
const errors = ref({})
const agora = ref('')
const form = ref({})

const travado = computed(() => !!props.preset)
const ordensDoCliente = computed(() =>
  ordens.value.filter((o) => o.agendamento?.cliente?.id === form.value.clienteId && o.status !== 'CANCELADA')
)
const saldoTexto = computed(() => (saldo.value == null ? '' : `Saldo em aberto da OS: ${formatMoney(saldo.value)}`))

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    const p = props.pagamento
    form.value = {
      clienteId: p?.cliente?.id ?? props.preset?.clienteId ?? '',
      ordemServicoId: p?.ordemServico?.id ?? props.preset?.ordemServicoId ?? '',
      valor: p?.valor ?? props.preset?.valor ?? null,
      data: toInputDateTime(p?.data ?? new Date()),
      descricao: p?.descricao ?? ''
    }
    errors.value = {}
    agora.value = toInputDateTime(new Date())
    carregando.value = true
    try {
      const [cs, os] = await Promise.all([clientesApi.todos('nome'), ordensApi.todas()])
      clientes.value = contentOf(cs)
      ordens.value = contentOf(os)
      await atualizarSaldo()
    } catch (e) {
      ui.error(errorMessage(e, 'Não foi possível carregar clientes e ordens.'))
    } finally {
      carregando.value = false
    }
  }
)

// saldo = valor da OS - pagamentos ja feitos (sem contar o que esta sendo editado)
async function atualizarSaldo() {
  const ordem = ordens.value.find((o) => o.id === form.value.ordemServicoId)
  if (!ordem) {
    saldo.value = null
    return
  }
  const pagos = await pagamentosApi.listarPorOrdem(ordem.id)
  const total = pagos.filter((p) => p.id !== props.pagamento?.id).reduce((s, p) => s + p.valor, 0)
  saldo.value = Math.round((ordem.valorTotal - total) * 100) / 100
}

watch(
  () => form.value.clienteId,
  (novo, antigo) => {
    // trocou o cliente: a OS selecionada era do outro cliente
    if (antigo && novo !== antigo) form.value.ordemServicoId = ''
  }
)
watch(
  () => form.value.ordemServicoId,
  () => {
    if (!carregando.value) atualizarSaldo().catch(() => (saldo.value = null))
  }
)

function validate() {
  const e = {}
  if (!form.value.clienteId) e.cliente = 'Selecione o cliente.'
  if (typeof form.value.valor !== 'number' || form.value.valor <= 0) e.valor = 'O valor deve ser maior que zero.'
  else if (saldo.value != null && form.value.valor > saldo.value + 0.001) e.valor = `Máximo: ${formatMoney(saldo.value)}.`
  if (form.value.data && form.value.data > toInputDateTime(new Date())) e.data = 'A data não pode ser no futuro.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    valor: form.value.valor,
    data: fromInputDateTime(form.value.data),
    descricao: form.value.descricao.trim() || null,
    cliente: { id: form.value.clienteId },
    ordemServico: form.value.ordemServicoId ? { id: form.value.ordemServicoId } : null
  }
  try {
    if (props.pagamento) {
      await pagamentosApi.atualizar(props.pagamento.id, body)
      ui.success('Pagamento atualizado.')
    } else {
      await pagamentosApi.criar(body)
      ui.success('Pagamento registrado.')
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
