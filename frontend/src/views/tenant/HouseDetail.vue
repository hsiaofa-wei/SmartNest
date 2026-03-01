<template>
  <div class="house-detail" v-if="house">
    <el-row :gutter="20">
      <el-col :xs="24" :md="16">
        <el-card>
          <div class="house-images">
            <el-carousel height="400px" v-if="images && images.length > 0">
              <el-carousel-item v-for="(image, index) in images" :key="index">
                <div class="carousel-image-wrapper">
                  <img :src="image.imageUrl" alt="房源图片" @error="handleImageError($event)" />
                </div>
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">暂无图片</div>
          </div>
           
          <div class="house-content">
            <h1>{{ house.title }}</h1>
            <div class="house-meta">
              <span class="price">¥{{ house.price }}/月</span>
              <span class="area">{{ house.area }}㎡</span>
              <span class="address">{{ house.address }}</span>
            </div>
            <div class="house-description">
              <h3>房源描述</h3>
              <p>{{ house.description }}</p>
            </div>
            <div class="house-details" v-if="detail">
              <h3>房源详情</h3>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="楼层">{{ detail.floor }}/{{ detail.totalFloors }}</el-descriptions-item>
                <el-descriptions-item label="户型">{{ detail.roomType }}</el-descriptions-item>
                <el-descriptions-item label="朝向">{{ detail.orientation }}</el-descriptions-item>
                <el-descriptions-item label="装修">{{ detail.decoration }}</el-descriptions-item>
                <el-descriptions-item label="电梯">{{ detail.hasElevator ? '有' : '无' }}</el-descriptions-item>
                <el-descriptions-item label="停车位">{{ detail.hasParking ? '有' : '无' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="action-card">
          <div class="action-buttons">
            <el-button
              type="primary"
              size="large"
              :icon="isFavorited ? 'StarFilled' : 'Star'"
              @click="toggleFavorite"
              style="width: 100%"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="handleRent"
              style="width: 100%; "
            >
              立即租房
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="showAppointmentDialog = true"
              style="width: 100%"
            >
              预约看房
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAppointmentDialog" title="预约看房" width="500px">
      <el-form :model="appointmentForm" label-width="100px">
        <el-form-item label="预约时间">
          <el-date-picker
            v-model="appointmentForm.appointmentTime"
            type="datetime"
            placeholder="选择预约时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            :disabled-time="disabledTime"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="appointmentForm.contactPhone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="appointmentForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAppointmentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getHouseDetail } from '../../api/house'
import request from '../../api/request'
import { mapService } from '../../services/map-service'

export default {
  name: 'HouseDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const house = ref(null)
    const detail = ref(null)
    const images = ref([])
    const isFavorited = ref(false)
    const showAppointmentDialog = ref(false)
    
    // 地图相关
    const mapContainer = ref(null)
    const map = ref(null)
    const marker = ref(null)
    const loading = ref(false)

    const appointmentForm = ref({
      appointmentTime: null,
      contactPhone: '',
      remark: ''
    })

    // 禁止选择今天以前的日期
    const disabledDate = (time) => {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      return time.getTime() < today.getTime()
    }

    // 禁止选择当前时间以前的时间（如果选择的是今天）
    const disabledTime = (date) => {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      
      // 如果选择的是今天
      if (date && date.getFullYear() === today.getFullYear() &&
          date.getMonth() === today.getMonth() &&
          date.getDate() === today.getDate()) {
        const currentHour = today.getHours()
        const currentMinute = today.getMinutes()
        return {
          disabledHours: () => {
            // 禁用当前小时之前的所有小时
            return Array.from({length: currentHour}, (_, i) => i)
          },
          disabledMinutes: (selectedHour) => {
            // 如果选择了当前小时，禁用当前分钟之前的所有分钟
            if (selectedHour === currentHour) {
              return Array.from({length: currentMinute}, (_, i) => i)
            }
            return []
          }
        }
      }
      return {}
    }

    const userId = computed(() => store.state.auth.user?.userId)

    const loadHouseDetail = async () => {
      try {
        const res = await getHouseDetail(route.params.id)
        house.value = res.house
        detail.value = res.detail
        images.value = res.images || []
        
        // 加载房源数据后初始化地图
        if (house.value.longitude && house.value.latitude) {
          await initMap()
        }
      } catch (error) {
        ElMessage.error('加载房源详情失败')
      }
    }
    
    // 图片加载失败处理函数
    const handleImageError = (event) => {
      // 创建一个新的div元素来显示"暂无图片"
      const errorDiv = document.createElement('div')
      errorDiv.className = 'no-image'
      errorDiv.textContent = '暂无图片'
      
      // 替换失败的图片
      const imgElement = event.target
      const parentElement = imgElement.parentElement
      if (parentElement) {
        parentElement.replaceChild(errorDiv, imgElement)
      }
    }
    
    // 初始化地图
    const initMap = async () => {
      if (!mapContainer.value || !house.value.longitude || !house.value.latitude) {
        return
      }
      
      try {
        const AMap = await mapService.loadAMapScript()
        
        // 使用mapService创建地图实例
        map.value = await mapService.createMap(mapContainer.value, {
          zoom: 15,
          center: [parseFloat(house.value.longitude), parseFloat(house.value.latitude)], // 高德地图使用[lng, lat]顺序
        })
        
        // 添加房源标记
        updateMarker(AMap)
        
        // 添加缩放防抖处理
        const debounce = (func, delay) => {
          let timeoutId
          return (...args) => {
            clearTimeout(timeoutId)
            timeoutId = setTimeout(() => func.apply(null, args), delay)
          }
        }
        
        const debouncedZoomHandler = debounce((e) => {
          console.log('地图缩放防抖处理，当前缩放级别:', map.value.getZoom())
        }, 500)
        
        // 监听地图缩放事件
        map.value.on('zoomend', debouncedZoomHandler)
        
      } catch (error) {
        console.error('地图初始化失败:', error)
      }
    }
    
    // 更新标记点
    const updateMarker = (AMap) => {
      if (!map.value || !house.value.longitude || !house.value.latitude) {
        return
      }
      
      // 移除现有标记
      if (marker.value) {
        map.value.remove(marker.value)
      }
      
      // 创建新标记
      marker.value = new AMap.Marker({
        position: [parseFloat(house.value.longitude), parseFloat(house.value.latitude)],
        map: map.value,
        icon: new AMap.Icon({
          size: new AMap.Size(25, 35),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          imageSize: new AMap.Size(25, 35),
          anchor: new AMap.Pixel(12, 35)
        })
      })
      
      // 添加信息窗口
      const infoWindow = new AMap.InfoWindow({
        content: `
          <div style="padding: 10px;">
            <strong>${house.value.title}</strong><br>
            价格：¥${house.value.price}/月<br>
            地址：${house.value.address}
          </div>
        `,
        offset: new AMap.Pixel(0, -30)
      })
      
      // 点击标记显示信息窗口
      marker.value.on('click', () => {
        infoWindow.open(map.value, marker.value.getPosition())
      })
      
      // 自动打开信息窗口
      infoWindow.open(map.value, marker.value.getPosition())
    }

    const toggleFavorite = async () => {
      if (!userId.value) {
        ElMessage.warning('请先登录')
        return
      }
      try {
        await request({
          url: `/tenant/favorites/${route.params.id}`,
          method: 'post',
          params: { tenantId: userId.value }
        })
        isFavorited.value = !isFavorited.value
        ElMessage.success(isFavorited.value ? '收藏成功' : '取消收藏成功')
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    const handleRent = () => {
      if (!userId.value) {
        ElMessage.warning('请先登录')
        return
      }
      router.push(`/tenant/orders/create/${route.params.id}`)
    }

    const submitAppointment = async () => {
      if (!userId.value) {
        ElMessage.warning('请先登录')
        return
      }
      if (!appointmentForm.value.appointmentTime) {
        ElMessage.warning('请选择预约时间')
        return
      }
      if (!appointmentForm.value.contactPhone || !appointmentForm.value.contactPhone.trim()) {
        ElMessage.warning('请输入联系电话')
        return
      }
      try {
        const apptTime = new Date(appointmentForm.value.appointmentTime).toISOString()
        await request({
          url: '/tenant/appointments',
          method: 'post',
          data: {
            tenantId: userId.value,
            houseId: route.params.id,
            landlordId: house.value.landlordId,
            appointmentTime: apptTime,
            contactPhone: appointmentForm.value.contactPhone.trim(),
            remark: appointmentForm.value.remark
          }
        })
        ElMessage.success('预约成功')
        showAppointmentDialog.value = false
      } catch (error) {
        ElMessage.error('预约失败')
      }
    }

    // 清理地图资源
    onBeforeUnmount(() => {
      if (marker.value) {
        marker.value.remove()
        marker.value = null
      }
      if (map.value) {
        map.value.destroy()
        map.value = null
      }
    })

    onMounted(() => {
      loadHouseDetail()
    })

    return {
      house,
      detail,
      images,
      isFavorited,
      showAppointmentDialog,
      appointmentForm,
      toggleFavorite,
      handleRent,
      submitAppointment,
      disabledDate,
      disabledTime,
      handleImageError
    }
  }
}
</script>

<style scoped>
.house-detail {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
}

.house-images {
  margin-bottom: 20px;
}

.house-images img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-image-wrapper {
  width: 100%;
  height: 400px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-image {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
}

.house-content h1 {
  margin: 0 0 20px 0;
  color: #333;
}

.house-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.house-meta .price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.house-meta .area,
.house-meta .address {
  color: #666;
  font-size: 16px;
}

.house-description,
.house-details {
  margin-top: 30px;
}

.house-description h3,
.house-details h3 {
  margin-bottom: 15px;
  color: #333;
}

.action-card {
  position: sticky;
  top: 20px;
}

@media (max-width: 768px) {
  .house-images {
    height: 250px;
  }
  
  .action-card {
    position: static;
    margin-top: 20px;
  }
}

/* 地图样式 */
.house-map {
  margin-top: 30px;
  margin-bottom: 30px;
}

.house-map h3 {
  margin-bottom: 15px;
  color: #333;
}

.map-container {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e6eb;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-buttons .el-button {
  margin: 0 !important;
  padding: 12px 19px !important;
}
</style>

