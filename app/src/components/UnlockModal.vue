<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      
      <h2>🔐 获取解锁口令</h2>
      
      <div v-if="step === 1" class="step step-1">
        <p class="instruction">
          1. 扫描下方二维码或搜索公众号<br>
          2. 发送任意消息获取 6 位口令<br>
          3. 在下方输入口令完成解锁
        </p>
        
        <div class="qr-code">
          <!-- 这里可以放公众号二维码 -->
          <div class="qr-placeholder">公众号二维码</div>
        </div>
        
        <button @click="goToStep2" class="next-btn">我已关注公众号</button>
      </div>
      
      <div v-if="step === 2" class="step step-2">
        <div class="input-group">
          <label>输入 6 位口令</label>
          <input 
            type="text" 
            v-model="unlockCode"
            maxlength="6"
            placeholder="请输入 6 位数字"
            class="code-input"
          />
        </div>
        
        <button 
          @click="submitCode" 
          :disabled="!isValidCode || submitting"
          class="submit-btn"
        >
          {{ submitting ? '验证中...' : '确认解锁' }}
        </button>
        
        <p v-if="error" class="error">{{ error }}</p>
      </div>
      
      <div v-if="step === 3" class="step step-3 success">
        <div class="success-icon">✓</div>
        <h3>解锁成功！</h3>
        <p>您已解锁全站内容，有效期 12 小时</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { validateUnlockCode } from '../api/auth'
import { getDeviceId } from '../utils/device'

const emit = defineEmits(['close', 'unlocked'])

const step = ref(1)
const unlockCode = ref('')
const submitting = ref(false)
const error = ref('')

const isValidCode = computed(() => {
  return /^\d{6}$/.test(unlockCode.value)
})

function goToStep2() {
  step.value = 2
}

async function submitCode() {
  if (!isValidCode.value) return
  
  submitting.value = true
  error.value = ''
  
  try {
    const deviceId = getDeviceId()
    const res = await validateUnlockCode(deviceId, unlockCode.value)
    
    if (res.code === 0 && res.data?.success) {
      step.value = 3
      setTimeout(() => {
        emit('unlocked')
      }, 1500)
    } else {
      error.value = res.message || '口令无效或已过期'
    }
  } catch (e) {
    error.value = '网络错误，请重试'
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
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
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 40px;
  max-width: 450px;
  width: 90%;
  position: relative;
  text-align: center;
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #999;
}

h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.instruction {
  color: #666;
  line-height: 1.8;
  margin-bottom: 25px;
}

.qr-code {
  margin: 25px 0;
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  background: #f5f5f5;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  border-radius: 8px;
}

.next-btn, .submit-btn {
  background: #4a90d9;
  color: white;
  border: none;
  padding: 14px 40px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 15px;
}

.next-btn:hover, .submit-btn:hover {
  background: #3a7bc8;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.input-group {
  margin: 25px 0;
}

.input-group label {
  display: block;
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.code-input {
  width: 100%;
  padding: 15px;
  font-size: 24px;
  text-align: center;
  letter-spacing: 8px;
  border: 2px solid #ddd;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.3s;
}

.code-input:focus {
  border-color: #4a90d9;
}

.error {
  color: #f44336;
  font-size: 14px;
  margin-top: 15px;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #4caf50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  margin: 0 auto 20px;
}

.success h3 {
  color: #4caf50;
  margin: 0 0 10px 0;
}

.success p {
  color: #666;
}
</style>
