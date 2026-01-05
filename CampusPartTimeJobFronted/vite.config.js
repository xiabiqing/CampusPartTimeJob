import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('代理错误:', err)
          })
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('代理请求:', req.method, req.url)
          })
        }
      },
      // WebSocket代理：注意后端有 context-path: /api，所以需要代理 /api/chat
      '/api/chat': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path,
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('WebSocket代理错误:', err, req?.url)
          })
          proxy.on('upgrade', (req, socket, head) => {
            console.log('WebSocket升级请求:', req.url, req.headers)
          })
        }
      }
    }
  }
})

