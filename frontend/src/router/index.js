import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { setUnauthorizedHandler } from '@/api/http'
import AppLayout from '@/components/AppLayout.vue'

// meta.gestor = tela exclusiva do Gestor (mesmas regras do SecurityConfig do backend)
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', name: 'inicio', component: () => import('@/views/DashboardView.vue'), meta: { title: 'Início' } },
      { path: 'agendamentos', name: 'agendamentos', component: () => import('@/views/AgendamentosView.vue'), meta: { title: 'Agendamentos' } },
      { path: 'ordens-servico', name: 'ordens', component: () => import('@/views/OrdensServicoView.vue'), meta: { title: 'Ordens de serviço' } },
      { path: 'clientes', name: 'clientes', component: () => import('@/views/ClientesView.vue'), meta: { title: 'Clientes' } },
      { path: 'servicos', name: 'servicos', component: () => import('@/views/ServicosView.vue'), meta: { title: 'Serviços' } },
      { path: 'produtos', name: 'produtos', component: () => import('@/views/ProdutosView.vue'), meta: { title: 'Produtos' } },
      { path: 'pagamentos', name: 'pagamentos', component: () => import('@/views/PagamentosView.vue'), meta: { title: 'Pagamentos' } },
      { path: 'relatorios', name: 'relatorios', component: () => import('@/views/RelatoriosView.vue'), meta: { title: 'Relatórios', gestor: true } },
      { path: 'funcionarios', name: 'funcionarios', component: () => import('@/views/FuncionariosView.vue'), meta: { title: 'Funcionários', gestor: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    return auth.isAuthenticated && to.name === 'login' ? { name: 'inicio' } : true
  }
  if (!auth.isAuthenticated) {
    auth.sair()
    return { name: 'login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if (to.meta.gestor && !auth.isGestor) {
    useUiStore().error('Esta área é exclusiva do Gestor.')
    return { name: 'inicio' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · Maverick Shift` : 'Maverick Shift'
})

// token expirado ou invalido: volta para o login
setUnauthorizedHandler(() => {
  const auth = useAuthStore()
  if (!auth.session) return
  auth.sair()
  useUiStore().error('Sua sessão expirou. Entre novamente.')
  router.push({ name: 'login' })
})

export default router
