import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: './',
  optimizeDeps: {
    include: ['three', 'three/examples/jsm/renderers/CSS2DRenderer.js'],
  },
})
