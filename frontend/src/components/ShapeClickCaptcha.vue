<template>
  <div class="shape-captcha-container">
    <div class="captcha-header">
      <h3>请点击{{ targetShape }}验证</h3>
      <el-button type="text" @click="refreshCaptcha" size="small">
        换一组
      </el-button>
    </div>
    <canvas 
      ref="canvas" 
      class="captcha-canvas"
      @click="handleCanvasClick"
      width="300" 
      height="200"
    ></canvas>
    <div v-if="error" class="captcha-error">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['validate', 'reset'])

// 验证码数据
const captchaData = ref(null)
const canvas = ref(null)
const targetShape = ref('')
const error = ref('')
const loading = ref(false)

// 获取验证码数据
const fetchCaptcha = async () => {
  error.value = ''
  loading.value = true
  try {
    const data = await request.get('/auth/shape-captcha')
    captchaData.value = data
    targetShape.value = data.targetShape
    drawShapes(data.shapes)
  } catch (err) {
    error.value = '获取验证码失败，请重试'
    ElMessage.error('获取验证码失败')
    console.error('Failed to fetch captcha:', err)
  } finally {
    loading.value = false
  }
}

// 绘制形状
const drawShapes = (shapes) => {
  if (!canvas.value) return
  
  const ctx = canvas.value.getContext('2d')
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 绘制背景
  ctx.fillStyle = '#f5f5f5'
  ctx.fillRect(0, 0, canvas.value.width, canvas.value.height)
  
  shapes.forEach(shape => {
    ctx.save()
    ctx.fillStyle = shape.color
    
    switch (shape.type) {
      case 'circle':
        drawCircle(ctx, shape)
        break
      case 'square':
        drawSquare(ctx, shape)
        break
      case 'triangle':
        drawTriangle(ctx, shape)
        break
      case 'star':
        drawStar(ctx, shape)
        break
    }
    
    ctx.restore()
  })
}

// 绘制圆形
const drawCircle = (ctx, shape) => {
  ctx.beginPath()
  ctx.arc(shape.x, shape.y, shape.size / 2, 0, Math.PI * 2)
  ctx.fill()
}

// 绘制正方形
const drawSquare = (ctx, shape) => {
  ctx.fillRect(shape.x - shape.size / 2, shape.y - shape.size / 2, shape.size, shape.size)
}

// 绘制三角形
const drawTriangle = (ctx, shape) => {
  const halfSize = shape.size / 2
  ctx.beginPath()
  ctx.moveTo(shape.x, shape.y - halfSize)
  ctx.lineTo(shape.x - halfSize, shape.y + halfSize)
  ctx.lineTo(shape.x + halfSize, shape.y + halfSize)
  ctx.closePath()
  ctx.fill()
}

// 绘制星形
const drawStar = (ctx, shape) => {
  const points = 5
  const outerRadius = shape.size / 2
  const innerRadius = outerRadius / 2
  
  ctx.beginPath()
  
  for (let i = 0; i < points * 2; i++) {
    const angle = (i * Math.PI) / points
    const radius = i % 2 === 0 ? outerRadius : innerRadius
    const x = shape.x + Math.cos(angle) * radius
    const y = shape.y + Math.sin(angle) * radius
    
    if (i === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  }
  
  ctx.closePath()
  ctx.fill()
}

// 处理点击事件
const handleCanvasClick = (event) => {
  if (!captchaData.value || loading.value) return
  
  const rect = canvas.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top
  
  // 检查点击的形状
  for (const shape of captchaData.value.shapes) {
    if (isPointInShape(x, y, shape)) {
      // 验证是否为目标形状
      const isCorrect = shape.type === captchaData.value.targetShape
      emit('validate', {
        key: captchaData.value.key,
        isCorrect,
        clickedShape: shape
      })
      break
    }
  }
}

// 检查点是否在形状内
const isPointInShape = (x, y, shape) => {
  switch (shape.type) {
    case 'circle':
      return isPointInCircle(x, y, shape)
    case 'square':
      return isPointInSquare(x, y, shape)
    case 'triangle':
      return isPointInTriangle(x, y, shape)
    case 'star':
      return isPointInStar(x, y, shape)
    default:
      return false
  }
}

// 检查点是否在圆形内
const isPointInCircle = (x, y, shape) => {
  const dx = x - shape.x
  const dy = y - shape.y
  return Math.sqrt(dx * dx + dy * dy) <= shape.size / 2
}

// 检查点是否在正方形内
const isPointInSquare = (x, y, shape) => {
  const halfSize = shape.size / 2
  return x >= shape.x - halfSize && x <= shape.x + halfSize &&
         y >= shape.y - halfSize && y <= shape.y + halfSize
}

// 检查点是否在三角形内
const isPointInTriangle = (x, y, shape) => {
  const halfSize = shape.size / 2
  const vertices = [
    { x: shape.x, y: shape.y - halfSize },
    { x: shape.x - halfSize, y: shape.y + halfSize },
    { x: shape.x + halfSize, y: shape.y + halfSize }
  ]
  
  // 使用面积法判断
  const area = 0.5 * (-vertices[1].y * vertices[2].x + vertices[0].y * (-vertices[1].x + vertices[2].x) + 
                     vertices[0].x * (vertices[1].y - vertices[2].y) + vertices[1].x * vertices[2].y)
  const sign = area < 0 ? -1 : 1
  
  const d1 = sign * (vertices[0].x * vertices[1].y - vertices[1].x * vertices[0].y + 
                     (vertices[1].y - vertices[0].y) * x - (vertices[1].x - vertices[0].x) * y)
  const d2 = sign * (vertices[1].x * vertices[2].y - vertices[2].x * vertices[1].y + 
                     (vertices[2].y - vertices[1].y) * x - (vertices[2].x - vertices[1].x) * y)
  const d3 = sign * (vertices[2].x * vertices[0].y - vertices[0].x * vertices[2].y + 
                     (vertices[0].y - vertices[2].y) * x - (vertices[0].x - vertices[2].x) * y)
  
  return d1 >= 0 && d2 >= 0 && d3 >= 0
}

// 检查点是否在星形内
const isPointInStar = (x, y, shape) => {
  // 简化的星形检测，使用边界框近似
  const halfSize = shape.size / 2
  return Math.abs(x - shape.x) <= halfSize && Math.abs(y - shape.y) <= halfSize
}

// 刷新验证码
const refreshCaptcha = async () => {
  await fetchCaptcha()
  emit('reset')
}

onMounted(() => {
  if (props.autoRefresh) {
    fetchCaptcha()
  }
})

// 暴露方法
const validate = async () => {
  if (!captchaData.value) {
    await fetchCaptcha()
  }
}

const reset = () => {
  refreshCaptcha()
}

defineExpose({
  refresh: refreshCaptcha,
  validate,
  reset
})
</script>

<style scoped>
.shape-captcha-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.captcha-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.captcha-canvas {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  background-color: #f5f5f5;
}

.captcha-canvas:hover {
  border-color: #409eff;
  box-shadow: 0 0 4px rgba(64, 158, 255, 0.3);
}

.captcha-error {
  font-size: 12px;
  color: #f56c6c;
  text-align: center;
}
</style>