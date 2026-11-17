<template>
  <BaseModal
    :open="!!servico"
    :title="servico?.descricao ?? ''"
    subtitle="Produtos consumidos a cada execução. A baixa no estoque acontece ao concluir a ordem de serviço."
    size="lg"
    @close="$emit('close')"
  >
    <form class="add-row" novalidate @submit.prevent="add">
      <div class="field grow">
        <label for="vinculo-produto">Produto</label>
        <select id="vinculo-produto" v-model="novo.produtoId" class="input">
          <option value="" disabled>Selecione um produto</option>
          <option v-for="p in produtosDisponiveis" :key="p.id" :value="p.id">
            {{ p.nome }} — {{ formatNumber(p.estoque) }} {{ UNIDADES_CURTAS[p.unidadeMedida] }} em estoque
          </option>
        </select>
      </div>
      <div class="field qty">
        <label for="vinculo-qtd">Quantidade</label>
        <input id="vinculo-qtd" v-model.number="novo.quantidade" type="number" min="1" step="1" class="input" />
      </div>
      <button class="btn btn-primary" :disabled="adding"><AppIcon name="plus" /> Vincular</button>
    </form>

    <div v-if="erro" class="alert alert-error">{{ erro }}</div>

    <table class="table bordered">
      <thead>
        <tr><th>Produto</th><th class="num">Qtd. necessária</th><th class="actions">Ações</th></tr>
      </thead>
      <tbody>
        <TableState :colspan="3" :loading="loading" :empty="!itens.length" empty-text="Nenhum produto vinculado." icon="box" />
        <template v-if="!loading">
          <tr v-for="item in itens" :key="item.id">
            <td>
              <strong>{{ item.produto.nome }}</strong>
              <span class="cell-sub">{{ item.produto.categoria }}</span>
            </td>
            <td class="num">
              <input
                v-if="editando === item.id"
                v-model.number="quantidadeEditada"
                type="number"
                min="1"
                step="1"
                class="input qty-input"
                aria-label="Nova quantidade"
                @keyup.enter="saveQuantidade(item)"
              />
              <span v-else>{{ item.quantidadeNecessaria }} {{ UNIDADES_CURTAS[item.produto.unidadeMedida] }}</span>
            </td>
            <td class="actions">
              <template v-if="editando === item.id">
                <button class="btn btn-sm btn-primary" @click="saveQuantidade(item)"><AppIcon name="check" :size="15" /></button>
                <button class="btn btn-sm" @click="editando = null"><AppIcon name="x" :size="15" /></button>
              </template>
              <template v-else>
                <button class="btn btn-sm" @click="startEdit(item)"><AppIcon name="edit" :size="15" /> Quantidade</button>
                <button class="btn btn-sm btn-danger" @click="remove(item)"><AppIcon name="trash" :size="15" /></button>
              </template>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </BaseModal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { produtosApi, servicosApi } from '@/api'
import { useUiStore } from '@/stores/ui'
import { contentOf } from '@/utils/page'
import { errorMessage } from '@/utils/errors'
import { formatNumber } from '@/utils/format'
import { UNIDADES_CURTAS } from '@/utils/labels'
import AppIcon from '@/components/AppIcon.vue'
import BaseModal from '@/components/BaseModal.vue'
import TableState from '@/components/TableState.vue'

const props = defineProps({ servico: { type: Object, default: null } })
defineEmits(['close'])

const ui = useUiStore()
const itens = ref([])
const produtos = ref([])
const loading = ref(false)
const adding = ref(false)
const erro = ref('')
const novo = ref({ produtoId: '', quantidade: 1 })
const editando = ref(null)
const quantidadeEditada = ref(1)

// nao oferece produto que ja esta vinculado
const produtosDisponiveis = computed(() => {
  const vinculados = new Set(itens.value.map((i) => i.produto.id))
  return produtos.value.filter((p) => !vinculados.has(p.id))
})

async function loadItens() {
  itens.value = await servicosApi.listarProdutos(props.servico.id)
}

watch(
  () => props.servico,
  async (servico) => {
    if (!servico) return
    loading.value = true
    erro.value = ''
    editando.value = null
    novo.value = { produtoId: '', quantidade: 1 }
    try {
      const [, todos] = await Promise.all([loadItens(), produtosApi.todos('nome')])
      produtos.value = contentOf(todos)
    } catch (e) {
      erro.value = errorMessage(e, 'Não foi possível carregar os produtos.')
    } finally {
      loading.value = false
    }
  }
)

const isQuantidadeValida = (q) => Number.isInteger(q) && q > 0

async function add() {
  if (!novo.value.produtoId) {
    erro.value = 'Selecione um produto.'
    return
  }
  if (!isQuantidadeValida(novo.value.quantidade)) {
    erro.value = 'A quantidade deve ser um número inteiro maior que zero.'
    return
  }
  adding.value = true
  erro.value = ''
  try {
    await servicosApi.adicionarProduto(props.servico.id, novo.value.produtoId, novo.value.quantidade)
    novo.value = { produtoId: '', quantidade: 1 }
    await loadItens()
    ui.success('Produto vinculado ao serviço.')
  } catch (e) {
    erro.value = errorMessage(e)
  } finally {
    adding.value = false
  }
}

function startEdit(item) {
  editando.value = item.id
  quantidadeEditada.value = item.quantidadeNecessaria
}

async function saveQuantidade(item) {
  if (!isQuantidadeValida(quantidadeEditada.value)) {
    erro.value = 'A quantidade deve ser um número inteiro maior que zero.'
    return
  }
  try {
    await servicosApi.atualizarProduto(props.servico.id, item.id, quantidadeEditada.value)
    editando.value = null
    erro.value = ''
    await loadItens()
    ui.success('Quantidade atualizada.')
  } catch (e) {
    erro.value = errorMessage(e)
  }
}

async function remove(item) {
  const ok = await ui.confirm({
    title: 'Desvincular produto',
    message: `Remover "${item.produto.nome}" deste serviço?`,
    confirmText: 'Remover',
    danger: true
  })
  if (!ok) return
  try {
    await servicosApi.removerProduto(props.servico.id, item.id)
    await loadItens()
    ui.success('Produto desvinculado.')
  } catch (e) {
    erro.value = errorMessage(e)
  }
}
</script>

<style scoped>
.add-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.grow {
  flex: 1;
  min-width: 220px;
}
.qty {
  width: 120px;
}
.qty-input {
  width: 90px;
  height: 30px;
  text-align: right;
}
.alert {
  margin-bottom: 12px;
}
.bordered {
  border: 1px solid var(--border);
}
</style>
