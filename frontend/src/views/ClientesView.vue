<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Clientes</h1>
        <p>Cadastro dos clientes da oficina.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()">
        <AppIcon name="plus" /> Novo cliente
      </button>
    </div>

    <div class="card">
      <div class="toolbar">
        <input v-model="busca" class="input input-search" placeholder="Buscar por nome" aria-label="Buscar por nome" />
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Telefone</th>
              <th>E-mail</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="4"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum cliente encontrado."
              icon="users"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="c in list.items.value" :key="c.id">
                <td><strong>{{ c.nome }}</strong></td>
                <td>{{ c.telefone }}</td>
                <td>{{ c.email || '—' }}</td>
                <td class="actions">
                  <button class="btn btn-sm" @click="openHistorico(c)"><AppIcon name="eye" :size="15" /> Histórico</button>
                  <button class="btn btn-sm" @click="openForm(c)"><AppIcon name="edit" :size="15" /> Editar</button>
                  <button class="btn btn-sm btn-danger" @click="remove(c)"><AppIcon name="trash" :size="15" /></button>
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

    <BaseModal :open="formOpen" :title="form.id ? 'Editar cliente' : 'Novo cliente'" @close="formOpen = false">
      <form id="cliente-form" class="form-grid" novalidate @submit.prevent="save">
        <FormField v-slot="{ id, invalid }" label="Nome" required full :error="errors.nome">
          <input :id="id" v-model="form.nome" class="input" :class="{ 'has-error': invalid }" maxlength="100" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Telefone" required :error="errors.telefone">
          <input :id="id" v-model="form.telefone" class="input" :class="{ 'has-error': invalid }" maxlength="20" placeholder="(11) 98765-4321" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="E-mail" :error="errors.email">
          <input :id="id" v-model="form.email" type="email" class="input" :class="{ 'has-error': invalid }" maxlength="100" />
        </FormField>
      </form>
      <template #footer>
        <button class="btn" @click="formOpen = false">Cancelar</button>
        <button class="btn btn-primary" form="cliente-form" :disabled="saving">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
      </template>
    </BaseModal>

    <ClienteHistoricoModal :cliente="historicoDe" @close="historicoDe = null" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { clientesApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { useDebouncedWatch } from '@/composables/useDebouncedWatch'
import { deleteErrorMessage, errorMessage, fieldErrors } from '@/utils/errors'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'
import ClienteHistoricoModal from './clientes/ClienteHistoricoModal.vue'

const ui = useUiStore()
const busca = ref('')

const list = usePagedList((params) =>
  busca.value.trim() ? clientesApi.buscarPorNome(busca.value.trim(), params) : clientesApi.listar(params)
)
useDebouncedWatch(busca, () => list.reset())
onMounted(() => list.load())

const empty = () => ({ id: null, nome: '', telefone: '', email: '' })
const form = ref(empty())
const formOpen = ref(false)
const saving = ref(false)
const errors = ref({})
const historicoDe = ref(null)

function openForm(cliente) {
  form.value = cliente ? { ...cliente, email: cliente.email ?? '' } : empty()
  errors.value = {}
  formOpen.value = true
}

function openHistorico(cliente) {
  historicoDe.value = cliente
}

function validate() {
  const e = {}
  if (form.value.nome.trim().length < 2) e.nome = 'Informe o nome (mínimo 2 caracteres).'
  if (!form.value.telefone.trim()) e.telefone = 'Informe o telefone.'
  if (form.value.email && !/^\S+@\S+\.\S+$/.test(form.value.email)) e.email = 'E-mail inválido.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    nome: form.value.nome.trim(),
    telefone: form.value.telefone.trim(),
    email: form.value.email.trim() || null
  }
  try {
    if (form.value.id) {
      await clientesApi.atualizar(form.value.id, body)
      ui.success('Cliente atualizado.')
    } else {
      await clientesApi.criar(body)
      ui.success('Cliente cadastrado.')
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

async function remove(cliente) {
  const ok = await ui.confirm({
    title: 'Excluir cliente',
    message: `Excluir "${cliente.nome}"? Esta ação não pode ser desfeita.`,
    confirmText: 'Excluir',
    danger: true
  })
  if (!ok) return
  try {
    await clientesApi.excluir(cliente.id)
    ui.success('Cliente excluído.')
    list.reload()
  } catch (e) {
    ui.error(deleteErrorMessage(e, 'cliente'))
  }
}
</script>
