import { ref } from 'vue'

const confirmState = ref({ visible: false, title: '', message: '', resolve: null })

function showConfirm(title, message) {
  return new Promise(resolve => {
    confirmState.value = { visible: true, title, message, resolve }
  })
}

function confirmOk() {
  confirmState.value.resolve?.(true)
  confirmState.value.visible = false
}

function confirmCancel() {
  confirmState.value.resolve?.(false)
  confirmState.value.visible = false
}

export function useConfirm() {
  return { confirmState, showConfirm, confirmOk, confirmCancel }
}
