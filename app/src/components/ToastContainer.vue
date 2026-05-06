<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div v-for="t in toasts" :key="t.id" class="toast-item" :class="t.type" @click="remove(t.id)">
          <i :class="iconMap[t.type]"></i>
          <span>{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
import { useToast } from '../composables/useToast'
const { toasts, remove } = useToast()

const iconMap = {
  success: 'ri-check-line',
  error: 'ri-close-circle-line',
  warning: 'ri-alert-line',
  info: 'ri-information-line'
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
}
.toast-item {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  border-radius: 14px;
  font-size: 0.9rem;
  color: #fff;
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.4);
  cursor: pointer;
  white-space: nowrap;
  max-width: 90vw;
  overflow: hidden;
  text-overflow: ellipsis;
}
.toast-item i { font-size: 1.15rem; flex-shrink: 0; }
.toast-item.success { background: rgba(56,142,60,0.85); border: 1px solid rgba(76,175,80,0.5); }
.toast-item.error   { background: rgba(198,40,40,0.85); border: 1px solid rgba(229,57,53,0.5); }
.toast-item.warning { background: rgba(230,126,34,0.85); border: 1px solid rgba(243,156,18,0.5); }
.toast-item.info    { background: rgba(41,98,255,0.75); border: 1px solid rgba(66,133,244,0.5); }

.toast-enter-active { animation: toastIn 0.35s cubic-bezier(0.19,1,0.22,1); }
.toast-leave-active { animation: toastOut 0.25s ease-in forwards; }
@keyframes toastIn { from { opacity: 0; transform: translateY(-16px) scale(0.92); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes toastOut { to { opacity: 0; transform: translateY(-10px) scale(0.95); } }
</style>
