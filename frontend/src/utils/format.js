const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const numero = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 })

export function formatMoney(value) {
  return value == null ? '—' : moeda.format(value)
}

export function formatNumber(value) {
  return value == null ? '—' : numero.format(value)
}

// O backend usa LocalDateTime sem fuso ("2026-11-10T14:30:00"), entao a data e montada como horario local
function parseLocal(value) {
  if (!value) return null
  const [datePart, timePart = '00:00:00'] = String(value).split('T')
  const [y, m, d] = datePart.split('-').map(Number)
  const [hh = 0, mm = 0, ss = 0] = timePart.split(':').map((p) => Number.parseFloat(p))
  return new Date(y, m - 1, d, hh, mm, Math.floor(ss))
}

export function formatDate(value) {
  const date = parseLocal(value)
  return date ? date.toLocaleDateString('pt-BR') : '—'
}

export function formatDateTime(value) {
  const date = parseLocal(value)
  if (!date) return '—'
  return date.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatTime(value) {
  const date = parseLocal(value)
  return date ? date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '—'
}

const pad = (n) => String(n).padStart(2, '0')

// "2026-11-16" no horario local (toISOString usaria UTC e poderia virar o dia)
export function toIsoDate(date = new Date()) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

// valor para <input type="datetime-local">: "2026-11-16T14:30"
export function toInputDateTime(value) {
  const date = value instanceof Date ? value : parseLocal(value)
  if (!date) return ''
  return `${toIsoDate(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

// do <input type="datetime-local"> para o LocalDateTime do backend
export function fromInputDateTime(value) {
  if (!value) return null
  return value.length === 16 ? `${value}:00` : value
}

export function firstDayOfMonth(date = new Date()) {
  return toIsoDate(new Date(date.getFullYear(), date.getMonth(), 1))
}

export function lastDayOfMonth(date = new Date()) {
  return toIsoDate(new Date(date.getFullYear(), date.getMonth() + 1, 0))
}

export function shortId(id) {
  return id ? `#${String(id).slice(0, 8).toUpperCase()}` : '—'
}
