<template>
  <div class="about-page">
    <div class="container">
      <div class="about-card">
        <!-- 头像 -->
        <div class="avatar-wrapper">
          <img :src="avatar" alt="站长头像" class="avatar" />
        </div>
        
        <!-- 基本信息 -->
        <h1 class="name">{{ name }}</h1>
        <p class="bio">{{ bio }}</p>
        
        <!-- 技术方向 -->
        <div class="tech-stack">
          <h3>技术方向</h3>
          <div class="tags">
            <span v-for="tech in techStack" :key="tech" class="tech-tag">
              {{ tech }}
            </span>
          </div>
        </div>
        
        <!-- 链接 -->
        <div class="links">
          <h3>友情链接</h3>
          <div class="link-grid">
            <a 
              v-for="link in links" 
              :key="link.name"
              :href="link.url" 
              target="_blank"
              rel="noopener"
              class="link-item"
            >
              <span class="link-icon">{{ link.icon }}</span>
              <span class="link-name">{{ link.name }}</span>
            </a>
          </div>
        </div>
        
        <!-- 联系信息 -->
        <div class="contact">
          <h3>联系方式</h3>
          <p class="email">邮箱：{{ email }}</p>
          <p class="note">欢迎技术交流与合作</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const avatar = ref('https://avatars.githubusercontent.com/u/your-avatar')
const name = ref('站长名称')
const bio = ref('全栈开发者 | 技术爱好者 | 终身学习者')

const techStack = ref([
  'Java',
  'Spring Boot',
  'Vue.js',
  'React',
  'MySQL',
  'Redis',
  'Docker',
  'Linux'
])

const links = ref([
  { name: 'GitHub', url: 'https://github.com/yourusername', icon: '🐙' },
  { name: 'Gitee', url: 'https://gitee.com/yourusername', icon: '🐒' },
  { name: '知乎', url: 'https://zhihu.com/people/yourusername', icon: '💡' },
  { name: '掘金', url: 'https://juejin.cn/user/yourusername', icon: '📝' }
])

const email = ref('contact@example.com')
</script>

<style scoped>
.about-page {
  min-height: 100vh;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.about-card {
  background: white;
  border-radius: 20px;
  padding: 50px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.avatar-wrapper {
  text-align: center;
  margin-bottom: 30px;
}

.avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 5px solid #667eea;
  object-fit: cover;
}

.name {
  text-align: center;
  font-size: 36px;
  color: #333;
  margin: 0 0 15px;
}

.bio {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin: 0 0 40px;
  line-height: 1.6;
}

.tech-stack,
.links,
.contact {
  margin-bottom: 40px;
}

h3 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #667eea;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 10px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s;
}

.link-item:hover {
  background: #667eea;
  color: white;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.link-icon {
  font-size: 24px;
}

.link-name {
  font-size: 14px;
  font-weight: 500;
}

.contact {
  text-align: center;
}

.email {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.note {
  font-size: 14px;
  color: #999;
  margin: 0;
}

@media (max-width: 768px) {
  .about-card {
    padding: 30px 20px;
  }
  
  .name {
    font-size: 28px;
  }
  
  .link-grid {
    grid-template-columns: 1fr;
  }
}
</style>
