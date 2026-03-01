<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon size="30"><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalHouses }}</div>
              <div class="stat-label">房源总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon size="30"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalOrders }}</div>
              <div class="stat-label">订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon size="30"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingAudits }}</div>
              <div class="stat-label">待审核房源</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div id="userChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span>房源分布</span>
          </template>
          <div id="houseChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

export default {
  name: 'Dashboard',
  setup() {
    const statistics = ref({
      totalUsers: 0,
      totalHouses: 0,
      totalOrders: 0,
      pendingAudits: 0
    })

    const loadStatistics = async () => {
      try {
        const res = await request({
          url: '/admin/statistics',
          method: 'get'
        })
        statistics.value = res
      } catch (error) {
        console.error('加载统计信息失败', error)
      }
    }

    const initCharts = async () => {
      try {
        // 获取真实统计数据
        const statsRes = await request({
          url: '/admin/statistics',
          method: 'get'
        })
        
        // 用户增长趋势图 - 使用真实数据或显示暂无数据
        const userChart = echarts.init(document.getElementById('userChart'))
        userChart.setOption({
          title: { text: '系统概览', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: ['用户数', '房源数', '订单数'] },
          yAxis: { type: 'value' },
          series: [{
            data: [
              statsRes.totalUsers || 0,
              statsRes.totalHouses || 0,
              statsRes.totalOrders || 0
            ],
            type: 'bar',
            itemStyle: {
              color: '#409eff'
            }
          }]
        })

        // 用户角色分布图 - 使用真实数据
        const houseChart = echarts.init(document.getElementById('houseChart'))
        const usersByRole = statsRes.usersByRole || {}
        houseChart.setOption({
          title: { text: '用户角色分布', left: 'center' },
          tooltip: { trigger: 'item' },
          series: [{
            type: 'pie',
            radius: '50%',
            data: [
              { value: usersByRole.ADMIN || 0, name: '管理员' },
              { value: usersByRole.LANDLORD || 0, name: '房东' },
              { value: usersByRole.TENANT || 0, name: '租客' }
            ]
          }]
        })
      } catch (error) {
        console.error('加载图表数据失败', error)
        // 显示空图表
        const userChart = echarts.init(document.getElementById('userChart'))
        userChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle' }
        })
        
        const houseChart = echarts.init(document.getElementById('houseChart'))
        houseChart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle' }
        })
      }
    }

    onMounted(() => {
      loadStatistics()
      setTimeout(() => {
        initCharts()
      }, 100)
    })

    return {
      statistics
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

@media (max-width: 768px) {
  .stat-content {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>

