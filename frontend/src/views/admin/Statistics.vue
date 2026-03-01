<template>
  <div class="statistics">
    <el-card>
      <template #header>
        <span>数据统计（真实数据）</span>
      </template>
      <div id="statisticsChart" style="height: 500px;"></div>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

export default {
  name: 'Statistics',
  setup() {
    const chartRef = ref(null)

    const loadData = async () => {
      try {
        const res = await request({
          url: '/admin/statistics/monthly',
          method: 'get'
        })

        const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        const users = res?.monthlyUsers || Array(12).fill(0)
        const houses = res?.monthlyHouses || Array(12).fill(0)
        const orders = res?.monthlyOrders || Array(12).fill(0)

        const chart = echarts.init(document.getElementById('statisticsChart'))
        chartRef.value = chart
        chart.setOption({
          title: {
            text: '系统数据统计（最近12个月）',
            left: 'center'
          },
          tooltip: {
            trigger: 'axis'
          },
          legend: {
            data: ['用户数', '房源数', '订单数'],
            top: 30
          },
          xAxis: {
            type: 'category',
            data: months
          },
          yAxis: {
            type: 'value'
          },
          series: [
            {
              name: '用户数',
              type: 'line',
              data: users
            },
            {
              name: '房源数',
              type: 'line',
              data: houses
            },
            {
              name: '订单数',
              type: 'line',
              data: orders
            }
          ]
        })
      } catch (error) {
        console.error('加载统计数据失败', error)
        const chart = echarts.init(document.getElementById('statisticsChart'))
        chartRef.value = chart
        chart.setOption({
          title: { text: '暂无数据', left: 'center', top: 'middle' }
        })
      }
    }

    onMounted(() => {
      setTimeout(() => {
        loadData()
      }, 100)
    })

    return {
      chartRef
    }
  }
}
</script>

<style scoped>
.statistics {
  padding: 20px;
}
</style>

