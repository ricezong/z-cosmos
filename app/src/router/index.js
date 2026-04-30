import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Community from '../views/Community.vue'
import Theater from '../views/Theater.vue'
import Hot from '../views/Hot.vue'
import Tools from '../views/Tools.vue'
import Search from '../views/Search.vue'
import Login from '../views/Login.vue'
import Profile from '../views/Profile.vue'
import AuthCallback from '../views/AuthCallback.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/community', component: Community },
  { path: '/theater', component: Theater },
  { path: '/hot', component: Hot },
  { path: '/tools', component: Tools },
  { path: '/search', component: Search },
  { path: '/login', component: Login },
  { path: '/auth/callback', component: AuthCallback },
  { path: '/profile', component: Profile },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
