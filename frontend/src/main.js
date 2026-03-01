import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 全局错误捕获：忽略高德地图API的Vg/Ud错误
app.config.errorHandler = (err, vm, info) => {
  const errorMessage = err.toString()
  if (!errorMessage.includes('Vg') && !errorMessage.includes('Ud')) {
    console.error('Vue全局错误:', err, vm, info)
  }
}

// 全局JS错误捕获：忽略高德地图API相关错误
window.onerror = (message, source, lineno, colno, error) => {
  const errorMessage = message.toString()
  
  // 忽略来自高德地图域名的错误
  if (source && source.includes('webapi.amap.com')) {
    return true
  }
  
  // 忽略包含Vg或Ud的错误
  if (errorMessage.includes('Vg') || errorMessage.includes('Ud')) {
    return true
  }
  
  // 忽略ResizeObserver错误（浏览器内部错误，不影响功能）
  if (errorMessage.includes('ResizeObserver')) {
    return true
  }
  
  // 忽略Chrome扩展相关错误
  if (errorMessage.includes('A listener indicated an asynchronous response')) {
    return true
  }
  
  // 其他错误正常记录
  console.error('全局JS错误:', message, source, lineno, colno, error)
  return false
}

app.use(store)
app.use(router)
app.use(ElementPlus)
app.mount('#app')

