import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api'
import { readStoredSession, storeSession } from '@/api/http'

// login do usuario vem no "sub" do JWT
function loginFromToken(token) {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(payload)).sub ?? null
  } catch {
    return null
  }
}

function isExpired(session) {
  return !session?.expiraEm || new Date(session.expiraEm).getTime() <= Date.now()
}

export const useAuthStore = defineStore('auth', () => {
  const stored = readStoredSession()
  const session = ref(stored && !isExpired(stored) ? stored : null)

  const isAuthenticated = computed(() => !!session.value && !isExpired(session.value))
  const isGestor = computed(() => session.value?.cargo === 'GESTOR')
  const nome = computed(() => session.value?.nome ?? '')
  const cargo = computed(() => session.value?.cargo ?? '')
  const login = computed(() => session.value?.login ?? '')

  async function entrar(usuario, senha) {
    const resposta = await authApi.login(usuario, senha)
    const nova = { ...resposta, login: loginFromToken(resposta.token) }
    storeSession(nova)
    session.value = nova
  }

  function sair() {
    storeSession(null)
    session.value = null
  }

  return { session, isAuthenticated, isGestor, nome, cargo, login, entrar, sair }
})
