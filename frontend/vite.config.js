import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// Em desenvolvimento o Vite repassa /api para o backend Spring Boot,
// assim o navegador nao faz requisicao cross-origin e nao precisa de CORS.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backend = env.BACKEND_URL || 'http://localhost:8080'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 5173,
      proxy: {
        '/api': { target: backend, changeOrigin: true }
      }
    },
    preview: {
      port: 4173,
      proxy: {
        '/api': { target: backend, changeOrigin: true }
      }
    }
  }
})
