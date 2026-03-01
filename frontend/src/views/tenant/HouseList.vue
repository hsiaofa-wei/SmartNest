<template>
  <div class="house-list">
    <HouseMap :houses="mapHouses" />
    
    <!-- 搜索功能容器 -->
    <div class="search-container">
      <!-- 热门城市选择 -->
      <div class="hot-cities-section">
        <h3 class="section-title">热门城市</h3>
        <div class="city-tags">
          <el-tag @click="viewAllHouses" type="primary" effect="plain">查看全部房源</el-tag>
          <el-tag v-for="city in hotCities" :key="city" @click="selectCity(city)">{{ city }}</el-tag>
        </div>
      </div>
      
      <!-- 省份-城市选择 -->
      <div class="province-city-section">
        <h3 class="section-title">按地区选择</h3>
        <div class="location-selector">
          <el-select v-model="selectedProvince" placeholder="选择省份" @change="handleProvinceChange" clearable>
            <el-option v-for="province in provinces" :key="province.name" :label="province.name" :value="province.name" />
          </el-select>
          <el-select v-model="selectedCity" placeholder="选择城市" @change="handleCityChange" clearable>
            <el-option v-for="city in cities" :key="city" :label="city" :value="city" />
          </el-select>
        </div>
      </div>
      
      <!-- 文本搜索 -->
      <div class="text-search-section">
        <h3 class="section-title">搜索房源</h3>
        <el-input
          v-model="searchParams.keyword"
          placeholder="搜索房源标题、描述或地址"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      
      <!-- 价格筛选 -->
      <div class="price-filter-section">
        <h3 class="section-title">价格筛选</h3>
        <div class="price-filter-container">
          <el-input-number
            v-model="searchParams.minPrice"
            :min="0"
            :precision="0"
            placeholder="最低价格"
            clearable
          >
            <template #append>¥</template>
          </el-input-number>
          <span class="price-separator">-</span>
          <el-input-number
            v-model="searchParams.maxPrice"
            :min="0"
            :precision="0"
            placeholder="最高价格"
            clearable
          >
            <template #append>¥</template>
          </el-input-number>
          <el-button type="primary" @click="handleSearch" style="margin-left: 16px;">
            <el-icon><Search /></el-icon>
            筛选
          </el-button>
        </div>
      </div>
      
      <!-- 房源列表 -->
      <div class="house-list-section">
        <h3 class="section-title">房源列表</h3>
        <div class="house-list-container">
          <el-empty v-if="houseList.length === 0" description="暂无房源信息" />
          <div v-for="house in houseList" :key="house.id" class="house-item" @click="handleHouseClick(house.id)" style="cursor: pointer;">
            <div class="house-image-container">
            <img v-if="house.mainImageUrl" :src="house.mainImageUrl" alt="房源图片" class="house-image" />
            <div v-else class="no-house-image">暂无图片</div>
          </div>
            <div class="house-info">
              <h4 class="house-title">{{ house.title }}</h4>
              <p class="house-address">{{ house.address }}</p>
              <p class="house-price">¥{{ house.price }}/月</p>
              <p class="house-details">{{ house.area }}㎡ | {{ house.bedrooms }}室{{ house.bathrooms }}厅</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { searchHouses } from '../../api/house'
import HouseMap from '../../components/HouseMap.vue'
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'HouseList',
  components: { HouseMap },
  setup() {
    const router = useRouter()
    
    // 热门城市数据
    const hotCities = ref(['北京市', '上海市', '广州市', '深圳市', '杭州市', '成都市', '武汉市', '平顶山市','西安市'])
    
    // 省份数据 - 包含全国所有省份和直辖市
    const provinces = ref([
      // 直辖市 - 城市名称改为"市辖区"避免与省份重名
      { name: '北京市', cities: ['市辖区'] },
      { name: '上海市', cities: ['市辖区'] },
      { name: '天津市', cities: ['市辖区'] },
      { name: '重庆市', cities: ['市辖区'] },
      // 省份
      { name: '河北省', cities: ['石家庄市', '唐山市', '邯郸市', '秦皇岛市', '保定市', '张家口市'] },
      { name: '山西省', cities: ['太原市', '大同市', '阳泉市', '长治市', '晋城市', '朔州市'] },
      { name: '辽宁省', cities: ['沈阳市', '大连市', '鞍山市', '抚顺市', '本溪市', '丹东市'] },
      { name: '吉林省', cities: ['长春市', '吉林市', '四平市', '辽源市', '通化市', '白山市'] },
      { name: '黑龙江省', cities: ['哈尔滨市', '齐齐哈尔市', '鸡西市', '鹤岗市', '双鸭山市', '大庆市'] },
      { name: '江苏省', cities: ['南京市', '无锡市', '徐州市', '常州市', '苏州市', '南通市'] },
      { name: '浙江省', cities: ['杭州市', '宁波市', '温州市', '嘉兴市', '湖州市', '绍兴市'] },
      { name: '安徽省', cities: ['合肥市', '芜湖市', '蚌埠市', '淮南市', '马鞍山市', '淮北市'] },
      { name: '福建省', cities: ['福州市', '厦门市', '莆田市', '三明市', '泉州市', '漳州市'] },
      { name: '江西省', cities: ['南昌市', '景德镇市', '萍乡市', '九江市', '新余市', '鹰潭市'] },
      { name: '山东省', cities: ['济南市', '青岛市', '淄博市', '枣庄市', '东营市', '烟台市'] },
      { name: '河南省', cities: ['郑州市', '开封市', '洛阳市', '平顶山市', '安阳市', '鹤壁市'] },
      { name: '湖北省', cities: ['武汉市', '黄石市', '十堰市', '宜昌市', '襄阳市', '鄂州市'] },
      { name: '湖南省', cities: ['长沙市', '株洲市', '湘潭市', '衡阳市', '邵阳市', '岳阳市'] },
      { name: '广东省', cities: ['广州市', '深圳市', '珠海市', '汕头市', '佛山市', '韶关市'] },
      { name: '广西壮族自治区', cities: ['南宁市', '柳州市', '桂林市', '梧州市', '北海市', '防城港市'] },
      { name: '海南省', cities: ['海口市', '三亚市', '三沙市', '儋州市'] },
      { name: '四川省', cities: ['成都市', '自贡市', '攀枝花市', '泸州市', '德阳市', '绵阳市'] },
      { name: '贵州省', cities: ['贵阳市', '六盘水市', '遵义市', '安顺市', '毕节市', '铜仁市'] },
      { name: '云南省', cities: ['昆明市', '曲靖市', '玉溪市', '保山市', '昭通市', '丽江市'] },
      { name: '西藏自治区', cities: ['拉萨市', '日喀则市', '昌都市', '林芝市'] },
      { name: '陕西省', cities: ['西安市', '铜川市', '宝鸡市', '咸阳市', '渭南市', '延安市'] },
      { name: '甘肃省', cities: ['兰州市', '嘉峪关市', '金昌市', '白银市', '天水市', '武威市'] },
      { name: '青海省', cities: ['西宁市', '海东市', '海北藏族自治州', '黄南藏族自治州'] },
      { name: '宁夏回族自治区', cities: ['银川市', '石嘴山市', '吴忠市', '固原市', '中卫市'] },
      { name: '新疆维吾尔自治区', cities: ['乌鲁木齐市', '克拉玛依市', '吐鲁番市', '哈密市'] },
      { name: '内蒙古自治区', cities: ['呼和浩特市', '包头市', '乌海市', '赤峰市', '通辽市', '鄂尔多斯市'] },
      { name: '台湾省', cities: ['台北市', '新北市', '桃园市', '台中市', '台南市', '高雄市'] },
      { name: '香港特别行政区', cities: ['香港特别行政区'] },
      { name: '澳门特别行政区', cities: ['澳门特别行政区'] }
    ])
    
    // 选择的省份和城市列表
    const selectedProvince = ref('')
    const selectedCity = ref('')
    const cities = ref([])
    
    // 搜索参数
    const searchParams = ref({
      keyword: '',
      city: '',
      province: '',
      minPrice: undefined,
      maxPrice: undefined
    })
    
    // 分页数据
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 房源列表
    const houseList = ref([])
    
    // 地图显示的房源（过滤掉没有经纬度的房源）
    const mapHouses = computed(() =>
      (houseList.value || []).filter(h => h.latitude && h.longitude)
    )
    
    // 加载房源
    const loadHouses = async () => {
      try {
        // 构建请求参数
        const params = {
          ...searchParams.value,
          page: currentPage.value - 1, // 后端从0开始计数
          size: pageSize.value // 后端参数名是size而不是pageSize
        }
        
        console.log('请求参数:', JSON.stringify(params))
        const res = await searchHouses(params)
        console.log('API响应:', JSON.stringify(res))
        
        // 检查res的结构
        if (res && Array.isArray(res)) {
          // 如果直接返回数组
          houseList.value = res
          total.value = res.length
        } else if (res && res.content) {
          // 如果返回分页对象
          houseList.value = res.content
          total.value = res.totalElements || res.content.length
        } else {
          // 其他情况
          console.error('API返回数据结构异常:', res)
          houseList.value = []
          total.value = 0
        }
        
        console.log('房源列表:', houseList.value)
      } catch (error) {
        console.error('加载房源失败', error)
        // 错误时也确保显示空状态
        houseList.value = []
        total.value = 0
      }
    }
    
    // 查看全部房源
    const viewAllHouses = () => {
      // 重置搜索参数
      searchParams.value = {
        keyword: '',
        city: '',
        province: '',
        minPrice: undefined,
        maxPrice: undefined
      }
      selectedProvince.value = ''
      selectedCity.value = ''
      cities.value = []
      currentPage.value = 1
      
      // 重新加载房源
      loadHouses()
    }
    
    // 选择城市
    const selectCity = (city) => {
      // 查找对应的省份数据（检查该城市属于哪个省份）
      const cityProvince = provinces.value.find(p => p.cities.includes(city))
      
      if (cityProvince && cityProvince.cities.length === 1 && cityProvince.cities[0] === '市辖区') {
        // 如果是直辖市（城市列表只有一个"市辖区"）
        searchParams.value.province = cityProvince.name
        searchParams.value.city = ''
        // 设置选中的省份
        selectedProvince.value = cityProvince.name
        // 清空城市选择
        selectedCity.value = ''
        // 设置城市列表
        cities.value = cityProvince.cities
      } else if (cityProvince) {
        // 普通省份的城市
        searchParams.value.province = cityProvince.name
        searchParams.value.city = city
        // 设置选中的省份和城市
        selectedProvince.value = cityProvince.name
        selectedCity.value = city
        // 设置城市列表
        cities.value = cityProvince.cities
      } else {
        // 直接从热门城市选择的情况
        const directCityProvince = provinces.value.find(p => p.name === city)
        if (directCityProvince && directCityProvince.cities.length === 1 && directCityProvince.cities[0] === '市辖区') {
          // 如果是直辖市
          searchParams.value.province = directCityProvince.name
          searchParams.value.city = ''
          selectedProvince.value = directCityProvince.name
          selectedCity.value = ''
          cities.value = directCityProvince.cities
        } else {
          // 普通城市
          searchParams.value.city = city
          searchParams.value.province = ''
          selectedProvince.value = ''
          selectedCity.value = city
          cities.value = []
        }
      }
      
      currentPage.value = 1
      
      // 重新加载房源
      loadHouses()
    }
    
    // 省份变化处理
    const handleProvinceChange = (province) => {
      // 查找对应的省份数据
      const selected = provinces.value.find(p => p.name === province)
      if (selected) {
        // 设置城市列表
        cities.value = selected.cities
        // 设置省份参数
        searchParams.value.province = province
        
        // 检查是否为直辖市（城市列表只有一个"市辖区"）
        if (selected.cities.length === 1 && selected.cities[0] === '市辖区') {
          // 如果是直辖市，只设置省份，不设置城市
          searchParams.value.city = ''
          selectedCity.value = ''
        } else {
          // 清空城市选择
          searchParams.value.city = ''
          selectedCity.value = ''
        }
      } else {
        cities.value = []
        searchParams.value.province = ''
        searchParams.value.city = ''
        selectedCity.value = ''
      }
      // 重新加载房源
      loadHouses()
    }
    
    // 城市变化处理
    const handleCityChange = (city) => {
      selectedCity.value = city
      
      // 检查当前省份是否为直辖市（城市列表只有一个"市辖区"）
      const currentProvince = selectedProvince.value
      const isDirectCity = provinces.value.some(p => p.name === currentProvince && p.cities.length === 1 && p.cities[0] === '市辖区')
      
      if (isDirectCity && city === '市辖区') {
        // 对于直辖市，当选择"市辖区"时，保持city参数为空
        searchParams.value.city = ''
      } else {
        searchParams.value.city = city
      }
      
      // 重新加载房源
      handleSearch()
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadHouses()
    }
    
    // 分页大小变化处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadHouses()
    }
    
    // 页码变化处理
    const handlePageChange = (page) => {
      currentPage.value = page
      loadHouses()
    }
    
    // 处理房源点击
    const handleHouseClick = (houseId) => {
      router.push(`/tenant/houses/${houseId}`)
    }
    
    // 组件挂载时加载房源
    onMounted(() => {
      loadHouses()
    })
    
    return {
      // 热门城市
      hotCities,
      
      // 省份城市
      provinces,
      selectedProvince,
      selectedCity,
      cities,
      
      // 搜索参数
      searchParams,
      
      // 分页
      currentPage,
      pageSize,
      total,
      
      // 房源列表
      houseList,
      mapHouses,
      
      // 方法
      viewAllHouses,
      selectCity,
      handleProvinceChange,
      handleCityChange,
      handleSearch,
      handleSizeChange,
      handlePageChange,
      handleHouseClick,
      
      // 图标
      Search
    }
  }
}
</script>

<style scoped>
.house-list {  
  max-width: 1300px;
  margin: 100px auto 0; 
  padding: 10px;
  background-color: #f9fafb;
  min-height: 100vh;
}

/* 搜索功能容器 */
.search-container {
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 20px;
}

/* 标题样式 */
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}

/* 热门城市部分 */
.hot-cities-section {
  margin-bottom: 24px;
}

.city-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 省份-城市选择部分 */
.province-city-section {
  margin-bottom: 24px;
}

.location-selector {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.location-selector .el-select {
  min-width: 180px;
  flex: 1;
  max-width: 300px;
}

/* 文本搜索部分 */
.text-search-section {
  margin-bottom: 24px;
}

.text-search-section .el-input {
  max-width: 600px;
}

/* 价格筛选部分 */
.price-filter-section {
  margin-bottom: 24px;
}

.price-filter-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price-filter-container .el-input-number {
  min-width: 120px;
  max-width: 200px;
  flex: 1;
}

.price-separator {
  color: #6b7280;
  font-size: 16px;
  font-weight: 600;
}

/* 房源列表部分 */
.house-list-section {
  margin-bottom: 24px;
}

.house-list-container { 
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 房源项 */
.house-item {
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.house-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 房源图片容器 */
.house-image-container {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 6px;
}

.house-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-house-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 14px;
}

.house-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.house-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.house-address {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.house-price {
  font-size: 20px;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}

.house-details {
  font-size: 14px;
  color: #4b5563;
  margin: 0;
}

/* 分页部分 */
.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .house-list {
    padding: 10px;
  }
  
  .search-container {
    padding: 16px;
  }
  
  .location-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .location-selector .el-select {
    max-width: none;
  }
  
  .text-search-section .el-input {
    max-width: none;
  }
  
  /* 房源项响应式 */
  .house-item {
    flex-direction: column;
  }
  
  .house-image-container {
    width: 100%;
    height: 200px;
  }
}
</style>

