<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Produtos</h1>
        <p>Itens em estoque usados na execução dos serviços.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()"><AppIcon name="plus" /> Novo produto</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <input v-model="busca" class="input input-search" placeholder="Buscar por nome" aria-label="Buscar por nome" />
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Categoria</th>
              <th class="num">Estoque</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="4"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum produto encontrado."
              icon="box"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="p in list.items.value" :key="p.id">
                <td>
                  <strong>{{ p.nome }}</strong>
                  <span class="cell-sub">{{ UNIDADES[p.unidadeMedida] ?? p.unidadeMedida }}</span>
                </td>
                <td>{{ p.categoria }}</td>
                <td class="num">
                  <span :class="{ low: p.estoque <= ESTOQUE_BAIXO }">
                    {{ formatNumber(p.estoque) }} {{ UNIDADES_CURTAS[p.unidadeMedida] }}
                  </span>
                </td>
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

    <BaseModal :open="formOpen" :title="form.id ? 'Editar produto' : 'Novo produto'" @close="formOpen = false">
      <form id="produto-form" class="form-grid" novalidate @submit.prevent="save">
        <FormField v-slot="{ id, invalid }" label="Nome" required full :error="errors.nome">
          <input :id="id" v-model="form.nome" class="input" :class="{ 'has-error': invalid }" maxlength="100" placeholder="Óleo 5W30" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Categoria" required :error="errors.categoria">
          <input :id="id" v-model="form.categoria" class="input" :class="{ 'has-error': invalid }" maxlength="50" placeholder="Lubrificantes" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Unidade de medida" required :error="errors.unidadeMedida">
          <select :id="id" v-model="form.unidadeMedida" class="input" :class="{ 'has-error': invalid }">
            <option value="" disabled>Selecione</option>
            <option v-for="o in unidadeOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Estoque atual" required :error="errors.estoque">
          <input :id="id" v-model.number="form.estoque" type="number" min="0" step="0.01" class="input" :class="{ 'has-error': invalid }" />
        </FormField>
      </form>
      <template #footer>
        <button class="btn" @click="formOpen = false">Cancelar</button>
        <button class="btn btn-primary" form="produto-form" :disabled="saving">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { produtosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { useDebouncedWatch } from '@/composables/useDebouncedWatch'
import { deleteErrorMessage, errorMessage, fieldErrors } from '@/utils/errors'
import { formatNumber } from '@/utils/format'
import { UNIDADES, UNIDADES_CURTAS, toOptions } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'

const ESTOQUE_BAIXO = 5
const unidadeOptions = toOptions(UNIDADES)
const ui = useUiStore()
const busca = ref('')

const list = usePagedList((params) =>
  busca.value.trim()
    ? produtosApi.buscarPorNome(busca.value.trim(), { ...params, sort: 'nome' })
    : produtosApi.listar(params)
)
useDebouncedWatch(busca, () => list.reset())
onMounted(() => list.load())

const empty = () => ({ id: null, nome: '', categoria: '', unidadeMedida: '', estoque: 0 })
const form = ref(empty())
const formOpen = ref(false)
const saving = ref(false)
const errors = ref({})

function openForm(produto) {
  form.value = produto ? { ...produto } : empty()
  errors.value = {}
  formOpen.value = true
}

function validate() {
  const e = {}
  if (form.value.nome.trim().length < 2) e.nome = 'Informe o nome (mínimo 2 caracteres).'
  if (!form.value.categoria.trim()) e.categoria = 'Informe a categoria.'
  if (!form.value.unidadeMedida) e.unidadeMedida = 'Selecione a unidade.'
  if (typeof form.value.estoque !== 'number' || form.value.estoque < 0) e.estoque = 'O estoque não pode ser negativo.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    nome: form.value.nome.trim(),
    categoria: form.value.categoria.trim(),
    unidadeMedida: form.value.unidadeMedida,
    estoque: form.value.estoque
  }
  try {
    if (form.value.id) {
      await produtosApi.atualizar(form.value.id, body)
      ui.success('Produto atualizado.')
    } else {
      await produtosApi.criar(body)
      ui.success('Produto cadastrado.')
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

async function remove(produto) {
  const ok = await ui.confirm({
    title: 'Excluir produto',
    message: `Excluir "${produto.nome}"? Esta ação não pode ser desfeita.`,
    confirmText: 'Excluir',
    danger: true
  })
  if (!ok) return
  try {
    await produtosApi.excluir(produto.id)
    ui.success('Produto excluído.')
    list.reload()
  } catch (e) {
    ui.error(deleteErrorMessage(e, 'produto'))
  }
}
</script>

<style scoped>
.low {
  color: var(--danger);
  font-weight: 600;
}
</style>
