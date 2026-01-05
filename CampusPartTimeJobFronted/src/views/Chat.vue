<template>
  <div class="chat-page">
    <van-nav-bar
      :title="chatTitle"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />
    
    <div class="chat-container" ref="chatContainer">
      <div class="message-list">
        <van-loading v-if="loadingHistory" type="spinner" vertical>加载中...</van-loading>
        <div
          v-for="msg in messageList"
          :key="msg.id"
          :class="['message-item', msg.isSelf ? 'message-self' : 'message-other']"
        >
          <div class="message-content">
            <div class="message-header" v-if="!msg.isSelf">
              <span class="message-sender">{{ msg.senderName }}</span>
            </div>
            <div class="message-bubble">
              {{ msg.content }}
              <van-loading v-if="msg.status === 'sending'" size="12px" color="#888" />
              <van-icon v-else-if="msg.status === 'failed'" name="close" class="send-failed-icon" color="#f53f3f" />
            </div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input-container">
      <van-field
        v-model="inputMessage"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
      >
        <template #button>
          <van-button
            type="primary"
            size="small"
            :disabled="!inputMessage.trim() || !wsConnected"
            :loading="!wsConnected"
            @click="sendMessage"
          >
            {{ wsConnected ? '发送' : '连接中...' }}
          </van-button>
        </template>
      </van-field>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import wsManager from '@/utils/websocket'
import { getChatHistory } from '@/api/chat'
import { showFailToast, showSuccessToast } from 'vant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const chatTitle = ref(route.query.name || '聊天')
const targetUserId = ref(Number(route.params.userId))
const targetRole = ref(Number(route.params.role))

console.log('Chat页面初始化:', {
  targetUserId: targetUserId.value,
  targetRole: targetRole.value,
  routeParams: route.params
})
const inputMessage = ref('')
const messageList = ref([])
const wsConnected = ref(false)
const chatContainer = ref(null)
const loadingHistory = ref(false)
const messageMap = new Map() // 用于去重，key为消息ID

// 不再使用计算属性，直接使用 wsConnected 响应式变量
// 定期检查函数会确保 wsConnected 与实际连接状态同步

// 解析消息格式：【role-userId】：message
const parseMessage = (message) => {
  const match = message.match(/^【(\d+)-(\d+)】：(.+)$/)
  if (match) {
    return {
      role: Number(match[1]),
      userId: Number(match[2]),
      content: match[3]
    }
  }
  return null
}

// 格式化时间
const formatTime = (date) => {
  if (!date) {
    date = new Date()
  } else if (typeof date === 'string') {
    date = new Date(date)
  }
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) {
    date = new Date()
  } else if (typeof date === 'string') {
    date = new Date(date)
  }
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  if (msgDate.getTime() === today.getTime()) {
    // 今天，只显示时间
    return formatTime(date)
  } else {
    // 其他日期，显示日期和时间
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 将ChatMessage转换为消息对象
const convertChatMessage = (msg) => {
  const userInfo = userStore.userInfo || userStore.userDetail
  const currentUserId = Number(userInfo?.id)
  
  // 判断消息是否是自己发送的（使用sendUserId字段，这是数据库中的实际发送者）
  const isSelf = Number(msg.sendUserId) === currentUserId
  
  // 解析消息内容
  const parsed = parseMessage(msg.msgContent)
  
  if (parsed) {
    // 如果解析成功，使用解析后的内容
    return {
      id: msg.id,
      content: parsed.content,
      senderName: isSelf ? '我' : (chatTitle.value || `用户${parsed.userId}`),
      senderId: Number(parsed.userId), // 确保是数字类型
      senderRole: Number(parsed.role),
      isSelf,
      time: formatDateTime(msg.createTime),
      createTime: msg.createTime
    }
  } else {
    // 如果解析失败，使用原始字段
    return {
      id: msg.id,
      content: msg.msgContent,
      senderName: isSelf ? '我' : (chatTitle.value || `用户${msg.sendUserId}`),
      senderId: Number(msg.sendUserId),
      senderRole: Number(msg.sendRole),
      isSelf,
      time: formatDateTime(msg.createTime),
      createTime: msg.createTime
    }
  }
}

// 加载聊天历史记录
const loadChatHistory = async () => {
  loadingHistory.value = true
  try {
    const res = await getChatHistory(targetUserId.value)
    if (res && res.code === 200 && res.data) {
      // 清空现有消息
      messageMap.clear()
      
      // 转换并添加历史消息
      res.data.forEach(msg => {
        const messageObj = convertChatMessage(msg)
        messageMap.set(messageObj.id, messageObj)
      })
      
      // 按时间排序
      messageList.value = Array.from(messageMap.values()).sort((a, b) => {
        const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
        const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
        return timeA - timeB
      })
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
  } catch (error) {
    console.error('加载聊天历史失败', error)
    const errorMsg = error.response?.data?.msg || error.message || '加载聊天历史失败'
    console.error('错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      fullUrl: error.config?.baseURL + error.config?.url,
      targetUserId: targetUserId.value
    })
    showFailToast(errorMsg)
  } finally {
    loadingHistory.value = false
  }
}

// 处理接收到的实时消息
const handleMessage = (message) => {
  console.log('Chat页面收到WebSocket消息:', message, {
    targetUserId: targetUserId.value,
    targetUserIdType: typeof targetUserId.value
  })
  
  const parsed = parseMessage(message)
  if (parsed) {
    const userInfo = userStore.userInfo || userStore.userDetail
    const currentUserId = Number(userInfo?.id)
    const parsedUserId = Number(parsed.userId)
    const targetId = Number(targetUserId.value)
    const isSelf = parsedUserId === currentUserId
    
    console.log('消息解析结果:', {
      parsed,
      currentUserId,
      parsedUserId,
      targetId,
      isSelf,
      userRole: userInfo?.role
    })
    
    // 检查是否是当前聊天对象的消息
    // WebSocket接收到的消息格式：【role-userId】：message
    // 其中userId是发送者的ID
    // 1. 如果发送者是当前用户（回显），应该显示
    // 2. 如果发送者是目标用户（目标用户发给当前用户），应该显示
    const isCurrentChatMessage = 
      parsedUserId === currentUserId ||  // 当前用户发送的消息（回显）
      parsedUserId === targetId  // 目标用户发送的消息
    
    console.log('消息匹配结果:', {
      isCurrentChatMessage,
      isSelf,
      parsedUserId,
      currentUserId,
      targetId,
      matchSelf: parsedUserId === currentUserId,
      matchTarget: parsedUserId === targetId
    })
    
    if (isCurrentChatMessage) {
      // 如果是自己发送的消息（回显），检查是否有对应的"发送中"状态的临时消息
      if (isSelf) {
        // 查找是否有相同内容的"发送中"状态消息
        const sendingMessage = Array.from(messageMap.values()).find(msg => 
          msg.status === 'sending' && 
          msg.content === parsed.content && 
          msg.isSelf === true &&
          msg.senderId === currentUserId
        )
        
        if (sendingMessage) {
          // 找到对应的发送中消息，更新状态为成功（移除status）
          console.log('找到对应的发送中消息，更新状态', { 
            tempId: sendingMessage.id, 
            content: parsed.content 
          })
          delete sendingMessage.status
          // 更新消息时间
          sendingMessage.time = formatTime()
          sendingMessage.createTime = new Date()
          // 触发响应式更新
          messageList.value = Array.from(messageMap.values()).sort((a, b) => {
            const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
            const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
            return timeA - timeB
          })
          nextTick(() => {
            scrollToBottom()
          })
          return // 不需要创建新消息
        }
      }
      
      // 创建临时消息对象（没有ID，使用时间戳作为临时ID）
      const tempId = `temp_${Date.now()}_${Math.random()}`
      const messageObj = {
        id: tempId,
        content: parsed.content,
        senderName: isSelf ? '我' : (chatTitle.value || `用户${parsed.userId}`),
        senderId: parsed.userId,
        senderRole: parsed.role,
        isSelf,
        time: formatTime(),
        createTime: new Date()
      }
      
      // 检查是否已存在（避免重复）
      const exists = Array.from(messageMap.values()).some(msg => 
        msg.content === messageObj.content && 
        msg.senderId === messageObj.senderId &&
        msg.isSelf === messageObj.isSelf &&
        !msg.status && // 排除发送中状态的消息
        Math.abs(new Date(msg.createTime).getTime() - messageObj.createTime.getTime()) < 2000
      )
      
      if (!exists) {
        messageMap.set(tempId, messageObj)
        messageList.value = Array.from(messageMap.values()).sort((a, b) => {
          const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
          const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
          return timeA - timeB
        })
        
        console.log('消息已添加到列表:', {
          content: messageObj.content,
          senderId: messageObj.senderId,
          isSelf: messageObj.isSelf,
          messageListLength: messageList.value.length,
          userRole: userInfo?.role
        })
        
        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })
      } else {
        console.log('消息已存在，跳过添加')
      }
    } else {
      console.log('消息不是当前聊天对象的消息，跳过', {
        parsedUserId: parsed.userId,
        currentUserId: Number(userInfo?.id),
        targetUserId: targetUserId.value,
        userRole: userInfo?.role
      })
    }
  } else {
    // 如果不是标准格式，直接显示
    const tempId = `temp_${Date.now()}_${Math.random()}`
    const messageObj = {
      id: tempId,
      content: message,
      senderName: '系统',
      senderId: null,
      senderRole: null,
      isSelf: false,
      time: formatTime(),
      createTime: new Date()
    }
    messageMap.set(tempId, messageObj)
    messageList.value = Array.from(messageMap.values())
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    const container = chatContainer.value.querySelector('.message-list')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) {
    return
  }

  if (!wsManager.isConnected()) {
    showFailToast('连接未建立，请稍后再试')
    connectWebSocket()
    return
  }

  const messageContent = inputMessage.value.trim()
  console.log('准备发送消息:', { targetUserId: targetUserId.value, messageContent })
  
  // 添加发送中状态的临时消息
  const userInfo = userStore.userInfo || userStore.userDetail;
  const tempId = `temp_send_${Date.now()}_${Math.random()}`;
  const messageObj = {
    id: tempId,
    content: messageContent,
    senderName: '我',
    senderId: userInfo?.id,
    senderRole: userInfo?.role,
    isSelf: true,
    time: formatTime(),
    createTime: new Date(),
    status: 'sending' // 添加发送状态
  };
  
  messageMap.set(tempId, messageObj);
  messageList.value = Array.from(messageMap.values()).sort((a, b) => {
    const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
    const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
    return timeA - timeB;
  });
  
  // 发送消息
  const success = wsManager.sendMessage(targetUserId.value, messageContent);
  if (!success) {
    // 更新消息状态为发送失败
    messageObj.status = 'failed';
    messageList.value = Array.from(messageMap.values()).sort((a, b) => {
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
      return timeA - timeB;
    });
    showFailToast('发送失败，请检查网络连接');
  }
  
  inputMessage.value = '';
  nextTick(() => {
    scrollToBottom();
  });
}

// 连接WebSocket
const connectWebSocket = async () => {
  // 确保用户信息已初始化
  if (!userStore.userInfo) {
    userStore.initUserInfo()
    // 等待用户信息完全初始化
    await new Promise(resolve => setTimeout(resolve, 300))
  }
  
  // 获取用户信息，优先使用userInfo，其次使用userDetail
  let userInfo = userStore.userInfo || userStore.userDetail
  
  // 如果仍然没有用户信息，尝试从localStorage获取
  if (!userInfo) {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo = JSON.parse(storedUserInfo)
        userStore.setUserInfo(userInfo) // 更新到store
      } catch (e) {
        console.error('解析本地用户信息失败', e)
      }
    }
  }
  
  // 如果还是没有用户信息，尝试调用API获取
  if (!userInfo || !userInfo.id) {
    try {
      await userStore.refreshUserDetail()
      userInfo = userStore.userDetail || userStore.userInfo
    } catch (error) {
      console.error('刷新用户信息失败', error)
    }
  }
  
  if (!userInfo || !userInfo.id) {
    console.error('用户信息获取失败:', {
      userInfo: userStore.userInfo,
      userDetail: userStore.userDetail,
      localStorageUserInfo: localStorage.getItem('userInfo')
    })
    showFailToast('用户信息获取失败，请重新登录')
    router.push('/login')
    return
  }
  
  // 严格验证userId
  if (!userInfo.id || userInfo.id === null || userInfo.id === undefined || userInfo.id === 'null' || userInfo.id === 'undefined') {
    console.error('无效的userId:', userInfo.id, 'userInfo:', userInfo)
    showFailToast('用户ID无效，请重新登录')
    router.push('/login')
    return
  }

  const userId = Number(userInfo.id)
  if (!isFinite(userId) || userId <= 0 || isNaN(userId)) {
    console.error('无效的userId数值:', userInfo.id, '转换后:', userId)
    showFailToast('用户ID无效，请重新登录')
    router.push('/login')
    return
  }

  // 严格验证role
  let role = 0
  if (userInfo.role !== undefined && userInfo.role !== null && userInfo.role !== 'null' && userInfo.role !== 'undefined') {
    role = Number(userInfo.role)
    if (!isFinite(role) || isNaN(role) || role < 0) {
      console.warn('role无效，使用默认值0:', userInfo.role, '转换后:', role)
      role = 0
    }
  }

  console.log('准备连接WebSocket:', { 
    userId, 
    role, 
    userInfo, 
    userIdType: typeof userId, 
    roleType: typeof role,
    targetUserId: targetUserId.value,
    isConnected: wsManager.isConnected()
  })

  // 先移除之前的消息处理器（如果有）
  wsManager.removeMessageHandler(handleMessage)

  // 如果WebSocket已经连接，直接添加消息处理器
  if (wsManager.isConnected() && wsManager.userId === userId && wsManager.role === role) {
    console.log('WebSocket已连接，直接添加消息处理器', { userId, role })
    wsManager.addMessageHandler(handleMessage)
    wsConnected.value = true
    return
  }

  wsManager.connect(
    userId,
    role,
    handleMessage,
    (error) => {
      console.error('WebSocket连接错误', error)
      wsConnected.value = false
    },
    () => {
      // 连接成功回调
      console.log('WebSocket连接成功', { userId, role })
      wsConnected.value = true
    }
  )
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadChatHistory()
  connectWebSocket()
})

onUnmounted(() => {
  wsManager.removeMessageHandler(handleMessage)
})

</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f7f8fa;
}

.chat-container {
  flex: 1;
  overflow: hidden;
  padding: 8px;
}

.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 8px 0;
}

.message-item {
  display: flex;
  margin-bottom: 12px;
}

.message-self {
  justify-content: flex-end;
}

.message-other {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-self .message-content {
  align-items: flex-end;
}

.message-other .message-content {
  align-items: flex-start;
}

.message-header {
  margin-bottom: 4px;
}

.message-sender {
  font-size: 12px;
  color: #969799;
}

.message-bubble {
  padding: 8px 12px;
  border-radius: 8px;
  word-wrap: break-word;
  word-break: break-all;
}

.message-self .message-bubble {
  background-color: #1989fa;
  color: #fff;
  border-bottom-right-radius: 2px;
}

.message-other .message-bubble {
  background-color: #fff;
  color: #323233;
  border-bottom-left-radius: 2px;
}

.message-time {
  margin-top: 4px;
  font-size: 11px;
  color: #c8c9cc;
}

.chat-input-container {
  padding: 8px;
  background-color: #fff;
  border-top: 1px solid #ebedf0;
}

:deep(.van-field) {
  padding: 8px 12px;
}

:deep(.van-field__control) {
  font-size: 14px;
}

.send-failed-icon {
  margin-left: 4px;
  font-size: 14px;
}

.message-item {
  display: flex;
  margin-bottom: 12px;
  animation: message-fade-in 0.3s ease-out;
}

@keyframes message-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>