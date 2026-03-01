<template>
  <div class="house-map">
    <div ref="mapContainer" class="map-container"></div>
    
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>地图加载中...</p>
    </div>
    
    <div v-if="error" class="error-message">
      <div class="error-icon">❌</div>
      <p class="error-text">{{ errorMessage }}</p>
      <button @click="retryLoad" class="retry-btn">重试加载</button>
    </div>
    
    
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { mapService } from '@/services/map-service'
import { ElMessage } from 'element-plus'

export default {
  name: 'HouseMap',
  props: {
    houses: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const mapContainer = ref(null)
    const map = ref(null)
    const loading = ref(true)
    const error = ref(false)
    const errorMessage = ref('')
    const markers = ref([])
    const currentLocationMarker = ref(null)
    
    const defaultCenter = [116.404, 39.915] // [lng, lat]
    const defaultZoom = 12
    
    // 防抖函数
    const debounce = (func, delay) => {
      let timeoutId
      return (...args) => {
        clearTimeout(timeoutId)
        timeoutId = setTimeout(() => func.apply(null, args), delay)
      }
    }
    
    // 获取当前位置
    const getCurrentLocation = async () => {
      if (!navigator.geolocation) {
        ElMessage.warning('浏览器不支持定位功能')
        return
      }
      
      try {
        loading.value = true
        
        navigator.geolocation.getCurrentPosition(
          async (position) => {
            const { latitude, longitude } = position.coords
            
            if (map.value) {
              // 移动地图到当前位置
              map.value.setCenter([longitude, latitude])
              map.value.setZoom(15)
              
              // 在当前位置添加标记
              await addCurrentLocationMarker(latitude, longitude)
            }
            
            ElMessage.success('定位成功')
            loading.value = false
          },
          (error) => {
            console.error('定位失败:', error)
            ElMessage.error('定位失败，请检查浏览器定位权限')
            loading.value = false
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
          }
        )
      } catch (err) {
        console.error('定位错误:', err)
        ElMessage.error('定位失败')
        loading.value = false
      }
    }
    
    // 添加当前位置标记
    const addCurrentLocationMarker = async (latitude, longitude) => {
      if (!map.value) return
      
      // 先清除现有标记（如果有）
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null)
        currentLocationMarker.value = null
      }
      
      // 创建当前位置标记
      const AMap = await mapService.loadAMapScript()
      currentLocationMarker.value = new AMap.Marker({
        map: map.value,
        position: [longitude, latitude],
        icon: new AMap.Icon({
          size: new AMap.Size(30, 30),
          image: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/location.png',
          imageSize: new AMap.Size(30, 30)
        }),
        offset: new AMap.Pixel(-15, -15)
      })
    }
    
    const initMap = async () => {
      try {
        console.log('开始初始化地图...')
        
        loading.value = true
        error.value = false
        errorMessage.value = ''
        
        if (!mapContainer.value) {
          throw new Error('地图容器不存在')
        }
        
        const containerRect = mapContainer.value.getBoundingClientRect()
        if (!containerRect.width || !containerRect.height) {
          mapContainer.value.style.width = '100%'
          mapContainer.value.style.height = '360px'
        }
        
        console.log('地图容器有效，开始创建地图实例')
        
        // 使用mapService创建地图实例（包含缩放范围限制）
        map.value = await mapService.createMap(mapContainer.value, {
          zoom: defaultZoom,
          center: defaultCenter,
          viewMode: '2D'
        })
        
        // 加载AMap对象用于后续操作
        const AMap = await mapService.loadAMapScript()
        
        console.log('地图对象创建成功')
        
        // 添加缩放防抖处理
        const debouncedZoomHandler = debounce((e) => {
          console.log('地图缩放防抖处理，当前缩放级别:', map.value.getZoom())
          // 可以在这里添加缩放后的其他处理逻辑
        }, 300)
        
        // 监听地图缩放事件
        map.value.on('zoomend', debouncedZoomHandler)
        
        // 添加高德地图控件
        try {
          // 使用plugin方法加载多个控件
          const AMap = await mapService.loadAMapScript()
          AMap.plugin(['AMap.Scale', 'AMap.Geolocation', 'AMap.ToolBar', 'AMap.MapType'], () => {
            // 比例尺控件
            map.value.addControl(new AMap.Scale())
            
            // 定位控件 - 实现回到我的位置功能
            map.value.addControl(new AMap.Geolocation({
              enableHighAccuracy: true, // 高精度定位
              timeout: 10000, // 超时时间
              zoomToAccuracy: true, // 定位成功后自动缩放地图
              buttonPosition: 'RT' // 控件位置：右上角
            }))
            
            // 缩放控件 - 提供放大缩小功能
            map.value.addControl(new AMap.ToolBar({
              position: 'LT' // 控件位置：左上角
            }))
            
            // 地图类型切换控件
            map.value.addControl(new AMap.MapType({
              showTraffic: false, // 是否显示实时交通图层
              defaultType: 0, // 默认地图类型：0-矢量地图，1-卫星图，2-卫星混合图
              position: 'RT' // 控件位置：右上角
            }))
          })
        } catch (controlError) {
          console.error('添加控件失败:', controlError)
        }
        
        // 地图加载完成后添加房源标记
        await addHouseMarkers()
        
      } catch (err) {
        console.error('地图初始化失败:', err)
        error.value = true
        errorMessage.value = `地图加载失败: ${err.message}`
        
        if (map.value) {
          map.value.destroy()
          map.value = null
        }
        
      } finally {
        loading.value = false
      }
    }
    
    const retryLoad = async () => {
      await initMap()
    }
    
    // 清除现有标记
    const clearMarkers = () => {
      if (markers.value && markers.value.length > 0) {
        markers.value.forEach(marker => {
          marker.setMap(null)
        })
        markers.value = []
      }
    }
    
    // 添加房源标记
    const addHouseMarkers = async () => {
      if (!map.value) return
      
      try {
        const AMap = await mapService.loadAMapScript()
        
        // 清除现有标记
        clearMarkers()
        
        if (!props.houses || props.houses.length === 0) return
        
        // 准备标记点数据并创建标记
        const markerList = props.houses
          .filter(house => (house.lng && house.lat) || (house.longitude && house.latitude)) // 过滤掉没有坐标的房源
          .map(house => {
            // 处理不同的坐标字段名
            const lng = house.lng || house.longitude
            const lat = house.lat || house.latitude
            
            // 创建标记
            const marker = new AMap.Marker({
              map: map.value,
              position: [lng, lat],
              icon: new AMap.Icon({
                size: new AMap.Size(25, 35),
                image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
                imageSize: new AMap.Size(25, 35)
              }),
              offset: new AMap.Pixel(-12, -35),
              title: house.title
            })
            
            // 存储房源信息
            marker.houseInfo = {
              id: house.id,
              title: house.title,
              price: house.price,
              address: house.address,
              area: house.area,
              bedrooms: house.bedrooms,
              livingRooms: house.livingRooms
            }
            
            // 创建信息窗口
            const infoWindow = new AMap.InfoWindow({
              isCustom: false, // 使用默认样式
              content: '', // 内容将在点击时动态设置
              offset: new AMap.Pixel(0, -30) // 调整信息窗口偏移量
            })
            
            // 添加点击事件
            marker.on('click', () => {
              const { id, title, price, address, area, bedrooms, livingRooms } = marker.houseInfo
              console.log(`点击了房源: ${id} - ${title}, 价格: ${price}`)
              
              // 设置信息窗口内容
              const content = `
                <div style="padding: 10px; min-width: 250px;">
                  <h4 style="margin: 0 0 10px 0; color: #333;">${title || '房源信息'}</h4>
                  <p style="margin: 5px 0; color: #666;"><strong>价格:</strong> ${price ? `${price}元` : '暂无数据'}</p>
                  <p style="margin: 5px 0; color: #666;"><strong>面积:</strong> ${area ? `${area}㎡` : '暂无数据'}</p>
                  <p style="margin: 5px 0; color: #666;"><strong>户型:</strong> ${bedrooms && livingRooms ? `${bedrooms}室${livingRooms}厅` : '暂无数据'}</p>
                  <p style="margin: 5px 0; color: #666;"><strong>地址:</strong> ${address || '暂无数据'}</p>
                  <p style="margin: 10px 0 5px 0; color: #999; font-size: 12px;">房源ID: ${id}</p>
                </div>
              `
              
              // 设置信息窗口内容并打开
              infoWindow.setContent(content)
              infoWindow.open(map.value, marker.getPosition())
            })
            
            return marker
          })
        
        markers.value = markerList
        
        console.log(`已添加 ${markerList.length} 个房源标记`)
        
      } catch (err) {
        console.error('添加房源标记失败:', err)
      }
    }
    
    onMounted(async () => {
      await nextTick()
      await initMap()
    })
    
    // 监听房源数据变化，更新标记
    watch(
      () => props.houses,
      async () => {
        if (map.value) {
          await addHouseMarkers()
        }
      },
      { deep: true }
    )
    
    onBeforeUnmount(() => {
      // 清理标记
      clearMarkers()
      
      // 清理当前位置标记
      if (currentLocationMarker.value) {
        currentLocationMarker.value.setMap(null)
        currentLocationMarker.value = null
      }
      
      // 销毁地图
      if (map.value) {
        map.value.destroy()
        map.value = null
      }
    })
    
    return {
      mapContainer,
      loading,
      error,
      errorMessage,
      retryLoad
    }
  }
}
</script>

<style scoped>
.house-map {
  width: 100%;
  height: 460px;
  margin-bottom: 16px;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e6eb;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  color: #f56c6c;
  padding: 20px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-text {
  margin-bottom: 20px;
  font-size: 14px;
  max-width: 80%;
}

.retry-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #40a9ff;
  transform: translateY(-1px);
}



</style>