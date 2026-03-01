<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button type="primary" @click="refreshOrders">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="orderList" v-loading="loading" border>
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="houseId" label="房源ID" />
        <el-table-column prop="monthlyRent" label="月租金" />
        <el-table-column prop="totalAmount" label="总金额" />
        <el-table-column prop="orderStatus" label="订单状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.orderStatus)">
              {{ getStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态">
          <template #default="scope">
            <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
              {{ getPaymentStatusText(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="350">
          <template #default="scope">
            <el-button 
              v-if="scope.row.orderStatus === 'PENDING'" 
              type="success" 
              size="small" 
              @click="confirmOrder(scope.row)"
            >
              确认订单
            </el-button>
            <el-button 
              v-if="scope.row.orderStatus === 'PENDING'" 
              type="danger" 
              size="small" 
              @click="cancelOrder(scope.row)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="scope.row.orderStatus === 'CONFIRMED'" 
              type="primary" 
              size="small" 
              @click="activateOrder(scope.row)"
            >
              激活订单
            </el-button>
            <el-button 
              v-if="scope.row.orderStatus === 'ACTIVE'" 
              type="info" 
              size="small" 
              @click="completeOrder(scope.row)"
            >
              标记为已完成
            </el-button>
            <el-button 
              v-if="scope.row.paymentStatus === 'PAID' && scope.row.paymentStatus !== 'REFUNDED'" 
              type="danger" 
              size="small" 
              @click="showRefundDialog(scope.row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
    
    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="订单退款"
      width="500px"
    >
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="80px">
        <el-form-item label="订单号">
          <el-input v-model="refundForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="refundForm.amount" disabled />
        </el-form-item>
        <el-form-item label="退款原因" prop="refundReason">
          <el-input
            v-model="refundForm.refundReason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund" :loading="refunding">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed, reactive } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'OrderManagement',
  setup() {
    const store = useStore()
    const loading = ref(false)
    const orderList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    // 退款对话框相关
    const refundDialogVisible = ref(false)
    const refunding = ref(false)
    const refundForm = reactive({
      orderNo: '',
      amount: '',
      refundReason: ''
    })
    const refundFormRef = ref(null)
    const refundRules = {
      refundReason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
    }

    const userId = computed(() => store.state.auth.user?.userId)

    const loadOrders = async () => {
      if (!userId.value) {
        ElMessage.error('用户信息获取失败')
        return
      }
      loading.value = true
      try {
        const res = await request({
          url: '/landlord/orders',
          method: 'get',
          params: {
            landlordId: userId.value,
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        orderList.value = res.content || []
        console.log('订单列表:', orderList.value)
        total.value = res.totalElements || 0
      } catch (error) {
        console.error('加载订单列表失败:', error)
        ElMessage.error('加载订单列表失败')
      } finally {
        loading.value = false
      }
    }

    const getStatusType = (status) => {
      const map = {
        PENDING: 'warning',
        CONFIRMED: 'success',
        ACTIVE: 'primary',
        COMPLETED: 'info',
        CANCELLED: 'danger'
      }
      return map[status] || 'info'
    }

    const getStatusText = (status) => {
      const map = {
        PENDING: '待确认',
        CONFIRMED: '已确认',
        ACTIVE: '进行中',
        COMPLETED: '已完成',
        CANCELLED: '已取消'
      }
      return map[status] || status
    }

    const handlePageChange = () => {
      loadOrders()
    }

    const handleSizeChange = () => {
      currentPage.value = 1
      loadOrders()
    }

    // 确认订单
    const confirmOrder = async (order) => {
      console.log('确认订单:', order)
      try {
        await request({
          url: `/landlord/orders/${order.id}/status`,
          method: 'put',
          params: {
            status: 'CONFIRMED'
          }
        })
        ElMessage.success('订单确认成功')
        loadOrders()
      } catch (error) {
        console.error('确认订单失败:', error)
        ElMessage.error('订单确认失败')
      }
    }

    // 取消订单
    const cancelOrder = async (order) => {
      try {
        await request({
          url: `/landlord/orders/${order.id}/status`,
          method: 'put',
          params: {
            status: 'CANCELLED'
          }
        })
        ElMessage.success('订单取消成功')
        loadOrders()
      } catch (error) {
        ElMessage.error('订单取消失败')
      }
    }

    // 激活订单
    const activateOrder = async (order) => {
      try {
        await request({
          url: `/landlord/orders/${order.id}/status`,
          method: 'put',
          params: {
            status: 'ACTIVE'
          }
        })
        ElMessage.success('订单激活成功')
        loadOrders()
      } catch (error) {
        ElMessage.error('订单激活失败')
      }
    }

    // 标记为已完成
    const completeOrder = async (order) => {
      try {
        await request({
          url: `/landlord/orders/${order.id}/status`,
          method: 'put',
          params: {
            status: 'COMPLETED'
          }
        })
        ElMessage.success('订单已标记为已完成')
        loadOrders()
      } catch (error) {
        ElMessage.error('标记订单完成失败')
      }
    }

    // 退款
    const refundOrder = async (order) => {
      try {
        await request({
          url: `/landlord/orders/${order.id}/payment-status`,
          method: 'put',
          params: {
            status: 'REFUNDED'
          }
        })
        ElMessage.success('订单已退款')
        loadOrders()
      } catch (error) {
        ElMessage.error('退款操作失败')
      }
    }

    // 显示退款对话框
    const showRefundDialog = (order) => {
      refundForm.orderNo = order.orderNo
      refundForm.amount = order.totalAmount
      refundForm.refundReason = ''
      refundDialogVisible.value = true
    }

    // 提交退款申请
    const submitRefund = async () => {
      if (!refundFormRef.value) return
      
      try {
        await refundFormRef.value.validate()
        
        // 查找当前订单
        const currentOrder = orderList.value.find(order => order.orderNo === refundForm.orderNo)
        if (!currentOrder) {
          ElMessage.error('找不到对应的订单')
          return
        }
        
        refunding.value = true
        // 调用退款API，使用后端已有的支付状态更新端点
        await request({
          url: `/landlord/orders/${currentOrder.id}/payment-status`,
          method: 'put',
          params: {
            status: 'REFUNDED'
          }
        })
        
        ElMessage.success('退款申请提交成功')
        refundDialogVisible.value = false
        loadOrders()
      } catch (error) {
        ElMessage.error('退款操作失败：' + (error.response?.headers?.['x-error-message'] || error.response?.data?.message || error.message))
      } finally {
        refunding.value = false
      }
    }

    // 获取支付状态标签类型
    const getPaymentStatusType = (status) => {
      const typeMap = {
        UNPAID: 'warning',
        PAID: 'success',
        REFUNDED: 'info'
      }
      return typeMap[status] || 'info'
    }

    // 获取支付状态文本
    const getPaymentStatusText = (status) => {
      const textMap = {
        UNPAID: '未支付',
        PAID: '已支付',
        REFUNDED: '已退款'
      }
      return textMap[status] || status
    }

    // 刷新订单列表
    const refreshOrders = () => {
      loadOrders()
      ElMessage.success('订单列表已刷新')
    }

    onMounted(() => {
      loadOrders()
    })

    return {
      loading,
      orderList,
      currentPage,
      pageSize,
      total,
      getStatusType,
      getStatusText,
      getPaymentStatusType,
      getPaymentStatusText,
      handlePageChange,
      handleSizeChange,
      refreshOrders,
      confirmOrder,
      cancelOrder,
      activateOrder,
      completeOrder,
      refundOrder,
      refundDialogVisible,
      refunding,
      refundForm,
      refundFormRef,
      refundRules,
      showRefundDialog,
      submitRefund
    }
  }
}
</script>

<style scoped>
.order-management {
  padding: 20px;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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
  margin-right: 8px;
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

