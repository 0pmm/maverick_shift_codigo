<template>
  <div class="shell" :class="{ 'menu-open': menuOpen }">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">M</span>
        <div>
          <strong>Maverick Shift</strong>
          <small>Gestão da oficina</small>
        </div>
      </div>

      <nav class="nav">
        <template v-for="group in visibleGroups" :key="group.title">
          <span class="nav-group">{{ group.title }}</span>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :exact-active-class="item.exact ? 'active' : ''"
            :active-class="item.exact ? '' : 'active'"
            @click="menuOpen = false"
          >
            <AppIcon :name="item.icon" />
            {{ item.label }}
          </RouterLink>
        </template>
      </nav>

      <div class="user">
        <span class="avatar">{{ initials }}</span>
        <div class="user-info">
          <strong>{{ auth.nome }}</strong>
          <small>{{ CARGOS[auth.cargo] ?? auth.cargo }}</small>
        </div>
        <button class="btn btn-ghost btn-sm logout" title="Sair" @click="logout">
          <AppIcon name="logout" />
        </button>
      </div>
    </aside>

    <div class="backdrop" @click="menuOpen = false" />

    <div class="main">
      <header class="topbar">
        <button class="btn btn-ghost btn-sm menu-btn" aria-label="Abrir menu" @click="menuOpen = true">
          <AppIcon name="menu" :size="20" />
        </button>
        <span class="topbar-title">{{ route.meta.title }}</span>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </div>

    <ConfirmDialog />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { CARGOS } from '@/utils/labels'
import AppIcon from './AppIcon.vue'
import ConfirmDialog from './ConfirmDialog.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)

const groups = [
  {
    title: 'Operação',
    items: [
      { to: '/', label: 'Início', icon: 'home', exact: true },
      { to: '/agendamentos', label: 'Agendamentos', icon: 'calendar' },
      { to: '/ordens-servico', label: 'Ordens de serviço', icon: 'clipboard' },
      { to: '/pagamentos', label: 'Pagamentos', icon: 'card' }
    ]
  },
  {
    title: 'Cadastros',
    items: [
      { to: '/clientes', label: 'Clientes', icon: 'users' },
      { to: '/servicos', label: 'Serviços', icon: 'wrench' },
      { to: '/produtos', label: 'Produtos', icon: 'box' }
    ]
  },
  {
    title: 'Gestão',
    gestor: true,
    items: [
      { to: '/relatorios', label: 'Relatórios', icon: 'chart' },
      { to: '/funcionarios', label: 'Funcionários', icon: 'badge' }
    ]
  }
]

const visibleGroups = computed(() => groups.filter((g) => !g.gestor || auth.isGestor))

const initials = computed(() =>
  auth.nome
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')
)

function logout() {
  auth.sair()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 248px;
  display: flex;
  flex-direction: column;
  background: var(--sidebar);
  color: var(--sidebar-text);
  z-index: 30;
}
.brand {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 20px 18px 18px;
  border-bottom: 1px solid #23272f;
}
.brand strong {
  display: block;
  color: #fff;
  font-size: 15px;
  letter-spacing: 0.01em;
}
.brand small {
  font-size: 12px;
}
.brand-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--accent-bright);
  color: #14171d;
  font-weight: 800;
  font-size: 17px;
}
.nav {
  flex: 1;
  overflow-y: auto;
  padding: 10px 12px;
}
.nav-group {
  display: block;
  padding: 14px 10px 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6b7280;
}
.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  margin-bottom: 2px;
  border-radius: 7px;
  text-decoration: none;
  font-weight: 500;
  transition: background 0.15s, color 0.15s;
}
.nav-link:hover {
  background: var(--sidebar-hover);
  color: #e5e7eb;
}
.nav-link.active {
  background: #262a33;
  color: #fff;
}
.nav-link.active::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 7px;
  bottom: 7px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--accent-bright);
}
.nav-link.active svg {
  color: var(--accent-bright);
}
.user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 14px 14px 16px;
  border-top: 1px solid #23272f;
}
.avatar {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #2b303a;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.user-info strong {
  display: block;
  color: #fff;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-info small {
  font-size: 12px;
}
.logout {
  color: var(--sidebar-text);
}
.logout:hover {
  background: var(--sidebar-hover) !important;
  color: #fff;
}
.main {
  flex: 1;
  min-width: 0;
  margin-left: 248px;
}
.topbar {
  display: none;
}
.content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 28px 32px 48px;
}
.backdrop {
  display: none;
}

@media (max-width: 960px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.2s ease;
  }
  .menu-open .sidebar {
    transform: none;
  }
  .menu-open .backdrop {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(15, 18, 24, 0.45);
    z-index: 20;
  }
  .main {
    margin-left: 0;
  }
  .topbar {
    position: sticky;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    gap: 8px;
    height: 54px;
    padding: 0 12px;
    background: var(--surface);
    border-bottom: 1px solid var(--border);
  }
  .topbar-title {
    font-weight: 650;
  }
  .content {
    padding: 20px 16px 40px;
  }
}
</style>
