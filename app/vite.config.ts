import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        builds: resolve(__dirname, 'builds/index.html'),
        visualtests: resolve(__dirname, 'visualtests/index.html')
      },
    },
  }
})
