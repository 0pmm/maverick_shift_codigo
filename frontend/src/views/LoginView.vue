<template>
  <div class="login-page">
    <section class="panel-brand">
      <div class="brand">
        <span class="brand-mark">M</span>
        <strong>Maverick Shift</strong>
      </div>
      <div class="pitch">
        <h1>A oficina inteira<br />em um só lugar.</h1>
        <p>Agendamentos, ordens de serviço, estoque e pagamentos conectados do começo ao fim.</p>
      </div>
      <ul class="steps">
        <li><span>01</span> Agende o cliente</li>
        <li><span>02</span> Abra e execute a OS</li>
        <li><span>03</span> Baixe o estoque e receba</li>
      </ul>
    </section>

    <section class="panel-form">
      <form class="form" novalidate @submit.prevent="submit">
        <h2>Entrar</h2>
        <p class="muted">Use o login e a senha cadastrados pelo gestor.</p>

        <div v-if="erro" class="alert alert-error">{{ erro }}</div>

        <FormField v-slot="{ id }" label="Login">
          <input :id="id" v-model.trim="login" class="input" autocomplete="username" autofocus required />
        </FormField>
        <FormField v-slot="{ id }" label="Senha">
          <input :id="id" v-model="senha" class="input" type="password" autocomplete="current-password" required />
        </FormField>

        <button class="btn btn-primary submit" :disabled="loading">
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/utils/errors'
import FormField from '@/components/FormField.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const login = ref('')
const senha = ref('')
const erro = ref('')
const loading = ref(false)

async function submit() {
  if (!login.value || !senha.value) {
    erro.value = 'Informe o login e a senha.'
    return
  }
  loading.value = true
  erro.value = ''
  try {
    await auth.entrar(login.value, senha.value)
    const destino = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(destino.startsWith('/') ? destino : '/')
  } catch (e) {
    erro.value = errorMessage(e, 'Não foi possível entrar.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  min-height: 100vh;
}
.panel-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px 48px;
  background:
    repeating-linear-gradient(135deg, rgba(255, 255, 255, 0.025) 0 2px, transparent 2px 14px),
    var(--sidebar);
  color: #d1d5db;
  overflow: hidden;
}
.panel-brand::after {
  content: '';
  position: absolute;
  right: -120px;
  bottom: -120px;
  width: 360px;
  height: 360px;
  border: 46px solid var(--accent-bright);
  border-radius: 50%;
  opacity: 0.9;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
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
}
.pitch h1 {
  color: #fff;
  font-size: clamp(30px, 3.6vw, 46px);
  font-weight: 750;
  letter-spacing: -0.025em;
}
.pitch p {
  max-width: 420px;
  margin: 16px 0 0;
  font-size: 16px;
  color: #9ca3af;
}
.steps {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
  font-weight: 500;
}
.steps span {
  display: inline-block;
  width: 32px;
  color: var(--accent-bright);
  font-family: var(--mono);
}
.panel-form {
  display: grid;
  place-items: center;
  padding: 32px 20px;
  background: var(--surface);
}
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 360px;
}
.form h2 {
  font-size: 26px;
  font-weight: 700;
}
.form > p {
  margin: -10px 0 4px;
}
.submit {
  height: 42px;
  margin-top: 4px;
  font-size: 14.5px;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .panel-brand {
    gap: 28px;
    padding: 28px 20px;
  }
  .panel-brand::after,
  .steps {
    display: none;
  }
}
</style>
