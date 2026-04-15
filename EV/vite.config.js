// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 스프링 부트 주소
        changeOrigin: true,
        // rewrite 줄은 아예 삭제하거나 지금처럼 주석 처리 유지!
      }
    }
  }
})