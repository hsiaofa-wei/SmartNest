<template>
  <div class="appointments">
    <el-card>
      <template #header>
        <span>我的预约</span>
      </template>

      <el-table :data="appointmentList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="houseId" label="房源ID" />
        <el-table-column prop="appointmentTime" label="预约时间" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 'CONFIRMED'" type="success">已确认</el-tag>
            <el-tag v-else-if="scope.row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="landlordReply" label="房东回复" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="goToDetail(scope.row.houseId)">查看房源</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'Appointments',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const appointmentList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const userId = computed(() => store.state.auth.user?.userId)

    const loadAppointments = async () => {
      if (!userId.value) return
      loading.value = true
      try {
        const res = await request({
          url: '/tenant/appointments',
          method: 'get',
          params: {
            tenantId: userId.value,
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        // 显示所有预约，包括已完成的
        appointmentList.value = res.content || []
        total.value = res.totalElements || 0
      } catch (error) {
        ElMessage.error('加载预约列表失败')
      } finally {
        loading.value = false
      }
    }

    const goToDetail = (houseId) => {
      router.push(`/tenant/houses/${houseId}`)
    }

    const handlePageChange = () => {
      loadAppointments()
    }

    const handleSizeChange = () => {
      currentPage.value = 1
      loadAppointments()
    }

    onMounted(() => {
      loadAppointments()
    })

    return {
      loading,
      appointmentList,
      currentPage,
      pageSize,
      total,
      goToDetail,
      handlePageChange,
      handleSizeChange
    }
  }
}
</script>

<style scoped>
.appointments {
  margin-top: 80px;
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

