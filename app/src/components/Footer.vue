<template>
  <footer class="site-footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <h4>备案信息</h4>
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">
            {{ icpNumber }}
          </a>
          <a href="http://www.beian.gov.cn/" target="_blank" rel="noopener">
            {{ policeNumber }}
          </a>
        </div>
        
        <div class="footer-section">
          <h4>联系方式</h4>
          <p>邮箱：{{ contactEmail }}</p>
        </div>
        
        <div class="footer-section">
          <h4>关于本站</h4>
          <p>{{ copyright }}</p>
          <p class="disclaimer">
            本站为个人技术博客，所有内容均为站长个人发布
          </p>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>{{ copyright }}</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

// 这些值可以从后端API获取，或者使用环境变量
const icpNumber = ref(import.meta.env.VITE_ICP_NUMBER || '京ICP备XXXXXXXX号')
const policeNumber = ref(import.meta.env.VITE_POLICE_NUMBER || '京公网安备 XXXXXXXXX号')
const copyright = ref(import.meta.env.VITE_COPYRIGHT || '© 2026 Z-Cosmos. All rights reserved.')
const contactEmail = ref(import.meta.env.VITE_CONTACT_EMAIL || 'contact@example.com')
</script>

<style scoped>
.site-footer {
  background: #2c3e50;
  color: #ecf0f1;
  padding: 40px 20px 20px;
  margin-top: 60px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-bottom: 30px;
}

.footer-section h4 {
  color: #fff;
  margin: 0 0 15px;
  font-size: 16px;
}

.footer-section a {
  display: block;
  color: #3498db;
  text-decoration: none;
  margin-bottom: 8px;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #5dade2;
  text-decoration: underline;
}

.footer-section p {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #bdc3c7;
}

.disclaimer {
  font-size: 12px;
  color: #95a5a6;
  font-style: italic;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding-top: 20px;
  text-align: center;
}

.footer-bottom p {
  margin: 0;
  font-size: 13px;
  color: #95a5a6;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>
