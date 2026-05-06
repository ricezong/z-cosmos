import { ref } from 'vue'

const toasts = ref([])
let nextId = 0

function addToast(type, message, duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, type, message })
  if (duration > 0) {
    setTimeout(() => removeToast(id), duration)
  }
  return id
}

function removeToast(id) {
  const idx = toasts.value.findIndex(t => t.id === id)
  if (idx !== -1) toasts.value.splice(idx, 1)
}

export function useToast() {
  return {
    toasts,
    success(msg, duration) { return addToast('success', msg, duration) },
    error(msg, duration) { return addToast('error', msg, duration) },
    warning(msg, duration) { return addToast('warning', msg, duration) },
    info(msg, duration) { return addToast('info', msg, duration) },
    remove: removeToast
  }
}
