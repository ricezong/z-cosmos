<template>
  <Teleport to="body">
    <Transition name="confirm">
      <div class="confirm-overlay" v-if="confirmState.visible" @click.self="confirmCancel">
        <div class="confirm-modal">
          <div class="confirm-icon"><i class="ri-question-line"></i></div>
          <h3 v-if="confirmState.title">{{ confirmState.title }}</h3>
          <p>{{ confirmState.message }}</p>
          <div class="confirm-btns">
            <button class="confirm-cancel" @click="confirmCancel">取消</button>
            <button class="confirm-ok" @click="confirmOk">确定</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { useConfirm } from '../composables/useConfirm'
const { confirmState, confirmOk, confirmCancel } = useConfirm()
</script>

<style scoped>
.confirm-overlay {
  position: fixed; inset: 0;
  background: rgba(5,8,20,0.8);
  z-index: 9998;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(4px);
}
.confirm-modal {
  background: rgba(18,24,48,0.96);
  border: 1px solid rgba(144,166,196,0.3);
  border-radius: 20px;
  padding: 32px 36px;
  min-width: 320px; max-width: 90vw;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}
.confirm-icon { font-size: 2.5rem; color: rgba(240,200,120,0.85); margin-bottom: 12px; }
.confirm-modal h3 { font-size: 1.1rem; font-weight: 500; color: #e8eef7; margin-bottom: 8px; }
.confirm-modal p { font-size: 0.9rem; color: rgba(255,255,255,0.65); margin-bottom: 24px; line-height: 1.6; }
.confirm-btns { display: flex; gap: 12px; justify-content: center; }
.confirm-cancel, .confirm-ok {
  padding: 10px 28px; border-radius: 24px; cursor: pointer; font-size: 0.9rem; transition: 0.25s;
}
.confirm-cancel {
  border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.05); color: #a8bcd4;
}
.confirm-cancel:hover { background: rgba(255,255,255,0.1); }
.confirm-ok {
  border: none; background: linear-gradient(135deg, #7890b5, #a8bcd4); color: #fff; font-weight: 500;
}
.confirm-ok:hover { box-shadow: 0 4px 18px rgba(144,166,196,0.45); }

.confirm-enter-active { animation: confirmIn 0.3s cubic-bezier(0.19,1,0.22,1); }
.confirm-leave-active { animation: confirmOut 0.2s ease-in forwards; }
@keyframes confirmIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes confirmIn { from { opacity: 0; } .confirm-modal { transform: scale(0.9); } }
@keyframes confirmOut { to { opacity: 0; } }
</style>
