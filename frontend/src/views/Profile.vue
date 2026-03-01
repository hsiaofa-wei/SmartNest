<template>
  <div class="profile">
    <el-card>
      <template #header>
        <span>个人信息</span>
      </template>

      <el-form :model="profileForm" label-width="100px" style="max-width: 600px">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :src="profileForm.avatarUrl" :size="100">
              {{ userInfo.username?.charAt(0) }}
            </el-avatar>
            <el-upload
              :action="uploadUrl"
              :data="uploadData"
              :headers="uploadHeaders"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :show-file-list="false"
            >
              <el-button size="small" type="primary">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" />
        </el-form-item>

        <el-form-item label="地址">
          <el-input v-model="profileForm.address" placeholder="请输入地址" />
        </el-form-item>

        <el-form-item label="真实姓名">
          <el-input v-model="profileForm.realName" />
        </el-form-item>

        <el-form-item label="身份证号">
          <el-input v-model="profileForm.idCard" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
          <el-button @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword, uploadFile } from '../api/user'
import request from '../api/request'

export default {
  name: 'Profile',
  setup() {
    const store = useStore()
    const loading = ref(false)
    const passwordDialogVisible = ref(false)
    const userInfo = computed(() => store.state.auth.user || {})
    
    const profileForm = ref({
      phone: '',
      avatarUrl: '',
      address: '',
      realName: '',
      idCard: ''
    })
    
    const passwordForm = ref({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const uploadUrl = computed(() => {
      const baseURL = request.defaults?.baseURL || '/api'
      return `${baseURL}/files/upload`
    })
    
    const uploadHeaders = computed(() => {
      const token = store.state.auth.token
      return {
        'Authorization': `Bearer ${token}`
      }
    })
    
    // 上传时附带 userId，让后端自动更新头像
    const uploadData = computed(() => {
      const uid = store.state.auth.user?.userId || store.state.auth.user?.id
      return uid ? { userId: uid } : {}
    })

    const loadProfile = async () => {
      try {
        const res = await getProfile()
        profileForm.value.phone = res.phone || ''
        profileForm.value.avatarUrl = res.avatarUrl || ''
        profileForm.value.address = res.address || ''
        profileForm.value.realName = res.realName || ''
        profileForm.value.idCard = res.idCard || ''
        // 更新Vuex中的用户信息，确保全局头像显示一致
        store.commit('auth/SET_USER', {
          ...userInfo.value,
          ...res
        })
      } catch (error) {
        ElMessage.error('加载个人信息失败')
      }
    }

    const handleSave = async () => {
      loading.value = true
      try {
        await updateProfile(profileForm.value)
        ElMessage.success('保存成功')
        await loadProfile()
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }

    const handleAvatarSuccess = async (response) => {
      if (!response.url) return
      // 确保返回的 URL 格式正确，使用相对路径
      let avatarUrl = response.url
      // 如果是绝对路径，转换为相对路径
      if (avatarUrl.startsWith('http://')) {
        avatarUrl = avatarUrl.replace(/^http:\/\/[^\/]+/, '')
      } else if (avatarUrl.startsWith('https://')) {
        avatarUrl = avatarUrl.replace(/^https:\/\/[^\/]+/, '')
      }
      // 确保路径以 / 开头
      if (!avatarUrl.startsWith('/')) {
        avatarUrl = '/' + avatarUrl
      }
      profileForm.value.avatarUrl = avatarUrl
      try {
        await updateProfile({ avatarUrl })
        await loadProfile()
      } catch (e) {
        // 后端更新失败时提示，但不阻塞前端显示
        ElMessage.error('头像已上传，但资料更新失败')
        console.error('更新头像失败:', e)
        return
      }
      ElMessage.success('头像上传成功')
    }

    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    }

    const handleChangePassword = () => {
      passwordDialogVisible.value = true
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }

    const handlePasswordSubmit = async () => {
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
      }
      
      try {
        await changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
      } catch (error) {
        ElMessage.error('密码修改失败')
      }
    }

    onMounted(() => {
      loadProfile()
    })

    return {
      loading,
      passwordDialogVisible,
      userInfo,
      profileForm,
      passwordForm,
      uploadUrl,
      uploadHeaders,
      uploadData,
      handleSave,
      handleAvatarSuccess,
      beforeAvatarUpload,
      handleChangePassword,
      handlePasswordSubmit
    }
  }
}
</script>

<style scoped>
.profile {
  margin-top: 80px;
  padding: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}
</style>

