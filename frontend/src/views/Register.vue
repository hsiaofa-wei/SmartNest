<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建您的账号</p>
      </div>
      <RegisterForm :show-login-link="true" :auto-load-captcha="true" />
    </div>
  </div>
</template>

<script>
import RegisterForm from '../components/RegisterForm.vue'

export default {
  name: 'Register',
  components: {
    RegisterForm
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 450px;
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
  color: #999;
  font-size: 14px;
}

.register-form {
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

.register-footer {
  text-align: center;
  width: 100%;
  font-size: 14px;
}

@media (max-width: 768px) {
  .register-box {
    padding: 30px 20px;
  }
  
  .captcha-image {
    width: 100px;
    height: 35px;
  }
}
</style>