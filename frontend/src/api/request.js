import axios from 'axios'
import { ElMessage } from 'element-plus'
import store from '../store'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = store.state.auth.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        store.dispatch('auth/logout')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else {
        // 检查是否为支付状态查询请求，如果是则静默处理
        if (error.config?.url?.includes('/payment/') && error.config?.url?.includes('/status')) {
          console.error('支付状态查询失败:', error.response)
        } else {
          const message = error.response.data?.message || error.response.data?.error || '请求失败'
          ElMessage.error(message)
          console.error('请求错误:', error.response)
        }
      }
    } else if (error.request) {
      ElMessage.error('无法连接到服务器，请检查后端服务是否启动')
      console.error('网络错误:', error)
    } else {
      ElMessage.error('请求配置错误')
      console.error('请求错误:', error.message)
    }
    return Promise.reject(error)
  }
)

export default service

