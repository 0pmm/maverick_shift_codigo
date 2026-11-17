import axios from 'axios'

const TOKEN_KEY = 'maverick.auth'

// VITE_API_URL permite apontar para um backend em outro endereco (precisa de CORS no backend).
// Sem ela, usa /api e o proxy do Vite repassa para o Spring Boot.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 15000
})

let onUnauthorized = () => {}

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

export function readStoredSession() {
  try {
    const raw = localStorage.getItem(TOKEN_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function storeSession(session) {
  try {
    if (session) {
      localStorage.setItem(TOKEN_KEY, JSON.stringify(session))
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
  } catch {
    // navegador sem localStorage (modo privado): a sessao vale so enquanto a aba estiver aberta
  }
}

http.interceptors.request.use((config) => {
  const session = readStoredSession()
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLogin = error.config?.url?.includes('/auth/login')
    if (error.response?.status === 401 && !isLogin) {
      onUnauthorized()
    }
    return Promise.reject(error)
  }
)

export default http
