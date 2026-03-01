<template>
  <div class="ai-chat-container">
    <!-- 悬浮聊天图标 -->
    <div 
      class="chat-icon" 
      @click="toggleChatWindow"
      :class="{ active: isChatOpen }"
    >
      <el-icon class="icon-chat"><ChatDotRound /></el-icon>
    </div>

    <!-- 聊天窗口 -->
    <div 
      class="chat-window" 
      v-if="isChatOpen"
      :class="{ open: isChatOpen }"
    >
      <!-- 窗口头部 -->
      <div class="chat-header">
        <h3 class="header-title">AI 客服助手</h3>
        <el-icon 
          class="icon-close" 
          @click="toggleChatWindow"
        ><Close /></el-icon>
      </div>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div class="message ai-message" v-if="messages.length === 0">
          <div class="message-content">
            <p class="welcome-text">您好！我是您的AI客服助手，有什么可以帮助您的吗？</p>
            <div class="quick-questions">
              <div class="quick-question-item" @click="quickQuestion('如何搜索和查看房源？')">如何搜索和查看房源？</div>
              <div class="quick-question-item" @click="quickQuestion('如何预约看房？')">如何预约看房？</div>
              <div class="quick-question-item" @click="quickQuestion('如何创建租赁订单？')">如何创建租赁订单？</div>
              <div class="quick-question-item" @click="quickQuestion('如何支付租金和押金？')">如何支付租金和押金？</div>
              <div class="quick-question-item" @click="quickQuestion('房东如何发布房屋？')">房东如何发布房屋？</div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="message"
          :class="message.sender === 'user' ? 'user-message' : 'ai-message'"
        >
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div class="message ai-message" v-if="isLoading">
          <div class="message-content">
            <div class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          placeholder="输入您的问题..."
          :disabled="isLoading"
          @keyup.enter="sendMessage"
        >
          <template #suffix>
            <el-button 
              type="primary" 
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isLoading"
            >
              <el-icon class="icon-send"><ArrowRightBold /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { ChatDotRound, Close, ArrowRightBold } from '@element-plus/icons-vue'
import axios from 'axios'

export default {
  name: 'AIChat',
  components: { ChatDotRound, Close, ArrowRightBold },
  setup() {
    const isChatOpen = ref(false)
    const messages = ref([])
    const inputMessage = ref('')
    const isLoading = ref(false)
    const messagesContainer = ref(null)
    
    // DeepSeek API 配置
    const API_KEY = ''
    const API_URL = '/deepseek/v1/chat/completions'
    
    // 切换聊天窗口
    const toggleChatWindow = () => {
      isChatOpen.value = !isChatOpen.value
    }
    
    // 快速提问
    const quickQuestion = (question) => {
      inputMessage.value = question
      sendMessage()
    }
    
    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return
      
      // 添加用户消息
      const userMessage = {
        sender: 'user',
        content: inputMessage.value.trim()
      }
      messages.value.push(userMessage)
      
      // 清空输入框
      inputMessage.value = ''
      
      // 滚动到底部
      scrollToBottom()
      
      // 调用DeepSeek API
      try {
        isLoading.value = true
        
        const headers = {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${API_KEY}`
        }
        
        const data = {
          model: 'deepseek-chat',
          messages: [
            {
              role: 'system',
              content: '你是一个专业的租房平台AI客服助手，需要用友好、专业的语言回答用户关于租房的问题。\n\n这个租房平台主要功能包括：\n1. 房屋管理：租户可以浏览房屋列表、查看房屋详情、收藏喜欢的房屋；房东可以发布、编辑、管理自己的房屋\n2. 预约管理：租户可以预约看房，房东可以管理看房预约\n3. 订单管理：租户可以创建租赁订单，房东可以管理订单\n4. 支付功能：支持在线支付租金和押金\n5. 用户角色：平台包含管理员、房东和租户三种角色，各自有不同的权限和功能\n\n请根据用户的角色和具体问题，提供准确、有用的回答。'
            },
            ...messages.value.map(msg => ({
              role: msg.sender === 'user' ? 'user' : 'assistant',
              content: msg.content
            }))
          ],
          stream: false
        }
        
        console.log('发送请求到API:', API_URL)
        console.log('请求头:', headers)
        console.log('请求数据:', data)
        
        const response = await axios.post(API_URL, data, { headers })
        
        console.log('API响应:', response)
        
        // 添加AI回复
        const aiMessage = {
          sender: 'ai',
          content: response.data.choices[0].message.content
        }
        messages.value.push(aiMessage)
        
      } catch (error) {
        console.error('AI客服错误:', error)
        console.error('错误详情:', error.message)
        console.error('响应状态:', error.response?.status)
        console.error('响应数据:', error.response?.data)
        
        // 添加错误提示
        const errorMessage = {
          sender: 'ai',
          content: `抱歉，AI客服暂时无法响应。错误原因: ${error.message}`
        }
        messages.value.push(errorMessage)
        
      } finally {
        isLoading.value = false
        scrollToBottom()
      }
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      setTimeout(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      }, 100)
    }
    
    // 监听消息变化，自动滚动到底部
    watch(messages, () => {
      scrollToBottom()
    }, { deep: true })
    
    onMounted(() => {
      // 初始化
    })
    
    return {
      isChatOpen,
      messages,
      inputMessage,
      isLoading,
      messagesContainer,
      toggleChatWindow,
      sendMessage,
      quickQuestion
    }
  }
}
</script>

<style scoped>
.ai-chat-container {
  position: fixed;
  bottom: 60px;
  right: 24px;
  z-index: 1000;
}

/* 聊天图标样式 */
.chat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1000;
}

.chat-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 36px rgba(24, 144, 255, 0.6);
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
}

.chat-icon.active {
  background: linear-gradient(135deg, #096dd9 0%, #1890ff 100%);
  transform: scale(0.95);
}

.chat-icon::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-radius: 50%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.chat-icon:hover::after {
  opacity: 1;
}

/* 消息加载动画 */
.loading-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background-color: #1890ff;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 聊天窗口样式 */
.chat-window {
  position: fixed;
  bottom: 130px;
  right: 24px;
  width: 380px;
  max-width: 90vw;
  height: 520px;
  background-color: #ffffff;
  border: 1px solid #e8f0fe;
  border-radius: 20px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  transform: translateY(20px);
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chat-window.open {
  transform: translateY(0);
  opacity: 1;
  visibility: visible;
}

/* 窗口头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  border-bottom: none;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.chat-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-header h3::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #ffffff;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
}

.close-icon {
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.3s ease;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.15);
  transform: rotate(90deg);
}

/* 聊天消息区域 */
.chat-messages {
  height: calc(100% - 130px);
  overflow-y: auto;
  padding: 24px;
  background-color: #fafafa;
  background-image: 
    radial-gradient(circle at 1px 1px, #e8f0fe 1px, transparent 0);
  background-size: 24px 24px;
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #1890ff;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #40a9ff;
}

/* 消息样式 */
.message {
  margin-bottom: 16px;
  display: flex;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-content {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  word-wrap: break-word;
}

.user-message .message-content {
  background-color: #1890ff;
  color: #ffffff;
  border-bottom-right-radius: 6px;
}

.ai-message .message-content {
  background-color: #ffffff;
  color: #333333;
  border-bottom-left-radius: 6px;
  border: 1px solid #e8f0fe;
}

/* 输入区域 */
.chat-input-area {
  padding: 15px 20px;
  background-color: #ffffff;
  border-top: 1px solid #e8f0fe;
}

.chat-input-area .el-input {
  width: 100%;
}

.chat-input-area .el-input__inner {
  background-color: #fafafa;
  border-color: #e8f0fe;
  color: #333333;
  border-radius: 20px;
  padding: 12px 18px;
  font-size: 14px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.chat-input-area .el-input__inner:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
  background-color: #ffffff;
}

.chat-input-area .el-button {
  background-color: #1890ff;
  border-color: #1890ff;
  color: #ffffff;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-input-area .el-button:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.chat-input-area .el-button:disabled {
  background-color: #f0f0f0;
  border-color: #f0f0f0;
  color: #cccccc;
}

.chat-input-area .el-button:disabled:hover {
  transform: none;
  box-shadow: none;
}

/* 图标样式 */
.icon-chat {
  font-size: 24px;
  font-weight: 500;
}

.icon-close {
  font-size: 20px;
  font-weight: 600;
}

.icon-send {
  font-size: 18px;
  font-weight: 600;
}

/* 字体样式 */
.header-title {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-weight: 600;
  font-size: 20px;
  letter-spacing: 0.5px;
}

.welcome-text {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* 快速提问样式 */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.quick-question-item {
  padding: 8px 12px;
  background-color: #f0f9ff;
  color: #1890ff;
  border: 1px solid #e0f2fe;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.quick-question-item:hover {
  background-color: #e0f2fe;
  border-color: #1890ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
}

.message-text {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
  word-break: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
  /* 聊天窗口 */
  .chat-window {
    width: 92vw;
    max-width: 400px;
    height: 70vh;
    max-height: 500px;
    right: 16px;
    bottom: 80px;
  }

  /* 聊天图标 */
  .chat-icon {
    right: 16px;
    bottom: 16px;
  }

  /* 窗口头部 */
  .chat-header {
    padding: 16px 20px;
  }

  .header-title {
    font-size: 18px;
  }

  /* 消息区域 */
  .chat-messages {
    padding: 16px;
  }

  .message-content {
    max-width: 80%;
    padding: 10px 14px;
    font-size: 13px;
  }

  /* 输入区域 */
  .chat-input-area {
    padding: 12px 16px;
  }

  .chat-input-area .el-input__inner {
    padding: 10px 14px;
    font-size: 13px;
  }

  .chat-input-area .el-button {
    width: 34px;
    height: 34px;
  }
}

@media (max-width: 480px) {
  /* 聊天窗口 */
  .chat-window {
    width: 95vw;
    height: 75vh;
  }

  /* 消息区域 */
  .message-content {
    max-width: 85%;
  }
}
</style>