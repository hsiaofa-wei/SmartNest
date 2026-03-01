<template>
  <div class="house-audit">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span>房源审核</span>
          <div class="actions">
            <el-select v-model="auditFilter" placeholder="筛选状态" style="width: 160px" @change="handleFilter">
              <el-option label="全部" value="ALL" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="houseList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="price" label="价格" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="landlordId" label="房东ID" />
        <el-table-column prop="adminAuditStatus" label="审核状态">
          <template #default="scope">
            <el-tag v-if="scope.row.adminAuditStatus === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.adminAuditStatus === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button
              size="small"
              type="success"
              @click="handleAudit(scope.row, 'APPROVED')"
              v-if="scope.row.adminAuditStatus === 'PENDING'"
            >
              通过
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleAudit(scope.row, 'REJECTED')"
              v-if="scope.row.adminAuditStatus === 'PENDING'"
            >
              拒绝
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

    <el-dialog v-model="auditDialogVisible" title="审核房源" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 房源详情 -->
    <el-dialog v-model="detailDialogVisible" title="房源详情" width="800px">
      <div v-if="detailData.house && detailData.house.id">
        <el-row :gutter="20">
          <el-col :span="10">
            <el-carousel height="260px" v-if="detailData.images && detailData.images.length">
              <el-carousel-item v-for="(img, idx) in detailData.images" :key="idx">
                <img :src="img.imageUrl" style="width:100%;height:260px;object-fit:cover" />
              </el-carousel-item>
            </el-carousel>
            <div v-else style="height:260px;display:flex;align-items:center;justify-content:center;border:1px dashed #ddd;">
              暂无图片
            </div>
          </el-col>
          <el-col :span="14">
            <h3>{{ detailData.house.title }}</h3>
            <p style="margin:4px 0;color:#f56c6c;font-size:18px;">￥{{ detailData.house.price }}/月</p>
            <p style="margin:4px 0;">面积：{{ detailData.house.area }}㎡</p>
            <p style="margin:4px 0;">地址：{{ detailData.house.address }}</p>
            <p style="margin:4px 0;">状态：{{ detailData.house.status }}</p>
            <p style="margin:4px 0;">审核状态：{{ detailData.house.adminAuditStatus }}</p>
            <p style="margin:4px 0;">浏览：{{ detailData.house.viewCount }}，收藏：{{ detailData.house.favoriteCount }}</p>
          </el-col>
        </el-row>

        <el-divider>详细信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="楼层">{{ detailData.detail?.floor }}/{{ detailData.detail?.totalFloors }}</el-descriptions-item>
          <el-descriptions-item label="户型">{{ detailData.detail?.roomType }}</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ detailData.detail?.orientation }}</el-descriptions-item>
          <el-descriptions-item label="装修">{{ detailData.detail?.decoration }}</el-descriptions-item>
          <el-descriptions-item label="电梯">{{ detailData.detail?.hasElevator ? '有' : '无' }}</el-descriptions-item>
          <el-descriptions-item label="停车位">{{ detailData.detail?.hasParking ? '有' : '无' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>房源描述</el-divider>
        <p style="white-space:pre-wrap;">{{ detailData.house.description }}</p>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

export default {
  name: 'HouseAudit',
  setup() {
    const loading = ref(false)
    const houseList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const auditDialogVisible = ref(false)
    const currentHouse = ref(null)
    const detailDialogVisible = ref(false)
    const detailData = ref({
      house: {},
      detail: {},
      images: []
    })

    const auditForm = ref({
      status: 'APPROVED',
      remark: ''
    })

    const loadHouses = async () => {
      loading.value = true
      try {
        const res = await request({
          url: '/admin/houses/audit',
          method: 'get',
          params: {
            status: auditFilter.value,
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

    const auditFilter = ref('ALL') // ALL / PENDING / APPROVED / REJECTED

    const handleFilter = () => {
      currentPage.value = 1
      loadHouses()
    }

    const handleView = (house) => {
      loadDetail(house.id)
    }

    const handleAudit = (house, status) => {
      currentHouse.value = house
      auditForm.value.status = status
      auditDialogVisible.value = true
    }

    const loadDetail = async (houseId) => {
      try {
        const res = await request({
          url: `/houses/${houseId}`,
          method: 'get'
        })
        detailData.value = res
        detailDialogVisible.value = true
      } catch (error) {
        ElMessage.error('加载房源详情失败')
      }
    }

    const submitAudit = async () => {
      try {
        await request({
          url: `/admin/houses/${currentHouse.value.id}/audit`,
          method: 'post',
          params: {
            status: auditForm.value.status,
            remark: auditForm.value.remark
          }
        })
        ElMessage.success('审核成功')
        auditDialogVisible.value = false
        loadHouses()
      } catch (error) {
        ElMessage.error('审核失败')
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
      auditDialogVisible,
      auditForm,
      auditFilter,
      detailDialogVisible,
      detailData,
      handleView,
      handleAudit,
      submitAudit,
      handlePageChange,
      handleSizeChange,
      handleFilter
    }
  }
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.house-audit {
  padding: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

