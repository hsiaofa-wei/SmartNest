<template>
  <div class="create-order">
    <el-card v-if="house">
      <template #header>
        <span>创建订单</span>
      </template>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="房源标题">{{ house.title }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ house.address }}</el-descriptions-item>
        <el-descriptions-item label="月租金">¥{{ house.price }}/月</el-descriptions-item>
        <el-descriptions-item label="面积">{{ house.area }}㎡</el-descriptions-item>
      </el-descriptions>

      <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="120px" style="margin-top: 20px">
        <el-form-item label="租赁开始日期" prop="startDate">
          <el-date-picker
            v-model="orderForm.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            :disabled-date="disabledStartDate"
          />
        </el-form-item>

        <el-form-item label="租赁结束日期" prop="endDate">
          <el-date-picker
            v-model="orderForm.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            :disabled-date="disabledEndDate"
          />
        </el-form-item>

        <el-form-item label="月租金">
          <el-input-number v-model="orderForm.monthlyRent" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="押金">
          <el-input-number v-model="orderForm.deposit" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <el-form-item label="总金额">
          <el-input-number v-model="totalAmount" :min="0" :precision="2" style="width: 100%" disabled />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="orderForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">
            创建订单
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getHouseDetail } from '../../api/house'
import { createOrder } from '../../api/payment'
import request from '../../api/request'

export default {
  name: 'CreateOrder',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const orderFormRef = ref(null)
    const loading = ref(false)
    const house = ref(null)
    const hasConfirmedAppointment = ref(false)

    const orderForm = ref({
      startDate: null,
      endDate: null,
      monthlyRent: 0,
      deposit: 0,
      remark: ''
    })

    const rules = {
      startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
      endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
    }

    const totalAmount = computed(() => {
      if (!orderForm.value.startDate || !orderForm.value.endDate) {
        return 0
      }
      const start = new Date(orderForm.value.startDate)
      const end = new Date(orderForm.value.endDate)
      const months = (end - start) / (1000 * 60 * 60 * 24 * 30)
      return parseFloat((orderForm.value.monthlyRent * months + (orderForm.value.deposit || 0)).toFixed(2))
    })
    
    // 检查金额是否超过沙箱限额（10000元）
    const isAmountExceeded = computed(() => {
      return totalAmount.value > 10000
    })

    const disabledStartDate = (time) => {
      return time.getTime() < Date.now()
    }

    const disabledEndDate = (time) => {
      if (!orderForm.value.startDate) return true
      return time.getTime() <= orderForm.value.startDate.getTime()
    }

    const checkAppointmentStatus = async () => {
      try {
        const userId = store.state.auth.user?.userId
        if (!userId) {
          ElMessage.warning('请先登录')
          router.push('/login')
          return false
        }
        
        const res = await request({
          url: '/tenant/appointments',
          method: 'get',
          params: {
            tenantId: userId,
            houseId: route.params.houseId,
            status: 'CONFIRMED'
          }
        })
        
        hasConfirmedAppointment.value = res.content && res.content.length > 0
        return hasConfirmedAppointment.value
      } catch (error) {
        console.error('检查预约状态失败:', error)
        ElMessage.error('检查预约状态失败')
        return false
      }
    }
    
    const loadHouse = async () => {
      try {
        const res = await getHouseDetail(route.params.houseId)
        house.value = res.house
        orderForm.value.monthlyRent = parseFloat(house.value.price)
        
        // 检查预约状态
        await checkAppointmentStatus()
      } catch (error) {
        ElMessage.error('加载房源信息失败')
        router.back()
      }
    }

    const handleSubmit = async () => {
      if (!orderFormRef.value) return
      
      // 检查金额是否超过沙箱限额
      if (isAmountExceeded.value) {
        ElMessage.error('订单总金额超过支付宝沙箱限额（10000元），请减少租赁周期或降低金额')
        return
      }
      
      // 再次检查预约状态
      const canCreateOrder = await checkAppointmentStatus()
      if (!canCreateOrder) {
        ElMessage.error('请先完成房源预约并等待房东确认')
        return
      }
      
      await orderFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const userId = store.state.auth.user?.userId
            const formatDate = (date) => {
              const d = new Date(date)
              const year = d.getFullYear()
              const month = String(d.getMonth() + 1).padStart(2, '0')
              const day = String(d.getDate()).padStart(2, '0')
              return `${year}-${month}-${day}`
            }
            
            const orderData = {
              tenantId: userId,
              landlordId: house.value.landlordId,
              houseId: house.value.id,
              startDate: formatDate(orderForm.value.startDate),
              endDate: formatDate(orderForm.value.endDate),
              monthlyRent: orderForm.value.monthlyRent,
              totalAmount: totalAmount.value.toFixed(2),
              deposit: orderForm.value.deposit ? orderForm.value.deposit.toFixed(2) : '0.00',
              remark: orderForm.value.remark
            }
            
            const res = await createOrder(orderData)
            ElMessage.success('订单创建成功')
            router.push(`/tenant/payment/${res.order.id}`)
          } catch (error) {
            ElMessage.error('创建订单失败: ' + (error.response?.data?.message || error.message))
          } finally {
            loading.value = false
          }
        }
      })
    }

    onMounted(() => {
      loadHouse()
    })

    return {
      orderFormRef,
      loading,
      house,
      orderForm,
      rules,
      totalAmount,
      disabledStartDate,
      disabledEndDate,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.create-order {
  max-width: 800px;
  padding: 20px;
  margin: 0 auto;
  margin-top: 80px;
}
</style>

