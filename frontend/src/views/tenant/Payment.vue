<template>
  <div class="payment-page">
    <el-card shadow="hover" v-if="order" class="payment-card">
      <template #header>
        <div class="card-header">
          <h3>订单支付</h3>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="房源ID">{{ order.houseId }}</el-descriptions-item>
        <el-descriptions-item label="月租金">¥{{ order.monthlyRent }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ order.deposit || 0 }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.orderStatus)">
            {{ getStatusText(order.orderStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="order.paymentStatus === 'PAID' ? 'success' : 'warning'">
            {{ order.paymentStatus === 'PAID' ? '已支付' : '未支付' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="payment-section" v-if="order.paymentStatus === 'UNPAID'">
        <el-divider>选择支付方式</el-divider>
        <el-radio-group v-model="paymentMethod" class="payment-methods">
          <el-radio label="alipay_web" border class="method-option">
            <div class="method-info">
              <div class="method-name">支付宝电脑网站支付</div>
            </div>
          </el-radio>
          <el-radio label="alipay_qr" border class="method-option">
            <div class="method-info">
              <div class="method-name">支付宝扫码支付</div> 
            </div>
          </el-radio>
        </el-radio-group>

        <div class="action-buttons">
          <el-button
            type="primary"
            size="large"
            @click="handlePay"
            :loading="loading"
            class="pay-button"
          >
            确认支付
          </el-button>
          <el-button size="large" @click="goBack">返回订单列表</el-button>
        </div>

        <!-- 支付宝扫码支付二维码 -->
        <div v-if="qrCodeUrl" class="qr-code-section">
          <el-divider>扫码支付</el-divider>
          <div class="qr-code-container">
            <img :src="qrCodeUrl" alt="支付宝支付二维码" class="qr-code" />
            <div class="qr-code-tips">
              <div class="tip-title">请使用支付宝APP扫描二维码</div>
              <div class="tip-time">
                剩余支付时间：{{ remainingTime }}秒
                <el-progress :percentage="progressPercentage" :stroke-width="6" :show-text="false" />
              </div>
              <div class="tip-note">订单将在{{ paymentTimeout / 1000 }}秒后超时</div>
            </div>
          </div>
        </div>

        <div class="sandbox-note">
          <el-icon>
            <WarningFilled />
          </el-icon>
          注意：这是支付宝沙箱测试环境，不会产生实际费用
        </div>
      </div>

      <div v-else class="paid-section">
        <el-result icon="success" title="支付成功" sub-title="您的订单已支付完成">
          <template #extra>
            <el-button type="primary" size="large" @click="goBack">查看订单列表</el-button>
          </template>
        </el-result>
      </div>
    </el-card>

    <!-- 订单加载失败或无效时显示 -->
    <div v-else class="error-section">
      <el-result
        icon="error"
        title="订单不存在或已失效"
        sub-title="请检查订单信息或返回订单列表"
      >
        <template #extra>
          <el-button type="primary" size="large" @click="goBack">返回订单列表</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElProgress } from 'element-plus'
import { WarningFilled } from '@element-plus/icons-vue'
import { createAlipayOrder, createAlipayQrCode } from '../../api/payment'
import request from '../../api/request'

// 状态管理
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)
const paymentMethod = ref('alipay_web')
const qrCodeUrl = ref(null)
const refreshTimer = ref(null)
const countdownTimer = ref(null)
const paymentTimeout = ref(180000) // 3分钟超时
const startTime = ref(null)
const currentTime = ref(Date.now())

// 验证订单ID是否有效
const isValidOrderId = (orderId) => {
  if (!orderId) return false
  if (typeof orderId === 'string' && orderId.toLowerCase() === 'undefined') return false
  if (isNaN(orderId)) return false
  const numOrderId = Number(orderId)
  return numOrderId > 0
}

// 获取当前订单ID
const getCurrentOrderId = () => {
  const orderId = route.params.orderId
  return isValidOrderId(orderId) ? Number(orderId) : null
}

// 加载订单信息
const loadOrder = async () => {
  const orderId = getCurrentOrderId()
  if (!orderId) {
    console.error('无效的订单ID:', route.params.orderId)
    ElMessage.error('无效的订单ID')
    goBack()
    return
  }

  try {
    const response = await request({
      url: `/tenant/orders/${orderId}`,
      method: 'get'
    })
    
    order.value = response
    
    // 如果订单已支付，停止刷新
    if (response.paymentStatus === 'PAID') {
      stopRefreshTimer()
      ElMessage.success('订单已支付')
    } else {
      // 订单未支付，启动刷新
      startRefreshTimer()
      startTime.value = Date.now()
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败，请检查网络连接或联系客服')
    goBack()
  }
}

// 主动查询订单支付状态
const queryPaymentStatus = async () => {
  const orderId = getCurrentOrderId()
  if (!orderId) return
  
  try {
    await request({
      url: `/payment/${orderId}/status`,
      method: 'get'
    })
    // 查询成功后重新加载订单信息
    await loadOrder()
  } catch (error) {
    // 静默处理错误，不显示错误信息
  }
}

// 开始定期刷新订单状态
const startRefreshTimer = () => {
  // 每3秒刷新一次订单状态
  if (!refreshTimer.value) {
    refreshTimer.value = setInterval(() => {
      queryPaymentStatus()
    }, 3000)
  }
}

// 停止定期刷新订单状态
const stopRefreshTimer = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

// 计算剩余支付时间
const remainingTime = computed(() => {
  if (!startTime.value) return paymentTimeout.value / 1000
  const elapsed = currentTime.value - startTime.value
  const remaining = Math.max(0, paymentTimeout.value - elapsed)
  return Math.ceil(remaining / 1000)
})

// 计算支付进度
const progressPercentage = computed(() => {
  if (!startTime.value) return 0
  const elapsed = currentTime.value - startTime.value
  const percentage = Math.min(100, (elapsed / paymentTimeout.value) * 100)
  return percentage
})

// 处理支付
const handlePay = async () => {
  const orderId = getCurrentOrderId()
  if (!orderId) {
    ElMessage.error('无效的订单ID')
    return
  }

  loading.value = true
  try {
    if (paymentMethod.value === 'alipay_web') {
      // 支付宝电脑网站支付
      const res = await createAlipayOrder(orderId)
      // 直接在当前页面打开支付表单
      document.write(res.form)
      document.close()
    } else if (paymentMethod.value === 'alipay_qr') {
        // 支付宝扫码支付
        const res = await createAlipayQrCode(orderId)
        // 生成二维码
        qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(res.qrCodeUrl)}`
        ElMessage.success('支付二维码已生成，请扫码支付')
        // 启动倒计时定时器
        startTime.value = Date.now()
        currentTime.value = Date.now()
        if (countdownTimer.value) {
          clearInterval(countdownTimer.value)
        }
        countdownTimer.value = setInterval(() => {
          currentTime.value = Date.now()
        }, 1000)
      }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message
    if (errorMsg?.includes('订单已支付') || errorMsg?.includes('TRADE_HAS_SUCCESS')) {
      ElMessage.success('订单已支付')
      await loadOrder()
    } else {
      ElMessage.error('支付失败: ' + errorMsg)
    }
  } finally {
    loading.value = false
  }
}

// 获取订单状态类型
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

// 获取订单状态文本
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

// 返回订单列表
const goBack = () => {
  router.push('/tenant/orders')
}

// 页面挂载时加载订单
onMounted(() => {
  loadOrder()
})

// 监听路由变化，确保订单ID变化时重新加载
watch(() => route.params.orderId, async () => {
  loadOrder()
})

// 组件卸载时清理资源
onUnmounted(() => {
  stopRefreshTimer()
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
})
</script>

<style scoped>
.payment-page {
  max-width: 800px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  padding: 20px;
  margin: 0 auto;
  margin-top: 80px;
}

.payment-card {
  flex: 1;
}

.card-header h3 {
  
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.payment-section {
  margin-top: 30px;
}

.payment-methods {
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
}

.method-option {
  flex: 1;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
}

.method-info {
  text-align: left;
}

.method-name {
  font-weight: 600;
  margin-bottom: 5px;
}


.action-buttons {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.pay-button {
  flex: 1;
}

.qr-code-section {
  margin-top: 30px;
}

.qr-code-container {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.qr-code {
  width: 250px;
  height: 250px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.qr-code-tips {
  max-width: 400px;
  margin: 0 auto;
}

.tip-title {
  font-weight: 600;
  margin-bottom: 15px;
}

.tip-time {
  margin-bottom: 10px;
}

.tip-note {
  font-size: 12px;
  color: #909399;
}

.sandbox-note {
  margin-top: 20px;
  padding: 10px 15px;
  background-color: #fffbe6;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  color: #d48806;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.paid-section {
  margin-top: 30px;
}

.error-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

