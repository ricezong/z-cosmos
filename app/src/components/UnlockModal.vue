<template>
  <div v-if="visible" class="unlock-modal-overlay" @click.self="close">
    <div class="unlock-modal">
      <div class="modal-header">
        <h3>解锁全文</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      
      <div class="modal-body">
        <div class="step-tips">
          <p class="step">步骤 1：复制下方口令</p>
          <div class="code-display">{{ unlockCode }}</div>
          <button class="copy-btn" @click="copyCode">复制口令</button>
        </div>
        
        <div class="step-tips">
          <p class="step">步骤 2：扫码关注公众号</p>
          <img :src="qrCodeUrl" alt="微信公众号二维码" class="qr-code" />
        </div>
        
        <div class="step-tips">
          <p class="step">步骤 3：发送口令到公众号</p>
          <p class="hint">发送后等待3-5秒，内容将自动解锁</p>
        </div>
        
        <div class="loading-indicator" v-if="polling">
          <LoadingSpinner size="small" />
          <span>等待解锁中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import LoadingSpinner from './LoadingSpinner.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  qrCodeUrl: {
    type: String,
    required: true
  },
  unlockCode: {
    type: String,
    required: true
  },
  polling: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'close'])

function close() {
  emit('update:visible', false)
  emit('close')
}

function copyCode() {
  navigator.clipboard.writeText(props.unlockCode).then(() => {
    // 可以使用toast提示
    alert('口令已复制')
  }).catch(err => {
    console.error('复制失败:', err)
  })
}
</script>

<style scoped>
.unlock-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.unlock-modal {
  background: white;
  border-radius: 12px;
  max-width: 400px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.step-tips {
  margin-bottom: 20px;
}

.step {
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  font-size: 14px;
}

.code-display {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  letter-spacing: 8px;
  color: #1890ff;
  margin-bottom: 10px;
  font-family: monospace;
}

.copy-btn {
  width: 100%;
  padding: 10px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.copy-btn:hover {
  background: #40a9ff;
}

.qr-code {
  width: 200px;
  height: 200px;
  display: block;
  margin: 10px auto;
  border-radius: 8px;
}

.hint {
  color: #999;
  font-size: 12px;
  text-align: center;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px;
  color: #1890ff;
  font-size: 14px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
