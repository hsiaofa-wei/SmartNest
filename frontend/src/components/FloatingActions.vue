<template>
  <div class="floating-actions">
    <el-tooltip content="回到顶部" placement="left">
      <el-button class="fab" type="info" circle @click="scrollTop">
        <el-icon><Top /></el-icon>
      </el-button>
    </el-tooltip>
  </div>
</template>

<script>
import { Top } from '@element-plus/icons-vue'

export default {
  name: 'FloatingActions',
  components: { Top },
  setup() {
    const scrollTop = () => {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    return {
      scrollTop
    }
  }
}
</script>

<style scoped>
.floating-actions {
  position: fixed;
  right: 24px;
  bottom: 120px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 2000;
}

.fab {
  width: 48px;
  height: 48px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.drawer-section {
  margin-bottom: 16px;
}

.faq {
  padding-left: 16px;
  line-height: 1.6;
}
</style>

