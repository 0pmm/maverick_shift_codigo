<template>
  <div>
    <div class="page-header">
      <div>
        <h1>Funcionários</h1>
        <p>Quem acessa o sistema. Gestores também acessam relatórios e este cadastro.</p>
      </div>
      <button class="btn btn-primary" @click="openForm()"><AppIcon name="plus" /> Novo funcionário</button>
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
              <th>Login</th>
              <th>Perfil</th>
              <th class="actions">Ações</th>
            </tr>
          </thead>
          <tbody>
            <TableState
              :colspan="4"
              :loading="list.loading.value"
              :error="list.error.value"
              :empty="!list.items.value.length"
              empty-text="Nenhum funcionário encontrado."
              icon="badge"
              @retry="list.reload"
            />
            <template v-if="!list.loading.value">
              <tr v-for="u in list.items.value" :key="u.id">
                <td>
                  <strong>{{ u.nome }}</strong>
                  <span v-if="u.login === auth.login" class="cell-sub">Você</span>
                </td>
                <td class="mono">{{ u.login }}</td>
                <td>
                  <span class="cargo" :class="{ gestor: u.cargo === 'GESTOR' }">{{ CARGOS[u.cargo] ?? u.cargo }}</span>
                </td>
                <td class="actions">
                  <button class="btn btn-sm" @click="openForm(u)"><AppIcon name="edit" :size="15" /> Editar</button>
                  <button
                    class="btn btn-sm btn-danger"
                    :disabled="u.login === auth.login"
                    :title="u.login === auth.login ? 'Você não pode excluir o próprio usuário' : 'Excluir'"
                    @click="remove(u)"
                  >
                    <AppIcon name="trash" :size="15" />
                  </button>
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

    <BaseModal :open="formOpen" :title="form.id ? 'Editar funcionário' : 'Novo funcionário'" @close="formOpen = false">
      <form id="funcionario-form" class="form-grid" novalidate autocomplete="off" @submit.prevent="save">
        <FormField v-slot="{ id, invalid }" label="Nome" required full :error="errors.nome">
          <input :id="id" v-model="form.nome" class="input" :class="{ 'has-error': invalid }" maxlength="100" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Login" required :error="errors.login">
          <input :id="id" v-model="form.login" class="input" :class="{ 'has-error': invalid }" maxlength="50" autocomplete="off" />
        </FormField>
        <FormField v-slot="{ id, invalid }" label="Perfil" required :error="errors.cargo">
          <select :id="id" v-model="form.cargo" class="input" :class="{ 'has-error': invalid }">
            <option v-for="o in cargoOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </FormField>
        <FormField
          v-slot="{ id, invalid }"
          label="Senha"
          :required="!form.id"
          full
          :error="errors.senha"
          :hint="form.id ? 'Deixe em branco para manter a senha atual.' : ''"
        >
          <input :id="id" v-model="form.senha" type="password" class="input" :class="{ 'has-error': invalid }" autocomplete="new-password" />
        </FormField>
      </form>
      <template #footer>
        <button class="btn" @click="formOpen = false">Cancelar</button>
        <button class="btn btn-primary" form="funcionario-form" :disabled="saving">{{ saving ? 'Salvando...' : 'Salvar' }}</button>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { usuariosApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { usePagedList } from '@/composables/usePagedList'
import { useDebouncedWatch } from '@/composables/useDebouncedWatch'
import { deleteErrorMessage, errorMessage, fieldErrors } from '@/utils/errors'
import { CARGOS, toOptions } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import FormField from '@/components/FormField.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import TableState from '@/components/TableState.vue'

const cargoOptions = toOptions(CARGOS)
const auth = useAuthStore()
const ui = useUiStore()
const busca = ref('')

const list = usePagedList((params) =>
  busca.value.trim() ? usuariosApi.buscarPorNome(busca.value.trim(), params) : usuariosApi.listar(params)
)
useDebouncedWatch(busca, () => list.reset())
onMounted(() => list.load())

const empty = () => ({ id: null, nome: '', login: '', cargo: 'FUNCIONARIO', senha: '' })
const form = ref(empty())
const formOpen = ref(false)
const saving = ref(false)
const errors = ref({})

function openForm(usuario) {
  form.value = usuario ? { ...usuario, senha: '' } : empty()
  errors.value = {}
  formOpen.value = true
}

function validate() {
  const e = {}
  if (form.value.nome.trim().length < 2) e.nome = 'Informe o nome (mínimo 2 caracteres).'
  if (form.value.login.trim().length < 3) e.login = 'O login deve ter entre 3 e 50 caracteres.'
  if (!form.value.cargo) e.cargo = 'Selecione o perfil.'
  if (!form.value.id && !form.value.senha) e.senha = 'Informe a senha.'
  errors.value = e
  return Object.keys(e).length === 0
}

async function save() {
  if (!validate()) return
  saving.value = true
  const body = {
    nome: form.value.nome.trim(),
    login: form.value.login.trim(),
    cargo: form.value.cargo,
    senha: form.value.senha
  }
  try {
    if (form.value.id) {
      await usuariosApi.atualizar(form.value.id, body)
      ui.success('Funcionário atualizado.')
    } else {
      await usuariosApi.criar(body)
      ui.success('Funcionário cadastrado.')
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

async function remove(usuario) {
  const ok = await ui.confirm({
    title: 'Excluir funcionário',
    message: `Excluir "${usuario.nome}"? Ele perderá o acesso ao sistema.`,
    confirmText: 'Excluir',
    danger: true
  })
  if (!ok) return
  try {
    await usuariosApi.excluir(usuario.id)
    ui.success('Funcionário excluído.')
    list.reload()
  } catch (e) {
    ui.error(deleteErrorMessage(e, 'funcionário'))
  }
}
</script>

<style scoped>
.cargo {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 999px;
  background: var(--neutral-soft);
  font-size: 12px;
  font-weight: 600;
}
.cargo.gestor {
  background: var(--accent-soft);
  color: var(--accent);
}
</style>
