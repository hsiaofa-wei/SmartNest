<template>
  <div class="orders-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
        </div>
      </template>

      <!-- 订单筛选区域 -->
      <div class="orders-filter">
        <el-radio-group v-model="orderStatusFilter" class="filter-group">
          <el-radio-button label="all">全部订单</el-radio-button>
          <el-radio-button label="PENDING">待确认</el-radio-button>
          <el-radio-button label="CONFIRMED">已确认</el-radio-button>
          <el-radio-button label="ACTIVE">进行中</el-radio-button>
          <el-radio-button label="COMPLETED">已完成</el-radio-button>
          <el-radio-button label="CANCELLED">已取消</el-radio-button>
        </el-radio-group>

        <el-radio-group v-model="paymentStatusFilter" class="filter-group">
          <el-radio-button label="all">全部支付状态</el-radio-button>
          <el-radio-button label="UNPAID">未支付</el-radio-button>
          <el-radio-button label="PAID">已支付</el-radio-button>
          <el-radio-button label="REFUNDED">已退款</el-radio-button>
        </el-radio-group>

        <el-button type="primary" @click="refreshOrders" :loading="loading" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新订单
        </el-button>
      </div>

      <!-- 订单列表 -->
      <el-table
        :data="filteredOrders"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="houseId" label="房源ID" width="100" />
        <el-table-column prop="monthlyRent" label="月租金" width="100">
          <template #default="scope">
            ¥{{ formatPrice(scope.row.monthlyRent) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="scope">
            ¥{{ formatPrice(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.orderStatus)">
              {{ getStatusText(scope.row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="120">
          <template #default="scope">
            <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
              {{ getPaymentStatusText(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handlePayClick(scope.row)"
              :disabled="scope.row.orderStatus !== 'PENDING' || scope.row.paymentStatus === 'PAID' || scope.row.paymentStatus === 'REFUNDED'"
              class="mr-2"
            >
              <el-icon><Money /></el-icon>
              {{ scope.row.paymentStatus === 'PAID' ? '已支付' : '去支付' }}
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleOrderDetail(scope.row)"
            >
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[5, 10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="orderDetailDrawer"
      title="订单详情"
      size="60%"
      :with-header="true"
    >
      <div v-if="selectedOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ selectedOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="房源ID">{{ selectedOrder.houseId }}</el-descriptions-item>
          <el-descriptions-item label="租客ID">{{ selectedOrder.tenantId }}</el-descriptions-item>
          <el-descriptions-item label="房东ID">{{ selectedOrder.landlordId }}</el-descriptions-item>
          <el-descriptions-item label="月租金">¥{{ formatPrice(selectedOrder.monthlyRent) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ formatPrice(selectedOrder.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="押金">¥{{ formatPrice(selectedOrder.deposit || 0) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(selectedOrder.orderStatus)">
              {{ getStatusText(selectedOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(selectedOrder.paymentStatus)">
              {{ getPaymentStatusText(selectedOrder.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDate(selectedOrder.startDate) }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ formatDate(selectedOrder.endDate) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(selectedOrder.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ selectedOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="暂无订单详情" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Money, View } from '@element-plus/icons-vue'
import request from '../../api/request'

// 状态管理
const store = useStore()
const router = useRouter()
const loading = ref(false)
const allOrders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选条件
const orderStatusFilter = ref('all')
const paymentStatusFilter = ref('all')

// 订单详情抽屉
const orderDetailDrawer = ref(false)
const selectedOrder = ref(null)

// 获取当前用户ID
const userId = computed(() => {
  const user = store.state.auth.user
  return user ? user.userId : null
})

// 过滤后的订单列表
const filteredOrders = computed(() => {
  let filtered = allOrders.value

  // 按订单状态筛选
  if (orderStatusFilter.value !== 'all') {
    filtered = filtered.filter(order => order.orderStatus === orderStatusFilter.value)
  }

  // 按支付状态筛选
  if (paymentStatusFilter.value !== 'all') {
    filtered = filtered.filter(order => order.paymentStatus === paymentStatusFilter.value)
  }

  // 确保订单有有效ID
  return filtered.filter(order => {
    return order && order.id && !isNaN(order.id) && Number(order.id) > 0
  })
})

// 加载订单列表
const loadOrders = async () => {
  // 检查用户是否已登录
  if (!userId.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const response = await request({
      url: '/tenant/orders',
      method: 'get',
      params: {
        tenantId: userId.value,
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })

    allOrders.value = response.content || []
    total.value = response.totalElements || 0
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新订单
const refreshOrders = () => {
  currentPage.value = 1
  loadOrders()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return Number(price).toFixed(2)
}

// 获取订单状态对应的类型
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    CONFIRMED: 'success',
    ACTIVE: 'primary',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取订单状态对应的文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    ACTIVE: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

// 获取支付状态对应的类型
const getPaymentStatusType = (status) => {
  const typeMap = {
    UNPAID: 'warning',
    PAID: 'success',
    REFUNDED: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取支付状态对应的文本
const getPaymentStatusText = (status) => {
  const textMap = {
    UNPAID: '未支付',
    PAID: '已支付',
    REFUNDED: '已退款'
  }
  return textMap[status] || status
}

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

// 验证订单ID是否有效
const isValidOrderId = (orderId) => {
  if (!orderId) return false
  if (typeof orderId === 'string' && orderId.toLowerCase() === 'undefined') return false
  if (isNaN(orderId)) return false
  const numOrderId = Number(orderId)
  return numOrderId > 0
}

// 处理支付按钮点击
const handlePayClick = (row) => {
  // 验证订单ID
  if (!isValidOrderId(row.id)) {
    console.error('订单ID无效:', row.id)
    ElMessage.error('订单ID无效')
    return
  }
  
  try {
    // 跳转到支付页面
    router.push(`/tenant/payment/${row.id}`)
  } catch (error) {
    console.error('跳转支付页面失败:', error)
    ElMessage.error('跳转支付页面失败，请稍后重试')
  }
}

// 处理订单详情
const handleOrderDetail = (row) => {
  selectedOrder.value = row
  orderDetailDrawer.value = true
}

// 页面挂载时加载订单
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  margin-top: 80px;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.orders-filter {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
}

.refresh-btn {
  margin-left: auto;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.mr-2 {
  margin-right: 8px;
}

.order-detail {
  padding: 20px 0;
}

.empty-detail {
  padding: 50px 0;
  text-align: center;
}
</style>

