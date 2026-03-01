<template>
  <div class="appointment-management-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
          <el-button type="primary" @click="refreshAppointments">刷新</el-button>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-form :model="filterForm" label-width="80px" inline>
          <el-form-item label="状态">
            <el-select v-model="filterForm.status" placeholder="选择状态" clearable @change="handleFilter">
              <el-option label="待处理" value="PENDING" />
              <el-option label="已确认" value="CONFIRMED" />
              <el-option label="已拒绝" value="REJECTED" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleFilter"
            />
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="appointments" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="预约ID" width="100" />
        <el-table-column prop="tenantId" label="租客ID" width="100" />
        <el-table-column prop="houseId" label="房源ID" width="100" />
        <el-table-column label="房源信息" width="300">
          <template #default="scope">
            <el-button type="text" @click="viewHouse(scope.row.houseId)">
              {{ scope.row.house?.title || '查看房源' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="200" />
        <el-table-column prop="contactPhone" label="联系电话" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="租客备注" />
        <el-table-column label="操作" width="250" fixed="right">
         <template #default="scope">
  <el-button-group>
    <el-button
      v-if="scope.row.status === 'PENDING'"
      type="success"
      size="small"
      @click="confirmAppointment(scope.row.id)"
    >
      确认预约
    </el-button>
    <el-button
      v-if="scope.row.status === 'PENDING'"
      type="danger"
      size="small"
      @click="rejectAppointment(scope.row.id)"
    >
      拒绝预约
    </el-button>
    <el-button
      type="primary"
      size="small"
      @click="viewDetail(scope.row.id)"
    >
      查看详情
    </el-button>
  </el-button-group>
</template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复预约"
      width="500px"
    >
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px">
        <el-form-item label="回复内容" prop="landlordReply">
          <el-input
            v-model="replyForm.landlordReply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            @input="handleInput"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, reactive, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'AppointmentManagement',
  setup() {
    const router = useRouter()
    const store = useStore()
    const appointments = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const loading = ref(false)
    const replyDialogVisible = ref(false)
    const currentAppointmentId = ref(null)
    const currentAction = ref('') // confirm or reject
    const replyFormRef = ref(null)
    
    // 获取当前用户信息
    const currentUser = computed(() => store.state.auth.user)
    const landlordId = computed(() => currentUser.value?.userId)
    
    const replyForm = reactive({
      landlordReply: ''
    })
    
    // 筛选表单
    const filterForm = reactive({
      status: '',
      dateRange: []
    })
    
    const replyRules = {
      landlordReply: [
        { required: true, message: '请输入回复内容', trigger: 'blur' }
      ]
    }
    
    // 加载预约列表
    const loadAppointments = async () => {
      if (!landlordId.value) {
        ElMessage.error('用户信息获取失败')
        return
      }
      
      loading.value = true
      try {
        // 构建筛选参数
        const params = {
          landlordId: landlordId.value,
          page: currentPage.value - 1, // 后端使用0开始的页码
          size: pageSize.value,
          status: filterForm.status || undefined,
          startDate: filterForm.dateRange?.[0] || undefined,
          endDate: filterForm.dateRange?.[1] || undefined
        }
        
        const res = await request({
          url: '/landlord/appointments',
          method: 'get',
          params: params
        })
        // 显示所有预约，包括已完成的
        appointments.value = res.content
        total.value = res.totalElements
      } catch (error) {
        ElMessage.error('加载预约列表失败: ' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }
    
    // 处理筛选条件变化
    const handleFilter = () => {
      currentPage.value = 1 // 筛选时回到第一页
      loadAppointments()
    }
    
    // 刷新预约列表
    const refreshAppointments = () => {
      loadAppointments()
      ElMessage.success('刷新成功')
    }
    
    // 查看房源详情
    const viewHouse = (houseId) => {
      router.push(`/house/${houseId}`)
    }
    
    // 查看预约详情
    const viewDetail = (appointmentId) => {
      // 这里可以跳转到预约详情页面，如果有的话
      ElMessage.info('查看详情功能待实现')
    }
    
    // 确认预约
    const confirmAppointment = (appointmentId) => {
      currentAppointmentId.value = appointmentId
      currentAction.value = 'confirm'
      replyForm.landlordReply = ''
      replyDialogVisible.value = true
      // 重置表单验证状态
      nextTick(() => {
        if (replyFormRef.value) {
          replyFormRef.value.resetFields()
        }
      })
    }
    
    // 拒绝预约
    const rejectAppointment = (appointmentId) => {
      currentAppointmentId.value = appointmentId
      currentAction.value = 'reject'
      replyForm.landlordReply = ''
      replyDialogVisible.value = true
      // 重置表单验证状态
      nextTick(() => {
        if (replyFormRef.value) {
          replyFormRef.value.resetFields()
        }
      })
    }
    
    // 处理输入事件，解决输入延迟问题
    const handleInput = (value) => {
      // 强制更新回复表单的内容
      replyForm.landlordReply = value
      // 如果表单验证有问题，重置验证状态
      if (replyFormRef.value) {
        replyFormRef.value.clearValidate('landlordReply')
      }
    }
    
    // 提交回复
    const submitReply = async () => {
      if (!currentAppointmentId.value) return
      
      loading.value = true
      try {
        const status = currentAction.value === 'confirm' ? 'CONFIRMED' : 'REJECTED'
        await request({
          url: `/landlord/appointments/${currentAppointmentId.value}/status`,
          method: 'put',
          params: {
            status,
            reply: replyForm.landlordReply
          }
        })
        
        ElMessage.success(currentAction.value === 'confirm' ? '预约已确认' : '预约已拒绝')
        replyDialogVisible.value = false
        loadAppointments()
      } catch (error) {
        ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }
    
    // 获取状态标签类型
    const getStatusType = (status) => {
      const typeMap = {
        PENDING: 'warning',
        CONFIRMED: 'success',
        REJECTED: 'danger',
        COMPLETED: 'info',
        CANCELLED: 'info'
      }
      return typeMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        PENDING: '待处理',
        CONFIRMED: '已确认',
        REJECTED: '已拒绝',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      }
      return textMap[status] || status
    }
    
    // 页码变化处理
    const handlePageChange = (page) => {
      currentPage.value = page
      loadAppointments()
    }
    
    onMounted(() => {
      loadAppointments()
    })
    
    return {
      appointments,
      total,
      currentPage,
      pageSize,
      loading,
      replyDialogVisible,
      replyForm,
      replyFormRef,
      replyRules,
      filterForm,
      viewHouse,
      viewDetail,
      confirmAppointment,
      rejectAppointment,
      submitReply,
      getStatusType,
      getStatusText,
      handlePageChange,
      handleFilter,
      refreshAppointments,
      handleInput
    }
  }
}
</script>

<style scoped>
.appointment-management-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.filter-section:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  padding: 10px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header-wrapper) {
  background-color: #fafafa;
}

:deep(.el-table__header th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table__body tr:hover) {
  background-color: #f5f7fa !important;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 对话框样式优化 */
:deep(.el-dialog__header) {
  background-color: #fafafa;
  border-radius: 8px 8px 0 0;
}

:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-size: 12px;
  padding: 4px 8px;
}

/* 选择器和输入框样式优化 */
:deep(.el-input__inner),
:deep(.el-select__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus),
:deep(.el-select__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}
</style>