<template>
  <el-container class="tenant-layout">
    <!-- 顶部导航栏 -->
    <el-header class="top-header">
      <div class="left-section">
        <div class="logo">
          <h2>智能租房平台</h2>
          <p class="slogan">真实房源 · 安全交易 · 便捷生活</p>
        </div>
        <div class="weather-info" v-if="weatherData" >
          <img :src="weatherIconUrl" alt="weather icon" class="weather-icon" />
          <span class="weather-text">{{ weatherData.lives[0]?.weather }}</span>
          <span class="temperature">{{ weatherData.lives[0]?.temperature }}°C</span>
          <span class="city-name">{{ weatherData.lives[0]?.city }}</span>
        </div>
        <!-- 新增导航容器 -->
        <div class="nav-container" >
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="main-menu" 
          >
            <el-menu-item index="/tenant/houses">房源浏览</el-menu-item>
            <el-menu-item index="/tenant/favorites" v-if="isLoggedIn">我的收藏</el-menu-item>
            <el-menu-item index="/tenant/appointments" v-if="isLoggedIn">我的预约</el-menu-item>
            <el-menu-item index="/tenant/orders" v-if="isLoggedIn">我的订单</el-menu-item>
            <el-menu-item index="/tenant/profile" v-if="isLoggedIn">个人信息</el-menu-item> 
          </el-menu>
        </div>
      </div>
      <div class="user-area">
        <!-- 用户未登录时显示注册和登录按钮 -->
        <div v-if="!isLoggedIn" class="auth-buttons">
          <el-button type="primary" @click="registerDialogVisible = true">立即注册</el-button>
          <el-button @click="$router.push('/login')">登录</el-button>
        </div>
        <!-- 用户已登录时显示用户信息 -->
        <el-dropdown v-else @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userInfo.avatarUrl" :size="50">{{ userInfo.username?.charAt(0) }}</el-avatar>
            <span style="margin: 8px;font-size: 20px;">{{ userInfo.username }}</span>
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
    <el-main class="main-content">
      <router-view />
    </el-main>
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于我们</h3>
          <p>智能租房平台是一家专注于提供高品质租房服务的互联网平台，致力于为用户打造安全、便捷、透明的租房体验。</p>
          <div class="contact-info">
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>客服热线：023-105-3291</span>
            </div>
            <div class="contact-item">
              <el-icon><Mail /></el-icon>
              <span>邮箱：hisaofawei@gmail.com</span>
            </div>
            <div class="contact-item">
              <el-icon><Location /></el-icon>
              <span>地址：平顶山市新华区平顶山学院湖滨校区</span>
            </div>
          </div>
        </div>
       
        <div class="footer-section">
          <h3>租房服务</h3>
          <ul class="footer-nav">
            <li class="t"><a href="/tenant/houses">房源列表</a></li>
            <li class="t"><a href="/tenant/favorites">我的收藏</a></li>
            <li class="t"><a href="/tenant/appointments">预约看房</a></li>
            <li class="t"><a href="/tenant/orders">我的订单</a></li> 
          </ul>
        </div>
        <div class="footer-section">
          <h3>关注我们</h3>
          <ul class="footer-nav">
            <li class="t"><a >QQ:231053291</a></li>
            <li class="t"><a >微博:@智能租房平台</a></li>
            <li class="t"><a >微信:231053291</a></li>
          </ul>
          <div class="copyright">
            <p class="t">© 2025 智能租房平台 版权所有</p>
            <p class="t">备案号：京ICP备12345678号-1</p>
          </div>
        </div>
      </div>
    </el-footer>
    <FloatingActions />
  </el-container>
</template>

<script>
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

import FloatingActions from '../components/FloatingActions.vue'
import RegisterForm from '../components/RegisterForm.vue'

export default {
  name: 'TenantLayout',
  components: { FloatingActions, ArrowDown, RegisterForm },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    const activeMenu = computed(() => route.path.split('/').slice(0, 3).join('/'))
    const userInfo = computed(() => store.state.auth.user || {})
    const isLoggedIn = computed(() => !!store.state.auth.user?.username)
    
    // 天气相关
    const weatherData = ref(null)
    const loadingWeather = ref(false)
    
    
    
    const handleCommand = (command) => {
      if (command === 'logout') {
        ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          zIndex: 3000
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

    // 注册弹窗相关
    const registerDialogVisible = ref(false)
    const registerFormRef = ref(null)

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

    // 天气图标URL（使用OpenWeatherMap直接返回的图标代码）
    const weatherIconUrl = computed(() => {
      if (!weatherData.value?.lives[0]?.icon) return ''
      
      // 使用OpenWeatherMap直接返回的图标代码
      const iconCode = weatherData.value.lives[0].icon
      
      // 使用OpenWeatherMap的图标服务，确保URL格式正确
      const iconUrl = `https://openweathermap.org/img/wn/${iconCode}.png`
      console.log('天气图标URL:', iconUrl)
      
      return iconUrl
    })
    
    // 获取用户当前位置（优化版）
    const getUserLocation = () => {
      return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
          reject(new Error('浏览器不支持地理定位'))
          return
        }
        
        // 高精度配置
        const options = {
          enableHighAccuracy: true,    // 启用高精度定位
          timeout: 15000,              // 延长超时时间到15秒
          maximumAge: 0                // 不使用缓存位置
        }
        
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const location = {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
              accuracy: position.coords.accuracy,  // 定位精度（米）
              altitude: position.coords.altitude,
              heading: position.coords.heading,
              speed: position.coords.speed
            }
            
            console.log('获取到精确位置:', location)
            console.log('定位精度:', location.accuracy, '米')
            console.log('纬度:', location.latitude, '经度:', location.longitude)
            
            resolve(location)
          },
          (error) => {
            const errorMessages = {
              1: '用户拒绝了位置权限',
              2: '无法获取位置信息（请检查GPS/WiFi）',
              3: '位置请求超时',
              0: '未知错误'
            }
            
            const errorMsg = errorMessages[error.code] || '位置获取失败'
            console.error('位置获取失败:', errorMsg, error)
            
            reject(new Error(`${errorMsg} (错误码: ${error.code})`))
          },
          options
        )
      })
    }
    
    // 根据经纬度获取真实位置名称（优先使用高德逆地理编码获取精确位置）
    const getLocationName = async (latitude, longitude) => {
      try {
        // 首先尝试使用高德逆地理编码获取精确位置
        const amapKey = '';
        const url = `https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${longitude},${latitude}&extensions=base&output=JSON`;
        
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();
        
        console.log('高德逆地理编码获取位置名称:', data);
        
        if (data.status === '1' && data.regeocode) {
          // 优先返回城市名称
          const addressComponent = data.regeocode.addressComponent;
          let locationName = addressComponent.city || addressComponent.province || addressComponent.district;
          
          // 过滤无效位置
          if (locationName && !isInvalidLocation(locationName)) {
            console.log('逆地理编码获取位置成功:', locationName);
            return locationName;
          }
        }
        
        // 如果逆地理编码失败或返回无效位置，尝试IP定位
        console.log('逆地理编码失败或返回无效位置，尝试IP定位');
        const ipCity = await getIPLocation();
        
        if (ipCity && ipCity !== '未知位置' && !isInvalidLocation(ipCity)) {
          console.log('IP定位成功:', ipCity);
          return ipCity;
        }
        
        // 如果所有方法都失败，使用默认城市名称
        console.log('所有定位方法失败，使用默认城市');
        return '未知城市';
        
      } catch (error) {
        console.error('获取位置名称失败:', error);
        
        // 异常情况下尝试IP定位
        try {
          const ipCity = await getIPLocation();
          if (ipCity && !isInvalidLocation(ipCity)) {
            return ipCity;
          }
        } catch (ipError) {
          console.error('IP定位也失败:', ipError);
        }
        
        return '未知城市';
      }
    }
    
    // 检查位置名称是否为无效位置（如"Jiaodian"等）
    const isInvalidLocation = (locationName) => {
      if (!locationName) return true
      
      // 转换为小写进行比较，确保不区分大小写
      const lowerLocationName = locationName.toLowerCase()
      
      // 无效位置关键词列表，仅包含明确无效的位置名称
      const invalidKeywords = [
        'jiaodian',      // 焦点（无效位置）
        '焦点',          // 中文焦点
        'unknown',       // 未知
        '未知位置',      // 中文未知位置
        'unknown location' // 未知位置
      ]
      
      // 检查是否包含无效关键词
      const containsInvalidKeyword = invalidKeywords.some(keyword => 
        lowerLocationName.includes(keyword.toLowerCase()) || 
        lowerLocationName === keyword.toLowerCase()
      )
      
      // 检查是否为纯数字或特殊字符
      const isNumericOrSpecial = /^[0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]*$/.test(locationName)
      
      // 检查是否为空或仅包含空格
      const isEmptyOrSpace = locationName.trim() === ''
      
      return containsInvalidKeyword || isNumericOrSpecial || isEmptyOrSpace
    }
    
    // IP定位备选方案
    const getIPLocation = async () => {
      try {
        // 尝试使用多个IP定位服务，提高成功率
        const services = [
          {
            url: 'https://api.ip.sb/geoip',
            parse: (data) => ({ city: data.city, region: data.region, country: data.country })
          },
          {
            url: 'https://ipinfo.io/json',
            parse: (data) => ({ city: data.city, region: data.region, country: data.country })
          },
          {
            url: 'https://ipapi.co/json/',
            parse: (data) => ({ city: data.city, region: data.region_name, country: data.country_name })
          },
          {
            url: 'https://ifconfig.co/json',
            parse: (data) => ({ city: data.city, region: data.region, country: data.country_name })
          }
        ]
        
        for (const service of services) {
          try {
            const response = await fetch(service.url, { timeout: 5000 })
            const data = await response.json()
            
            // 解析响应数据
            const locationData = service.parse(data)
            const { city, region, country } = locationData
            
            if (city && country) {
              console.log('IP定位成功:', {
                city: city,
                region: region,
                country: country,
                service: service.url
              })
              
              // 直接返回城市名称，不再过滤
              return city
            } else if (region && country) {
              console.log('IP定位只返回了地区信息:', {
                region: region,
                country: country,
                service: service.url
              })
              return region
            }
          } catch (e) {
            console.log(`IP服务${service.url}失败，尝试下一个:`, e.message)
            continue
          }
        }
        
        throw new Error('所有IP定位服务都失败了')
        
      } catch (error) {
        console.error('IP定位失败:', error)
        return '未知位置'
      }
    }
    
    // 通过经纬度获取城市编码
    const getCityCodeByCoords = async (lat, lon) => {
      const amapKey = '';
      const url = `https://restapi.amap.com/v3/geocode/regeo?key=${amapKey}&location=${lon},${lat}&extensions=base&output=JSON`;
      
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();
        
        console.log('高德逆地理编码API返回数据:', data);
        
        if (data.status === '1' && data.regeocode) {
          // 返回城市编码
          return data.regeocode.addressComponent.adcode;
        } else {
          throw new Error(`获取城市编码失败: status=${data.status}, info=${data.info || '未知'}`);
        }
      } catch (error) {
        console.error('逆地理编码失败:', error);
        throw error;
      }
    };
    
    // 通过经纬度查询高德地图天气API
    const getWeatherByCoords = async (lat, lon) => {
      const amapKey = '';
      
      try {
        // 先通过经纬度获取城市编码
        const cityCode = await getCityCodeByCoords(lat, lon);
        console.log('获取到城市编码:', cityCode);
        
        // 使用城市编码查询天气
        const url = `https://restapi.amap.com/v3/weather/weatherInfo?key=${amapKey}&city=${cityCode}&extensions=base&output=JSON`;
        
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();
        
        console.log('高德天气API返回数据:', data);
        console.log('API请求URL:', url);
        
        if (data.status === '1' && data.lives && data.lives.length > 0) {
          const liveWeather = data.lives[0];
          return {
            weather: liveWeather.weather, // 天气状况（中文）
            temperature: parseFloat(liveWeather.temperature), // 温度，保留原始小数
            city: liveWeather.city, // 城市名称（中文）
            icon: getAmapWeatherIcon(liveWeather.weather) // 自定义图标映射
          };
        } else {
          // 添加更详细的错误信息
          const errorMsg = `获取天气数据失败: status=${data.status}, info=${data.info || '未知'}, infocode=${data.infocode || '未知'}`;
          console.error('天气API调用失败详情:', errorMsg);
          
          // 检查是否是密钥问题
          if (data.infocode === '10001' || data.info?.includes('key')) {
            console.error('高德地图API密钥可能无效或没有天气服务权限');
            // 尝试使用IP定位的城市名结合OpenWeatherMap API
            return await getWeatherByCityName();
          }
          
          throw new Error(errorMsg);
        }
      } catch (error) {
        console.error('坐标查询失败:', error);
        // 尝试使用IP定位的城市名获取天气
        try {
          return await getWeatherByCityName();
        } catch (e) {
          throw error;
        }
      }
    };
    
    // 通过城市名称获取天气（备用方案）
    const getWeatherByCityName = async () => {
      try {
        // 获取IP定位的城市名称
        const cityName = await getIPLocation();
        if (isInvalidLocation(cityName)) {
          throw new Error('无法获取有效城市名称');
        }
        
        // 使用模拟数据但基于真实城市名
        return {
          weather: '晴',
          temperature: (Math.random() * 10 + 15).toFixed(1), // 随机温度15-25°C，保留1位小数
          city: cityName,
          icon: '01d'
        };
      } catch (error) {
        console.error('通过城市名获取天气失败:', error);
        throw error;
      }
    };
    
    // 高德地图天气状况到图标代码的映射
    const getAmapWeatherIcon = (weather) => {
      // 高德地图天气状况到OpenWeatherMap图标代码的映射
      const iconMap = {
        '晴': '01d',
        '多云': '02d',
        '阴': '03d',
        '阵雨': '09d',
        '雷阵雨': '11d',
        '雷阵雨伴有冰雹': '11d',
        '雨夹雪': '13d',
        '小雨': '10d',
        '中雨': '10d',
        '大雨': '10d',
        '暴雨': '10d',
        '大暴雨': '10d',
        '特大暴雨': '10d',
        '阵雪': '13d',
        '小雪': '13d',
        '中雪': '13d',
        '大雪': '13d',
        '暴雪': '13d',
        '雾': '50d',
        '霾': '50d'
      };
      return iconMap[weather] || '01d';
    };
    
    // 模拟天气数据
    const getMockWeather = (cityName) => {
      return {
        weather: '晴天',
        temperature: 22.5,
        city: cityName,
        icon: '01d'
      };
    };
    
    // 根据经纬度获取天气信息（使用高德地图天气API）
    const getWeatherByLocation = async (latitude, longitude) => {
      loadingWeather.value = true;
      try {
        let weatherResult;
        let preciseLocationName = '未知城市';
        
        // 1. 首先尝试使用用户实时坐标获取天气数据
        try {
          // 同时获取精确位置名称
          preciseLocationName = await getLocationName(latitude, longitude);
          weatherResult = await getWeatherByCoords(latitude, longitude);
          console.log('使用用户实时坐标获取天气成功:', weatherResult);
        } catch (coordsError) {
          console.log('用户坐标查询失败，尝试使用默认坐标');
          
          // 2. 坐标失败，尝试使用默认坐标
          try {
            const DEFAULT_COORDS = { lat: 39.9042, lon: 116.4074 }; // 北京坐标
            weatherResult = await getWeatherByCoords(DEFAULT_COORDS.lat, DEFAULT_COORDS.lon);
            console.log('使用默认坐标获取天气成功:', weatherResult);
          } catch (defaultCoordsError) {
            console.log('默认坐标查询失败，尝试使用城市名称获取天气');
            
            // 3. 尝试使用城市名称获取天气
            try {
              weatherResult = await getWeatherByCityName();
              console.log('使用城市名称获取天气成功:', weatherResult);
            } catch (cityError) {
              console.log('城市名称查询失败，使用模拟数据');
              // 所有方案都失败，使用模拟数据
              weatherResult = getMockWeather(preciseLocationName);
            }
          }
        }
        
        // 设置天气数据，确保格式与现有模板兼容
        weatherData.value = {
          lives: [{
            weather: weatherResult.weather,
            temperature: parseFloat(weatherResult.temperature), // 确保温度是数字类型
            city: preciseLocationName, // 使用精确获取的位置名称
            icon: weatherResult.icon
          }]
        };
        
      } catch (error) {
        console.error('获取天气信息失败:', error);
        
        // 异常情况下，使用默认天气数据
        weatherData.value = {
          lives: [{
            weather: '晴',
            temperature: 20,
            city: '未知城市',
            icon: '01d'
          }]
        };
      } finally {
        loadingWeather.value = false;
      }
    }
    
    onMounted(async () => {
      try {
        // 获取用户位置并获取天气信息
        const location = await getUserLocation()
        console.log('浏览器定位成功，精度:', location.accuracy, '米')
        
        // 如果定位精度较高（小于500米），直接使用该坐标获取天气和位置
        if (location.accuracy < 500) {
          console.log('定位精度较高，使用精确坐标')
          await getWeatherByLocation(location.latitude, location.longitude)
        } else {
          console.log('定位精度一般，尝试进一步优化')
          // 定位精度一般，尝试通过逆地理编码获取更精确的位置名称
          const preciseLocationName = await getLocationName(location.latitude, location.longitude)
          const weatherResult = await getWeatherByCoords(location.latitude, location.longitude)
          
          // 设置天气数据，使用精确位置名称
          weatherData.value = {
            lives: [{
              weather: weatherResult.weather,
              temperature: parseFloat(weatherResult.temperature),
              city: preciseLocationName,
              icon: weatherResult.icon
            }]
          }
        }
      } catch (error) {
        console.error('获取位置失败:', error)
        
        try {
          // 第一步回退：尝试获取IP位置信息
          console.log('尝试使用IP定位获取位置')
          const ipLocationResult = await getIPLocation()
          
          if (!isInvalidLocation(ipLocationResult)) {
            console.log('IP定位成功，使用该城市名称:', ipLocationResult)
            // 使用模拟数据但基于真实城市名
            weatherData.value = {
              lives: [{
                weather: '晴',
                temperature: (Math.random() * 10 + 15).toFixed(1),
                city: ipLocationResult,
                icon: '01d'
              }]
            }
          } else {
            // 第二步回退：使用默认坐标和精确位置获取
            console.log('IP定位返回无效位置，尝试使用默认坐标获取天气')
            await getWeatherByLocation(39.9042, 116.4074)
          }
        } catch (ipError) {
          console.error('IP定位也失败:', ipError)
          
          // 第三步回退：使用默认天气数据
          console.log('使用默认天气数据')
          weatherData.value = {
            lives: [{
              weather: '晴',
              temperature: 20,
              city: '未知城市',
              icon: '01d'
            }]
          };
        }
      }
    })

    return {
      activeMenu,
      userInfo,
      isLoggedIn,
      handleCommand,
      // 注册弹窗相关
      registerDialogVisible,
      registerFormRef,
      handleRegisterSuccess,
      handleDialogClosed,
      // 天气相关
      weatherData,
      loadingWeather,
      weatherIconUrl
    }
  }
}
</script>

<style scoped>
.tenant-layout {
  min-height: 120vh;
  /* background: #f7da94; */
  background-image: url("C:\Users\Lenovo\Desktop\picture3.jpg");
  background-size: 100% auto;
  background-position: top center;
  background-repeat: no-repeat;
  background-attachment: scroll;  
}

.header {
  background: rgba(255, 255, 255, 0.8);
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
  gap: 20px; 
}

.header-menu {
  border: none; 
}

.user-info {  
  display: flex;
  align-items: center;
  cursor: pointer;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

.main-content {
   padding: 20px;
   margin-top: 80px;
   background-color: #f9f4dc;
}

@media (max-width: 768px) {
  .header {
 
  padding-top: 1000px;

    height: auto;
    padding: 10px;
  }
  
  .header-menu {
    display: none;
  }
  
  .logo h2 {
    font-size: 16px;
  }
}


/* 页脚样式 */
.footer {
  background-color: #333;
  color: #fff;
  padding: 40px 0 20px;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  flex-wrap: wrap;
}

.footer-section {
  flex: 1;
  min-width: 250px;
  margin-bottom: 20px;
  padding: 0 20px;
}

.footer-section h3 {
  color: #409eff;
  margin-bottom: 20px;
  margin-top: -15px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.footer-section p {
  line-height: 1.6;
  color: #ccc;
  margin-bottom: 15px;
}

.footer-nav {
  list-style: none;
  padding: 0;
}

.footer-nav li {
  margin-bottom: 10px;
}

.footer-nav a {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-nav a:hover {
  color: #409eff;
}

.contact-info {
  margin-top: 20px;
}

.contact-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #ccc;
}

.contact-item .el-icon {
  margin-right: 10px;
  color: #409eff;
}

.social-links {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.social-links .el-button {
  background-color: #409eff;
  border-color: #409eff;
}

.copyright {
  color: #999;
  font-size: 12px;
}

.copyright p {
  margin: 5px 0;
}

/* 搜索区域样式 */
.search-section {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 40px 0;
}

.search-box {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 150px;
}

.search-item label {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.search-item .el-select {
  width: 100%;
}

.search-item .el-input, .search-item .el-select .el-input {
  border-radius: 4px;
  overflow: hidden;
}

.search-btn {
  flex-shrink: 0;
  margin-top: 20px;
}

/* 顶部导航栏样式 */
.top-header { 
  padding: 20px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  width: 100%;
}

.left-section {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  flex: 1;
  flex-wrap: wrap;
}

.logo {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center; 
  flex-wrap: wrap;
}

.logo h2 {
  padding-left: 120px;
  margin: 0;
  color: #409eff;
  font-size: 26px;
  font-weight: 700;
}

/* 天气信息样式 */
.weather-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  color: #303133;
  font-size: 20px;
}

.weather-icon {
  width: 60px auto;  
}

.weather-text { 
  color: #606266;
}

.temperature {
  color: #409eff;
  font-size: 28px;
  font-weight: 500;
}

.city-name {
  color: #909399;
  font-size: 20px;
}

.logo .slogan {
  padding-left: 120px;
  margin: 5px 0 0;
  color: #909399;
  font-size: 20px;
}

/* 新增导航容器样式 */
.nav-container {
  margin-left: 220px;
  width: 120%;
  margin-top: 20px;
}

.user-area  {
  display: flex;
  align-items: center;
  gap: 0px;
  padding-right: 120px; /* 水平方向调整 */
  padding-bottom: 120px; /* 垂直方向调整 */
}


.main-menu {
  border: none;
  background: none;
  height: 60px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.main-menu .el-menu-item {
  font-size: 18px;
  margin: 20px;
  font-weight: 500;
  color: #303133; 
}

.main-menu .el-menu-item.is-active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 100%;
  }
}

@media (max-width: 992px) {
  .search-box {
    flex-direction: column;
    gap: 20px;
  }
  
  .search-item {
    min-width: 100%;
  }
  
  .search-btn {
    margin-top: 0;
  }
  
  .footer-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .footer-section {
    padding: 0;
  }
}
 .t{
  text-align: center;
 }
@media (max-width: 768px) {
  .top-nav {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .main-menu {
    justify-content: center;
  }
  
  .main-menu .el-menu-item { 
    font-size: 14px;
    padding: 0 10px;
  }
}
</style>