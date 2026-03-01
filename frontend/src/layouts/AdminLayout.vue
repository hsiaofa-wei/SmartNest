<template>
  <el-container class="admin-layout">
    <el-header class="header">
      <div class="logo">
        <h2>智能租房系统 - 管理后台</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userInfo.avatarUrl" :size="40">{{ userInfo.username?.charAt(0) }}</el-avatar>
            <span>{{ userInfo.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="register">注册新账号</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '150px'" class="sidebar">
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon v-if="isCollapse"><ArrowRight /></el-icon>
          <el-icon v-else><ArrowLeft /></el-icon>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          :collapse="isCollapse"
          collapse-transition
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>
              <span>数据看板</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>
              <span>用户管理</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/admin/houses/audit">
            <el-icon><Document /></el-icon>
            <template #title>
              <span>房源审核</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>
              <span>数据统计</span>
            </template>
          </el-menu-item>
          <el-menu-item index="/admin/profile">
            <el-icon><User /></el-icon>
            <template #title>
              <span>个人信息</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 注册弹窗 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="用户注册"
      width="500px"
      :close-on-click-modal="false"
      :z-index="3000"
      @closed="handleDialogClosed"
    >
      <RegisterForm 
        :show-login-link="true" 
        :auto-load-captcha="true"
        ref="registerFormRef"
        @success="handleRegisterSuccess"
        @cancel="registerDialogVisible = false"
      />
    </el-dialog>
  </el-container>
</template>

<script>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox } from 'element-plus'
import RegisterForm from '../components/RegisterForm.vue'

export default {
  name: 'AdminLayout',
  components: { RegisterForm },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const isCollapse = ref(false)

    const activeMenu = computed(() => route.path)
    const userInfo = computed(() => store.state.auth.user || {})

    // 注册弹窗相关
    const registerDialogVisible = ref(false)
    const registerFormRef = ref(null)

    const handleCommand = (command) => {
      if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
          .then(() => {
            store.dispatch('auth/logout')
            router.push('/login')
          })
          .catch(() => {})
      } else if (command === 'register') {
        registerDialogVisible.value = true
      }
    }

    const handleRegisterSuccess = () => {
      registerDialogVisible.value = false
    }

    const handleDialogClosed = () => {
      // 弹窗关闭时重置表单
      if (registerFormRef.value) {
        registerFormRef.value.resetForm()
      }
    }

    // 监听注册弹窗显示，自动加载验证码
    watch(registerDialogVisible, (newVal) => {
      if (newVal && registerFormRef.value) {
        // 延迟一下确保组件已渲染
        setTimeout(() => {
          registerFormRef.value?.refreshCaptcha()
        }, 100)
      }
    })

    const toggleCollapse = () => {
      isCollapse.value = !isCollapse.value
    }

    return {
      activeMenu,
      userInfo,
      handleCommand,
      isCollapse,
      toggleCollapse,
      // 注册弹窗相关
      registerDialogVisible,
      registerFormRef,
      handleRegisterSuccess,
      handleDialogClosed
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo h2 {
  margin: 0;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.sidebar {
  background: #304156;
  transition: width 0.3s ease-in-out;
  position: relative;
}

.sidebar-menu {
  border: none;
  background: #304156;
  margin-top: 50px;
}

.sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover {
  background: #263445;
}

.sidebar-menu .el-menu-item.is-active {
  background: #409eff;
  color: #fff;
}

.collapse-btn {
  position: absolute;
  top: 10px;
  right: calc(50% - 12px);
  background: #304156;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #bfcbd9;
  transition: all 0.3s;
  z-index: 100;
}

.collapse-btn:hover {
  color: #409eff;
  background: #263445;
}

.main-content {  
  background: #f0f2f5;
  padding: 20px;
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px !important;
  }
  
  .logo h2 {
    font-size: 16px;
  }
}
</style>