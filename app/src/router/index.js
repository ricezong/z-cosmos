import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Tieba from '../views/Tieba.vue'
import Theater from '../views/Theater.vue'
import Hot from '../views/Hot.vue'
import Tools from '../views/Tools.vue'
import Search from '../views/Search.vue'
import Login from '../views/Login.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/tieba', component: Tieba },
  { path: '/theater', component: Theater },
  { path: '/hot', component: Hot },
  { path: '/tools', component: Tools },
  { path: '/search', component: Search },
  { path: '/login', component: Login },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
