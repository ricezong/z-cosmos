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
.toast-item.success { background: var(--status-success); border: 1px solid var(--status-success-border); box-shadow: 0 8px 30px rgba(0,0,0,0.4), 0 0 20px var(--status-success-glow); }
.toast-item.error   { background: var(--status-error); border: 1px solid var(--status-error-border); box-shadow: 0 8px 30px rgba(0,0,0,0.4), 0 0 20px var(--status-error-glow); }
.toast-item.warning { background: var(--status-warning); border: 1px solid var(--status-warning-border); box-shadow: 0 8px 30px rgba(0,0,0,0.4), 0 0 20px var(--status-warning-glow); }
.toast-item.info    { background: var(--status-info); border: 1px solid var(--status-info-border); box-shadow: 0 8px 30px rgba(0,0,0,0.4), 0 0 20px var(--status-info-glow); }

.toast-enter-active { animation: toastIn 0.35s cubic-bezier(0.19,1,0.22,1); }
.toast-leave-active { animation: toastOut 0.25s ease-in forwards; }
@keyframes toastIn { from { opacity: 0; transform: translateY(-16px) scale(0.92); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes toastOut { to { opacity: 0; transform: translateY(-10px) scale(0.95); } }
</style>
