<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="scope.row.role === 'LANDLORD'" type="warning">房东</el-tag>
            <el-tag v-else type="success">租客</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'UserManagement',
  setup() {
    const loading = ref(false)
    const userList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)

    const loadUsers = async () => {
      loading.value = true
      try {
        const res = await request({
          url: '/admin/users',
          method: 'get',
          params: {
            page: currentPage.value - 1,
            size: pageSize.value
          }
        })
        userList.value = res.content || []
        total.value = res.totalElements || 0
      } catch (error) {
        ElMessage.error('加载用户列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleToggleStatus = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确定要${user.status === 'ACTIVE' ? '禁用' : '启用'}该用户吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await request({
          url: `/admin/users/${user.id}/status`,
          method: 'put',
          params: {
            status: user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
          }
        })
        ElMessage.success('操作成功')
        loadUsers()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    }

    const handleAdd = () => {
      ElMessage.info('新增用户功能待实现')
    }

    const handleEdit = (user) => {
      ElMessage.info('编辑用户功能待实现')
    }

    const handlePageChange = () => {
      loadUsers()
    }

    const handleSizeChange = () => {
      currentPage.value = 1
      loadUsers()
    }

    onMounted(() => {
      loadUsers()
    })

    return {
      loading,
      userList,
      currentPage,
      pageSize,
      total,
      handleToggleStatus,
      handleAdd,
      handleEdit,
      handlePageChange,
      handleSizeChange
    }
  }
}
</script>

<style scoped>
.user-management {
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

