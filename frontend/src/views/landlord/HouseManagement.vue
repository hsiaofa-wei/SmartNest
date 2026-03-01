<template>
  <div class="house-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>房源管理</span>
          <el-button type="primary" @click="handleAdd">发布房源</el-button>
        </div>
      </template>

      <el-table :data="houseList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="price" label="价格" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="adminAuditStatus" label="审核状态">
          <template #default="scope">
            <el-tag v-if="scope.row.adminAuditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.adminAuditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'info'">
              {{ scope.row.status === 'AVAILABLE' ? '可租' : '已租' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'HouseManagement',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const houseList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const userId = computed(() => store.state.auth.user?.userId)

    const loadHouses = async () => {
      if (!userId.value) return
      loading.value = true
      try {
        const res = await request({
          url: '/landlord/houses',
          method: 'get',
          params: {
            landlordId: userId.value,
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        houseList.value = res.content || []
        total.value = res.totalElements || 0
      } catch (error) {
        ElMessage.error('加载房源列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleAdd = () => {
      router.push('/landlord/houses/publish')
    }

    const handleEdit = (house) => {
      router.push(`/landlord/houses/edit/${house.id}`)
    }

    const handleDelete = async (house) => {
      try {
        await ElMessageBox.confirm('确定要删除该房源吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        // 调用后端API删除房源
        await request({
          url: `/houses/${house.id}`,
          method: 'delete'
        })
        
        ElMessage.success('删除成功')
        loadHouses()
      } catch (error) {
        if (error.name !== 'Cancel') {
          // 如果不是用户取消操作，显示错误信息
          console.error('删除房源失败:', error)
          ElMessage.error('删除失败: ' + (error.response?.data?.message || '未知错误'))
        }
      }
    }

    const handlePageChange = () => {
      loadHouses()
    }

    const handleSizeChange = () => {
      currentPage.value = 1
      loadHouses()
    }

    onMounted(() => {
      loadHouses()
    })

    return {
      loading,
      houseList,
      currentPage,
      pageSize,
      total,
      handleAdd,
      handleEdit,
      handleDelete,
      handlePageChange,
      handleSizeChange
    }
  }
}
</script>

<style scoped>
.house-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

