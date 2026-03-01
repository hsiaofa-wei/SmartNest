const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000,
    host: 'localhost',
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: (error) => {
          const errorMessage = error.message || ''
          // 静默ResizeObserver错误
          if (errorMessage.includes('ResizeObserver')) {
            return false
          }
          // 静默Chrome扩展相关错误
          if (errorMessage.includes('A listener indicated an asynchronous response')) {
            return false
          }
          return true
        }
      }
    },
    headers: {
      'Cross-Origin-Embedder-Policy': 'unsafe-none',
      'Cross-Origin-Opener-Policy': 'same-origin',
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端没有context-path，所以需要去掉/api前缀
        pathRewrite: {
          '^/api': ''  // 将 /api/auth/captcha 转发为 http://localhost:8080/auth/captcha
        }
      },
      '/files': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/deepseek': {
        target: 'https://api.deepseek.com',
        changeOrigin: true,
        pathRewrite: {
          '^/deepseek': ''  // 将 /deepseek/v1/chat/completions 转发为 https://api.deepseek.com/v1/chat/completions
        },
        secure: false
      },
      '/map-api': {
        target: 'https://apis.map.qq.com',
        changeOrigin: true,
        pathRewrite: {
          '^/map-api': ''  // 将 /map-api/ws/geocoder/v1/ 转发为 https://apis.map.qq.com/ws/geocoder/v1/
        },
        secure: false
      }
    }
  },
  // 配置webpack
  configureWebpack: {
    // 移除外部依赖配置，因为我们通过map-service.js动态加载高德地图
  }
})

