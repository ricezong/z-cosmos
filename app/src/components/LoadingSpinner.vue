<template>
  <div class="loading-wrap" :class="{ overlay }">
    <div class="spinner">
      <div class="spinner-ring"></div>
      <div class="spinner-ring"></div>
      <div class="spinner-dot"></div>
    </div>
    <p class="loading-text" v-if="text">{{ text }}</p>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '' },
  overlay: { type: Boolean, default: false }
})
</script>

<style scoped>
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}
.loading-wrap.overlay {
  position: absolute;
  inset: 0;
  background: rgba(5, 8, 18, 0.6);
  backdrop-filter: blur(4px);
  z-index: 10;
  border-radius: inherit;
}
.spinner {
  position: relative;
  width: 40px;
  height: 40px;
}
.spinner-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: #a8bcd4;
  animation: spin 1s linear infinite;
}
.spinner-ring:nth-child(2) {
  inset: 6px;
  border-top-color: #7890b5;
  animation-duration: 1.5s;
  animation-direction: reverse;
}
.spinner-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 6px;
  height: 6px;
  margin: -3px 0 0 -3px;
  border-radius: 50%;
  background: #c5d5ea;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}
.loading-text {
  font-size: 0.82rem;
  color: #a8bcd4;
  opacity: 0.7;
  letter-spacing: 1px;
}
</style>