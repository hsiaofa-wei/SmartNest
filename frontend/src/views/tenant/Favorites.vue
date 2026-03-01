<template>
  <div class="favorites">
    <el-card>
      <template #header>
        <span>我的收藏</span>
      </template>

      <el-table :data="favoriteList" v-loading="loading" border>
        <el-table-column prop="houseId" label="房源ID" />
        <el-table-column prop="createTime" label="收藏时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="goToDetail(scope.row.houseId)">查看</el-button>
              <el-button size="small" type="danger" @click="handleRemove(scope.row)">取消收藏</el-button>
            </div>
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
  name: 'Favorites',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const favoriteList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const userId = computed(() => store.state.auth.user?.userId)

    const loadFavorites = async () => {
      if (!userId.value) return
      loading.value = true
      try {
        const res = await request({
          url: '/tenant/favorites',
          method: 'get',
          params: {
            tenantId: userId.value,
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        favoriteList.value = res.content || []
        total.value = res.totalElements || 0
      } catch (error) {
        ElMessage.error('加载收藏列表失败')
      } finally {
        loading.value = false
      }
    }

    const goToDetail = (houseId) => {
      router.push(`/tenant/houses/${houseId}`)
    }

    const handleRemove = async (favorite) => {
      try {
        await request({
          url: `/tenant/favorites/${favorite.houseId}`,
          method: 'post',
          params: { tenantId: userId.value }
        })
        ElMessage.success('取消收藏成功')
        loadFavorites()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }

    const handlePageChange = () => {
      loadFavorites()
    }

    const handleSizeChange = () => {
      currentPage.value = 1
      loadFavorites()
    }

    onMounted(() => {
      loadFavorites()
    })

    return {
      loading,
      favoriteList,
      currentPage,
      pageSize,
      total,
      goToDetail,
      handleRemove,
      handlePageChange,
      handleSizeChange
    }
  }
}
</script>

<style scoped>
.favorites {
  margin-top: 80px;
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 统一操作列按钮布局 */
.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  margin: 0 !important;
  padding: 8px 10px !important;
}
</style>

