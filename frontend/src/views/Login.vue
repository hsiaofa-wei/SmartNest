<template>
  <div class="login-container">
    <video class="background-video" autoplay loop muted playsinline>
      <source src="/back.mp4" type="video/mp4">
    </video>
    <div class="login-box">
      <div class="login-header">
        <h2>智能租房系统</h2>
        <p>欢迎登录</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              prefix-icon="DocumentCopy"
              size="large"
              clearable
              style="flex: 1"
            />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-image"
              @click="refreshCaptcha"
              alt="验证码"
            />
            <el-icon v-else class="captcha-loading" @click="refreshCaptcha">
              <Loading />
            </el-icon>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="login-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Loading, DocumentCopy } from '@element-plus/icons-vue'
import { login, getCaptcha } from '../api/auth'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const captchaImage = ref('')
    const captchaKey = ref('')

    const loginForm = ref({
      username: '',
      password: '',
      captcha: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
      ]
    }

    const loadCaptcha = async () => {
      try {
        const res = await getCaptcha()
        captchaImage.value = res.image
        captchaKey.value = res.key
      } catch (error) {
        ElMessage.error('加载验证码失败')
        console.error('验证码加载失败:', error)
      }
    }

    const refreshCaptcha = () => {
      loadCaptcha()
      loginForm.value.captcha = ''
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const res = await login({
              username: loginForm.value.username,
              password: loginForm.value.password,
              captcha: loginForm.value.captcha,
              captchaKey: captchaKey.value
            })
            
            store.dispatch('auth/login', {
              token: res.token,
              user: {
                username: res.username,
                role: res.role,
                userId: res.userId,
                avatarUrl: res.avatarUrl
              }
            })

            ElMessage.success('登录成功')
            
            // 根据角色跳转
            if (res.role === 'ADMIN') {
              router.push('/admin/dashboard')
            } else if (res.role === 'LANDLORD') {
              router.push('/landlord/houses')
            } else {
              router.push('/tenant/houses')
            }
          } catch (error) {
            // 登录失败时刷新验证码
            refreshCaptcha()
            ElMessage.error(error.response?.data?.message || '登录失败')
          } finally {
            loading.value = false
          }
        }
      })
    }

    // 组件挂载时加载验证码
    onMounted(() => {
      loadCaptcha()
    })

    return {
      loginForm,
      loginFormRef,
      rules,
      loading,
      captchaImage,
      captchaKey,
      handleLogin,
      refreshCaptcha,
      // Icons
      Loading,
      DocumentCopy
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.background-video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.captcha-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.login-footer {
  text-align: center;
  width: 100%;
  font-size: 14px;
}

@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .captcha-image {
    width: 100px;
    height: 35px;
  }
}
</style>

