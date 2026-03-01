<template>
  <div class="edit-house">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <span>编辑房源</span>
          <el-button type="text" icon="ArrowLeft" @click="handleBack">
            返回
          </el-button>
        </div>
      </template>

      <el-form :model="houseForm" :rules="rules" ref="houseFormRef" label-width="120px">
        <el-form-item label="房源标题" prop="title">
          <el-input v-model="houseForm.title" placeholder="请输入房源标题" />
        </el-form-item>

        <el-form-item label="房源描述" prop="description">
          <el-input
            v-model="houseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入房源描述"
            resize="none"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="月租金(元)" prop="price">
              <el-input-number v-model="houseForm.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(㎡)" prop="area">
              <el-input-number v-model="houseForm.area" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="houseForm.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="房源位置">
          <!-- 地图容器 -->
          <div ref="mapContainer" class="map-container"></div>
          <p class="map-hint">点击地图选择房源位置，或搜索地址</p>
        </el-form-item>

        <el-form-item label="搜索地址">
          <el-autocomplete
            v-model="searchAddress"
            :fetch-suggestions="querySearchAsync"
            placeholder="请输入地址"
            :trigger-on-focus="false"
            @select="handleSelectAddress"
          >
            <template #suffix>
              <el-button @click="searchLocation" :loading="searchLoading">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-autocomplete>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份">
              <el-input v-model="houseForm.province" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市">
              <el-input v-model="houseForm.city" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区/县">
              <el-input v-model="houseForm.district" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input v-model="houseForm.longitude" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input v-model="houseForm.latitude" placeholder="自动生成" readonly />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="房源图片">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleImageSuccess"
            :on-remove="handleImageRemove"
            :file-list="imageList"
            :limit="10"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-divider>房源详情</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所在楼层">
              <el-input-number v-model="houseForm.floor" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总楼层">
              <el-input-number v-model="houseForm.totalFloors" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="户型">
              <el-input v-model="houseForm.roomType" placeholder="如一室一厅、两室一厅" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="朝向">
              <el-select v-model="houseForm.orientation" placeholder="选择朝向" style="width: 100%">
                <el-option label="南" value="南" />
                <el-option label="北" value="北" />
                <el-option label="东" value="东" />
                <el-option label="西" value="西" />
                <el-option label="东南" value="东南" />
                <el-option label="西南" value="西南" />
                <el-option label="东北" value="东北" />
                <el-option label="西北" value="西北" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="装修情况">
          <el-select v-model="houseForm.decoration" placeholder="选择装修情况" style="width: 100%">
            <el-option label="精装" value="精装" />
            <el-option label="简装" value="简装" />
            <el-option label="毛坯" value="毛坯" />
          </el-select>
        </el-form-item>

        <el-form-item label="配套设施">
          <el-checkbox-group v-model="houseForm.facilities">
            <el-checkbox value="hasElevator">电梯</el-checkbox>
            <el-checkbox value="hasParking">停车位</el-checkbox>
            <el-checkbox value="hasFurniture">家具</el-checkbox>
            <el-checkbox value="hasAirConditioner">空调</el-checkbox>
            <el-checkbox value="hasWashingMachine">洗衣机</el-checkbox>
            <el-checkbox value="hasRefrigerator">冰箱</el-checkbox>
            <el-checkbox value="hasWifi">WiFi</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">
            保存修改
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Plus, Search, ArrowLeft } from '@element-plus/icons-vue'
import request from '../../api/request'
import { mapService } from '@/services/map-service'

const houseId = ref(null)

export default {
  name: 'HouseEdit',
  components: {
    Plus,
    Search,
    ArrowLeft
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const houseFormRef = ref(null)
    const loading = ref(false)
    const imageList = ref([])
    const mapContainer = ref(null)
    const map = ref(null)
    const marker = ref(null)
    const searchAddress = ref('')
    const searchLoading = ref(false)
    const mapLoaded = ref(false)
    const addressOptions = ref([])
    const autoComplete = ref(null)

    // 获取路由参数中的houseId
    houseId.value = route.params.id

    // 腾讯地图API密钥（已在map-service.js中配置）

    const houseForm = ref({
      title: '',
      description: '',
      price: null,
      area: null,
      address: '',
      province: '',
      city: '',
      district: '',
      longitude: null,
      latitude: null,
      floor: null,
      totalFloors: null,
      roomType: '',
      orientation: '',
      decoration: '',
      facilities: []
    })

    const rules = {
      title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
      price: [{ required: true, message: '请输入月租金', trigger: 'blur' }],
      area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
      address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
    }

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

    const handleImageSuccess = (response) => {
      if (response.url) {
        imageList.value.push({
          url: response.url,
          name: response.fileName
        })
      }
    }

    const handleImageRemove = (file) => {
      const index = imageList.value.findIndex(item => item.url === file.url)
      if (index > -1) {
        imageList.value.splice(index, 1)
      }
    }

    // 加载房源详情
    const loadHouseDetail = async () => {
      if (!houseId.value) return

      try {
        loading.value = true
        const res = await request({
          url: `/houses/${houseId.value}`,
          method: 'get'
        })

        if (res && res.data) {
          const house = res.data
          // 填充表单数据
          houseForm.value.title = house.title
          houseForm.value.description = house.description
          houseForm.value.price = house.price
          houseForm.value.area = house.area
          houseForm.value.address = house.address
          houseForm.value.province = house.province
          houseForm.value.city = house.city
          houseForm.value.district = house.district
          houseForm.value.longitude = house.longitude
          houseForm.value.latitude = house.latitude
          houseForm.value.floor = house.floor
          houseForm.value.totalFloors = house.totalFloors
          houseForm.value.roomType = house.roomType
          houseForm.value.orientation = house.orientation
          houseForm.value.decoration = house.decoration

          // 处理设施
          const facilities = []
          if (house.hasElevator) facilities.push('hasElevator')
          if (house.hasParking) facilities.push('hasParking')
          if (house.hasFurniture) facilities.push('hasFurniture')
          if (house.hasAirConditioner) facilities.push('hasAirConditioner')
          if (house.hasWashingMachine) facilities.push('hasWashingMachine')
          if (house.hasRefrigerator) facilities.push('hasRefrigerator')
          if (house.hasWifi) facilities.push('hasWifi')
          houseForm.value.facilities = facilities

          // 处理图片
          if (house.imageUrls && house.imageUrls.length > 0) {
            imageList.value = house.imageUrls.map((url, index) => ({
              url,
              name: `house_${houseId.value}_${index}`
            }))
          }


        }
      } catch (error) {
        console.error('加载房源详情失败:', error)
        ElMessage.error('加载房源详情失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    const handleSubmit = async () => {
      if (!houseFormRef.value || !houseId.value) return
      
      await houseFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const formData = { ...houseForm.value }
            
            // 处理设施
            const facilities = houseForm.value.facilities || []
            formData.hasElevator = facilities.includes('hasElevator')
            formData.hasParking = facilities.includes('hasParking')
            formData.hasFurniture = facilities.includes('hasFurniture')
            formData.hasAirConditioner = facilities.includes('hasAirConditioner')
            formData.hasWashingMachine = facilities.includes('hasWashingMachine')
            formData.hasRefrigerator = facilities.includes('hasRefrigerator')
            formData.hasWifi = facilities.includes('hasWifi')
            delete formData.facilities
            
            // 添加图片URL
            formData.imageUrls = imageList.value.map(img => img.url)
            
            const res = await request({
              url: `/houses/${houseId.value}`,
              method: 'put',
              data: formData
            })
            
            ElMessage.success('房源更新成功')
            router.push('/landlord/houses')
          } catch (error) {
            console.error('更新房源失败:', error)
            ElMessage.error('更新房源失败，请稍后重试')
          } finally {
            loading.value = false
          }
        }
      })
    }
    // 初始化地图
    const initMap = async (retryCount = 0) => {
      if (!mapContainer.value) return
      
      // 检查容器尺寸
      const containerRect = mapContainer.value.getBoundingClientRect()
      if (containerRect.width === 0 || containerRect.height === 0) {
        // 如果容器尺寸为0，1秒后重试
        if (retryCount < 2) {
          setTimeout(() => initMap(retryCount + 1), 1000 * Math.pow(2, retryCount))
        } else {
          console.error('地图容器尺寸为0，无法初始化地图')
          ElMessage.error('地图容器尺寸无效，请检查页面布局')
        }
        return
      }
      
      try {
        const AMap = await mapService.loadAMapScript()
        
        // 使用mapService创建地图实例（包含缩放范围限制）
        map.value = await mapService.createMap(mapContainer.value, {
          zoom: 11,
          center: [116.404, 39.915], // 默认北京坐标
          viewMode: '2D'
        })
        
        // 添加地图工具
        AMap.plugin(['AMap.Scale'], () => {
          map.value.addControl(new AMap.Scale())
        })
        
        // 添加点击事件
        map.value.on('click', handleMapClick)
        
        // 添加缩放防抖处理
        const debouncedZoomHandler = debounce((e) => {
          console.log('地图缩放防抖处理，当前缩放级别:', map.value.getZoom())
          // 可以在这里添加缩放后的其他处理逻辑
        }, 300)
        
        // 监听地图缩放事件
        map.value.on('zoomend', debouncedZoomHandler)
        
        mapLoaded.value = true
      } catch (error) {
        console.error('地图初始化失败:', error)
        // 重试逻辑
        if (retryCount < 2) {
          // 指数退避延迟
          const delay = 1000 * Math.pow(2, retryCount)
          setTimeout(() => initMap(retryCount + 1), delay)
        } else {
          ElMessage.error('地图加载失败，请稍后重试')
        }
      }
    }

    // 处理地图点击事件
    const handleMapClick = (e) => {
      if (!map.value) return
      
      const lng = e.lnglat.getLng()
      const lat = e.lnglat.getLat()
      
      // 更新坐标
      houseForm.value.longitude = lng
      houseForm.value.latitude = lat
      
      // 更新标记点
      updateMarker(lng, lat)
      
      // 逆地理编码获取地址
      getAddressFromCoords(lng, lat)
    }

    // 更新标记点
    const updateMarker = async (lng, lat) => {
      if (!map.value) return
      
      // 移除现有标记
      if (marker.value) {
        map.value.remove(marker.value)
      }
      
      // 创建新标记
      const AMap = await mapService.loadAMapScript()
      marker.value = new AMap.Marker({
        position: [lng, lat],
        title: '房源位置'
      })
      
      map.value.add(marker.value)
    }

    // 逆地理编码获取地址
    const getAddressFromCoords = async (lng, lat) => {
      try {
        const AMap = await mapService.loadAMapScript()
        
        AMap.plugin('AMap.Geocoder', () => {
          const geocoder = new AMap.Geocoder()
          geocoder.getAddress([lng, lat], (status, result) => {
            if (status === 'complete' && result.regeocode) {
              const address = result.regeocode.formattedAddress
              const addressComponent = result.regeocode.addressComponent
              
              // 更新地址信息
              houseForm.value.address = address
              houseForm.value.province = addressComponent.province
              houseForm.value.city = addressComponent.city
              houseForm.value.district = addressComponent.district
            }
          })
        })
      } catch (error) {
        console.error('获取地址信息失败:', error)
      }
    }

    // 地址搜索建议
    const querySearchAsync = async (queryString, cb) => {
      if (!queryString) {
        cb([])
        return
      }
      
      searchLoading.value = true
      
      try {
        const AMap = await mapService.loadAMapScript()
        
        AMap.plugin('AMap.Autocomplete', () => {
          const autoComplete = new AMap.Autocomplete({
            city: '', // 全国范围搜索
            datatype: 'poi'
          })
          
          autoComplete.search(queryString, (status, result) => {
            searchLoading.value = false
            
            if (status === 'complete' && result.tips) {
              addressOptions.value = result.tips.map(tip => ({
                value: tip.name,
                address: tip.address,
                location: tip.location,
                district: tip.district || '',
                city: tip.city || '',
                province: tip.province || ''
              }))
              cb(addressOptions.value)
            } else {
              cb([])
            }
          })
        })
      } catch (error) {
        console.error('地址搜索建议失败:', error)
        searchLoading.value = false
        cb([])
      }
    }

    // 处理地址选择
    const handleSelectAddress = (option) => {
      if (option.location && map.value) {
        const lng = option.location.lng
        const lat = option.location.lat
        
        // 更新坐标
        houseForm.value.longitude = lng
        houseForm.value.latitude = lat
        
        // 更新地图视图和标记
        map.value.setCenter([lng, lat])
        map.value.setZoom(15)
        updateMarker(lng, lat)
        
        // 使用逆地理编码获取准确的地址信息
        getAddressFromCoords(lng, lat)
      }
    }

    // 搜索地址
    const searchLocation = async () => {
      if (!searchAddress.value) return
      
      searchLoading.value = true
      
      try {
        const AMap = await mapService.loadAMapScript()
        
        AMap.plugin('AMap.Geocoder', () => {
          const geocoder = new AMap.Geocoder({
            city: '', // 全国范围搜索
            radius: 1000
          })
          
          geocoder.getLocation(searchAddress.value, (status, result) => {
            searchLoading.value = false
            
            if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
              const geocode = result.geocodes[0]
              const lng = geocode.location.getLng()
              const lat = geocode.location.getLat()
              
              // 更新坐标
              houseForm.value.longitude = lng
              houseForm.value.latitude = lat
              
              // 更新地址信息
              houseForm.value.address = geocode.formattedAddress
              houseForm.value.province = geocode.province
              houseForm.value.city = geocode.city
              houseForm.value.district = geocode.district
              
              // 更新地图视图和标记
              map.value.setCenter([lng, lat])
              map.value.setZoom(15)
              updateMarker(lng, lat)
            } else {
              ElMessage.error('未找到该地址')
            }
          })
        })
      } catch (error) {
        console.error('地址搜索失败:', error)
        searchLoading.value = false
        ElMessage.error('地址搜索失败，请重试')
      }
    }

    // 防抖函数
    const debounce = (func, delay) => {
      let timeoutId
      return (...args) => {
        clearTimeout(timeoutId)
        timeoutId = setTimeout(() => func.apply(null, args), delay)
      }
    }

    const handleBack = () => {
      router.back()
    }

    const handleReset = () => {
      // 重置表单数据为当前房源的原始数据
      loadHouseDetail()
      searchAddress.value = ''
      
      // 清除地图标记
      if (marker.value && map.value) {
        map.value.remove(marker.value)
        marker.value = null
      }
    }

    // 初始化AutoComplete实例
    const initAutoComplete = async () => {
      try {
        const AMap = await mapService.loadAMapScript()
        
        AMap.plugin('AMap.AutoComplete', () => {
          autoComplete.value = new AMap.AutoComplete({
            city: '', // 全国范围搜索
            datatype: 'poi'
          })
        })
      } catch (error) {
        console.error('初始化AutoComplete失败:', error)
      }
    }

    // 组件挂载后初始化地图和Autocomplete
    onMounted(async () => {
      await nextTick()
      if (mapContainer.value) {
        initMap()
      }
      await initAutoComplete()
      
      // 加载房源详情
      await loadHouseDetail()
    })

    return {
      houseFormRef,
      loading,
      houseForm,
      rules,
      imageList,
      uploadUrl,
      uploadHeaders,
      handleImageSuccess,
      handleImageRemove,
      handleSubmit,
      handleReset,
      handleBack,
      // 地图相关
      mapContainer,
      searchAddress,
      searchLoading,
      querySearchAsync,
      handleSelectAddress,
      searchLocation
    }
  }
}
</script>

<style scoped>
.edit-house {
  padding: 20px;
}

.map-container {
  width: 100%;
  height: 400px;
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.map-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 400px;
  margin-bottom: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f7fa;
  color: #606266;
}

.map-loading .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.map-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 400px;
  margin-bottom: 10px;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 20px;
}

.map-error .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.map-error span {
  flex: 1;
  text-align: center;
  margin-right: 10px;
}

.map-hint {
  margin: 5px 0 15px 0;
  color: #909399;
  font-size: 12px;
}
</style>
