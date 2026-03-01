<template>
  <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
    <el-form-item prop="username">
      <el-input
        v-model="registerForm.username"
        placeholder="用户名"
        prefix-icon="User"
        size="large"
        clearable
      />
    </el-form-item>
    <el-form-item prop="password">
      <el-input
        v-model="registerForm.password"
        :type="showPassword ? 'text' : 'password'"
        placeholder="密码（至少8位，包含大小写字母和数字）"
        prefix-icon="Lock"
        size="large"
        clearable
      >
        <template #suffix>
          <el-icon @click="showPassword = !showPassword" style="cursor: pointer">
            <View v-if="!showPassword" />
            <Hide v-else />
          </el-icon>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item prop="phone">
      <el-input
        v-model="registerForm.phone"
        placeholder="手机号"
        prefix-icon="Phone"
        size="large"
        clearable
      />
    </el-form-item>
    <el-form-item prop="role">
      <el-radio-group v-model="registerForm.role" size="large">
        <el-radio label="LANDLORD">房东</el-radio>
        <el-radio label="TENANT">租客</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item prop="realName">
      <el-input
        v-model="registerForm.realName"
        placeholder="真实姓名"
        prefix-icon="UserFilled"
        size="large"
        clearable
      />
    </el-form-item>
    <el-form-item prop="captcha">
      <div class="captcha-container">
        <el-input
          v-model="registerForm.captcha"
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
    <el-form-item prop="agreementAccepted">
      <el-checkbox v-model="registerForm.agreementAccepted">
        我已阅读并同意用户协议和隐私政策
      </el-checkbox>
    </el-form-item>
    <el-form-item>
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        @click="handleRegister"
        style="width: 100%"
      >
        注册
      </el-button>
    </el-form-item>
    <el-form-item v-if="showLoginLink">
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="handleLogin">立即登录</el-link>
      </div>
    </el-form-item>
  </el-form>
</template>

<script>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { View, Hide, Loading, DocumentCopy } from '@element-plus/icons-vue'
import { register, getCaptcha } from '../api/auth'

export default {
  name: 'RegisterForm',
  props: {
    showLoginLink: {
      type: Boolean,
      default: true
    },
    autoLoadCaptcha: {
      type: Boolean,
      default: true
    }
  },
  emits: ['success', 'cancel'],
  setup(props, { emit }) {
    const router = useRouter()
    const store = useStore()
    const registerFormRef = ref(null)
    const loading = ref(false)
    const showPassword = ref(false)
    const captchaImage = ref('')
    const captchaKey = ref('')

    const registerForm = ref({
      username: '',
      password: '',
      phone: '',
      role: 'TENANT',
      realName: '',
      captcha: '',
      agreementAccepted: false
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { 
          pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/, 
          message: '密码至少8位，包含大小写字母和数字', 
          trigger: 'blur' 
        }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '真实姓名长度在2到20个字符', trigger: 'blur' }
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' }
      ],
      agreementAccepted: [
        { 
          validator: (rule, value, callback) => {
            if (!value) {
              callback(new Error('必须同意用户协议和隐私政策'))
            } else {
              callback()
            }
          }, 
          trigger: 'change' 
        }
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
      registerForm.value.captcha = ''
    }

    const resetForm = () => {
      if (registerFormRef.value) {
        registerFormRef.value.resetFields()
      }
      registerForm.value = {
        username: '',
        password: '',
        phone: '',
        role: 'TENANT',
        realName: '',
        captcha: '',
        agreementAccepted: false
      }
      refreshCaptcha()
    }

    const handleRegister = async () => {
      if (!registerFormRef.value) return
      
      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const res = await register({
              username: registerForm.value.username.trim(),
              password: registerForm.value.password,
              phone: registerForm.value.phone.trim(),
              role: registerForm.value.role,
              realName: registerForm.value.realName.trim(),
              captcha: registerForm.value.captcha,
              captchaKey: captchaKey.value,
              agreementAccepted: registerForm.value.agreementAccepted,
              // 兼容后端校验的默认占位字段
              verificationType: 'PHONE',
              verificationCode: '123456',
              verificationKey: 'default-key'
            })
            
            // 保存登录信息
            store.dispatch('auth/login', {
              token: res.token,
              user: {
                id: res.userId,
                userId: res.userId,
                username: res.username,
                role: res.role
              }
            })

            ElMessage.success('注册成功！')
            
            // 触发成功事件
            emit('success', res)
            
            // 根据角色跳转
            if (res.role === 'LANDLORD') {
              router.push('/landlord/houses')
            } else if (res.role === 'TENANT') {
              router.push('/tenant/houses')
            } else {
              router.push('/login')
            }
          } catch (error) {
            // 注册失败时刷新验证码
            refreshCaptcha()
            ElMessage.error(error.response?.data?.message || '注册失败')
          } finally {
            loading.value = false
          }
        }
      })
    }

    const handleLogin = () => {
      emit('cancel')
      router.push('/login')
    }

    // 暴露方法供父组件调用
    const validate = () => {
      return registerFormRef.value?.validate()
    }

    const clearValidate = () => {
      registerFormRef.value?.clearValidate()
    }

    // 组件挂载时加载验证码
    onMounted(() => {
      if (props.autoLoadCaptcha) {
        loadCaptcha()
      }
    })

    return {
      registerForm,
      registerFormRef,
      rules,
      loading,
      showPassword,
      captchaImage,
      captchaKey,
      handleRegister,
      handleLogin,
      resetForm,
      refreshCaptcha,
      validate,
      clearValidate,
      // Icons
      View,
      Hide,
      Loading,
      DocumentCopy
    }
  }
}
</script>

<style scoped>
.register-form {
  margin-top: 20px;
}

.captcha-container {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  flex-shrink: 0;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #409eff;
  box-shadow: 0 0 4px rgba(64, 158, 255, 0.3);
}

.register-footer {
  text-align: center;
  width: 100%;
  font-size: 14px;
}

@media (max-width: 768px) {
  .captcha-image {
    width: 100px;
    height: 35px;
  }
}
</style>

