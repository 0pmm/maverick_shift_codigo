<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Serviços</h1>
        <p>Catálogo de serviços e os produtos que cada um consome.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()"><AppIcon name="plus" /> Novo serviço</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <input v-model="busca" class="input input-search" placeholder="Buscar por descrição" aria-label="Buscar por descrição" />
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Descrição</th>
              <th>Veículo</th>
              <th class="num">Preço</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="4"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum serviço encontrado."
              icon="wrench"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="s in list.items.value" :key="s.id">
                <td><strong>{{ s.descricao }}</strong></td>
                <td>{{ CONFIGURACOES[s.configuracao] ?? s.configuracao }}</td>
                <td class="num">{{ formatMoney(s.preco) }}</td>
                <td class="actions">
                  <button class="btn btn-sm" @click="produtosDe = s"><AppIcon name="box" :size="15" /> Produtos</button>
                  <button class="btn btn-sm" @click="openForm(s)"><AppIcon name="edit" :size="15" /> Editar</button>
                  <button class="btn btn-sm btn-danger" @click="remove(s)"><AppIcon name="trash" :size="15" /></button>
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

    <BaseModal :open="formOpen" :title="form.id ? 'Editar serviço' : 'Novo serviço'" @close="formOpen = false">
      <form id="servico-form" class="form-grid" novalidate @submit.prevent="save">
        <FormField v-slot="{ id, invalid }" label="Descrição" required full :error="errors.descricao">
          <input :id="id" v-model="form.descricao" class="input" :class="{ 'has-error': invalid }" maxlength="100" placeholder="Lavagem completa" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Tipo de veículo" required :error="errors.configuracao">
          <select :id="id" v-model="form.configuracao" class="input" :class="{ 'has-error': invalid }">
            <option value="" disabled>Selecione</option>
            <option v-for="o in configuracaoOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Preço (R$)" required :error="errors.preco">
          <input :id="id" v-model.number="form.preco" type="number" min="0.01" step="0.01" class="input" :class="{ 'has-error': invalid }" />
        </FormField>
      </form>
      <template #footer>
        <button class="btn" @click="formOpen = false">Cancelar</button>
        <button class="btn btn-primary" form="servico-form" :disabled="saving">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
      </template>
    </BaseModal>

    <ServicoProdutosModal :servico="produtosDe" @close="produtosDe = null" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { servicosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { useDebouncedWatch } from '@/composables/useDebouncedWatch'
import { deleteErrorMessage, errorMessage, fieldErrors } from '@/utils/errors'
import { formatMoney } from '@/utils/format'
import { CONFIGURACOES, toOptions } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'
import ServicoProdutosModal from './servicos/ServicoProdutosModal.vue'

const configuracaoOptions = toOptions(CONFIGURACOES)
const ui = useUiStore()
const busca = ref('')
const produtosDe = ref(null)

const list = usePagedList((params) =>
  busca.value.trim()
    ? servicosApi.buscarPorDescricao(busca.value.trim(), { ...params, sort: 'descricao' })
    : servicosApi.listar(params)
)
useDebouncedWatch(busca, () => list.reset())
onMounted(() => list.load())

const empty = () => ({ id: null, descricao: '', configuracao: '', preco: null })
const form = ref(empty())
const formOpen = ref(false)
const saving = ref(false)
const errors = ref({})

function openForm(servico) {
  form.value = servico ? { ...servico } : empty()
  errors.value = {}
  formOpen.value = true
}

function validate() {
  const e = {}
  if (!form.value.descricao.trim()) e.descricao = 'Informe a descrição.'
  if (!form.value.configuracao) e.configuracao = 'Selecione o tipo de veículo.'
  if (typeof form.value.preco !== 'number' || form.value.preco <= 0) e.preco = 'O preço deve ser maior que zero.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    descricao: form.value.descricao.trim(),
    configuracao: form.value.configuracao,
    preco: form.value.preco
  }
  try {
    if (form.value.id) {
      await servicosApi.atualizar(form.value.id, body)
      ui.success('Serviço atualizado.')
    } else {
      await servicosApi.criar(body)
      ui.success('Serviço cadastrado.')
    }
    formOpen.value = false
    list.reload()
  } catch (e) {
    errors.value = fieldErrors(e)
    ui.error(errorMessage(e))
  } finally {
    saving.value = false
  }
}

async function remove(servico) {
  const ok = await ui.confirm({
    title: 'Excluir serviço',
    message: `Excluir "${servico.descricao}"? Os vínculos com produtos também serão removidos.`,
    confirmText: 'Excluir',
    danger: true
  })
  if (!ok) return
  try {
    await servicosApi.excluir(servico.id)
    ui.success('Serviço excluído.')
    list.reload()
  } catch (e) {
    ui.error(deleteErrorMessage(e, 'serviço'))
  }
}
</script>
