import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Notes from '../views/Notes.vue'
import NoteDetail from '../views/NoteDetail.vue'
import Hot from '../views/Hot.vue'
import Theater from '../views/Theater.vue'
import Tools from '../views/Tools.vue'
import Search from '../views/Search.vue'
import About from '../views/About.vue'

const routes = [
  { path: '/', component: Home, name: 'Home' },
  { path: '/notes', component: Notes, name: 'Notes' },
  { path: '/notes/:id', component: NoteDetail, name: 'NoteDetail', props: true },
  { path: '/hot', component: Hot, name: 'Hot' },
  { path: '/theater', component: Theater, name: 'Theater' },
  { path: '/tools', component: Tools, name: 'Tools' },
  { path: '/search', component: Search, name: 'Search' },
  { path: '/about', component: About, name: 'About' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return { top: 0 }
  }
})

export default router
