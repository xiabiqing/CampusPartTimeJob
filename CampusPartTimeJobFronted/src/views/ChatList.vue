<template>
  <div class="chat-list-page">
    <van-nav-bar title="聊天" fixed placeholder />
    
    <div class="chat-list-container">
      <van-empty
        v-if="!loading && contactList.length === 0"
        description="暂无聊天对象"
        image="chat"
      />
      
      <van-skeleton
        v-if="loading"
        title
        avatar
        :row="2"
        :loading="loading"
      />
      
      <van-cell-group v-else>
        <van-cell
          v-for="contact in contactList"
          :key="contact.id"
          :title="contact.name || `用户${contact.id}`"
          :label="contact.lastMessage || '暂无消息'"
          is-link
          @click="goToChat(contact)"
        >
          <template #icon>
            <van-image
              round
              width="40"
              height="40"
              :src="contact.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
              fit="cover"
            />
          </template>
          <template #right-icon>
            <div class="unread-container">
              <van-badge
                v-if="contact.unreadCount > 0"
                :content="contact.unreadCount > 99 ? '99+' : contact.unreadCount"
                class="unread-badge"
              />
              <span v-if="contact.lastMessageTime" class="last-message-time">{{ formatTime(contact.lastMessageTime) }}</span>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getChatContacts, getChatHistory } from '@/api/chat'
import { showFailToast } from 'vant'
import wsManager from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const contactList = ref([])

// 加载联系人列表
const loadContacts = async () => {
  loading.value = true
  try {
    const userInfo = userStore.userInfo || userStore.userDetail
    if (!userInfo) {
      showFailToast('请先登录')
      router.push('/login')
      return
    }

    const role = userInfo.role !== undefined && userInfo.role !== null ? userInfo.role : 0
    
    // 调用API获取联系人列表
    const res = await getChatContacts(role)
    console.log('联系人列表API响应:', res)
    
    if (res && res.code === 200 && res.data) {
      // 处理联系人数据，添加最后一条消息和未读数
      const dataArray = Array.isArray(res.data) ? res.data : Array.from(res.data || [])
      console.log('联系人数据:', dataArray, '数量:', dataArray.length)
      
      const contacts = dataArray.map(contact => {
        // 确保ID是数字类型
        const contactId = Number(contact.id)
        if (!contactId || isNaN(contactId)) {
          console.warn('联系人ID无效，跳过', contact)
          return null
        }
        
        // 确定联系人的角色：如果当前用户是学生(0)，联系人就是商家(1)；如果当前用户是商家(1)，联系人就是学生(0)
        const contactRole = role === 0 ? 1 : 0
        
        return {
          id: contactId,
          name: contact.username || `用户${contactId}`,
          role: contact.role !== undefined && contact.role !== null ? Number(contact.role) : contactRole,
          avatar: contact.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
          lastMessage: '', // 后续可以从聊天记录中获取
          unreadCount: contact.unreadCount || 0,
          lastMessageTime: null
        }
      }).filter(contact => contact !== null) // 过滤掉无效的联系人
      
      console.log('处理后的联系人列表:', contacts)
      
      // 解析消息内容（去除【role-userId】：前缀）
      const parseMessageContent = (msgContent) => {
        if (!msgContent) return ''
        const match = msgContent.match(/^【\d+-\d+】：(.+)$/)
        return match ? match[1] : msgContent
      }
      
      // 为每个联系人加载最后一条消息（可选，如果接口有问题可以先注释掉）
      const contactsWithLastMessage = await Promise.all(
        contacts.map(async (contact) => {
          try {
            const historyRes = await getChatHistory(contact.id)
            if (historyRes && historyRes.code === 200 && historyRes.data && historyRes.data.length > 0) {
              // 获取最后一条消息，解析出纯文本内容
              const lastMsg = historyRes.data[historyRes.data.length - 1]
              contact.lastMessage = parseMessageContent(lastMsg.msgContent) || ''
            }
          } catch (error) {
            // 如果获取历史记录失败，不影响联系人列表的显示
            console.error(`获取联系人${contact.id}的最后一条消息失败`, error)
            // 不设置lastMessage，保持为空字符串
          }
          return contact
        })
      )
      
      contactList.value = contactsWithLastMessage
    } else {
      contactList.value = []
    }
  } catch (error) {
    console.error('加载联系人列表失败', error)
    const errorMsg = error.response?.data?.msg || error.message || '加载联系人列表失败'
    console.error('错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      fullUrl: error.config?.baseURL + error.config?.url
    })
    showFailToast(errorMsg)
    contactList.value = []
  } finally {
    loading.value = false
  }
}

// 跳转到聊天页面
const goToChat = (contact) => {
  resetUnreadCount(contact.id)
  router.push({
    name: 'Chat',
    params: {
      userId: contact.id,
      role: contact.role
    },
    query: {
      name: contact.name
    }
  })
}

// 解析消息内容（去除【role-userId】：前缀）
const parseMessageContent = (msgContent) => {
  if (!msgContent) return ''
  const match = msgContent.match(/^【\d+-\d+】：(.+)$/)
  return match ? match[1] : msgContent
}

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

// 当前聊天对象ID（用于处理发送消息的回显）
const currentChatUserId = ref(null)

// 监听路由变化，记录当前聊天对象
router.afterEach((to) => {
  if (to.name === 'Chat' && to.params.userId) {
    currentChatUserId.value = Number(to.params.userId)
  } else {
    currentChatUserId.value = null
  }
})

// 处理WebSocket消息，实时更新联系人列表
const handleWebSocketMessage = (message) => {
  console.log('ChatList收到WebSocket消息:', message)
  
  const parsed = parseMessage(message)
  if (!parsed) {
    console.log('消息解析失败:', message)
    return
  }
  
  const userInfo = userStore.userInfo || userStore.userDetail
  if (!userInfo) {
    console.log('用户信息不存在')
    return
  }
  
  console.log('消息解析结果:', {
    parsed,
    currentUserId: userInfo.id,
    currentUserRole: userInfo.role,
    senderId: parsed.userId,
    senderRole: parsed.role
  })
  
  // 判断消息的发送者ID
  const senderId = parsed.userId
  const currentUserId = Number(userInfo.id)
  
  // 确定是哪个联系人的消息
  let targetContactId = null
  
  if (senderId === currentUserId) {
    // 当前用户发送的消息（回显）
    // 如果当前在聊天页面，更新当前聊天对象的信息
    if (currentChatUserId.value) {
      targetContactId = currentChatUserId.value
      console.log('处理发送消息的回显，目标联系人ID:', targetContactId)
    } else {
      // 不在聊天页面，无法确定接收者，不处理
      console.log('发送消息的回显，但不在聊天页面，跳过')
      return
    }
  } else {
    // 别人发送给当前用户的消息
    targetContactId = senderId
    console.log('收到别人发送的消息，发送者ID:', targetContactId)
    
    // 增加未读消息计数
    incrementUnreadCount(targetContactId)
  }
  
  // 找到对应的联系人（确保ID类型一致）
  const contactIndex = contactList.value.findIndex(contact => {
    const contactId = Number(contact.id)
    const targetId = Number(targetContactId)
    return contactId === targetId
  })
  
  console.log('查找联系人结果:', {
    targetContactId,
    targetContactIdType: typeof targetContactId,
    contactIndex,
    contactListLength: contactList.value.length,
    contactIds: contactList.value.map(c => ({ id: c.id, idType: typeof c.id }))
  })
  
  if (contactIndex !== -1) {
    // 更新最后一条消息和时间
    const contact = contactList.value[contactIndex]
    contact.lastMessage = parsed.content
    contact.lastMessageTime = new Date().toISOString()
    
    // 将该联系人移到列表顶部（最新消息在顶部）
    const updatedContact = contactList.value.splice(contactIndex, 1)[0]
    contactList.value.unshift(updatedContact)
    
    console.log('实时更新联系人列表成功:', {
      name: updatedContact.name,
      content: parsed.content,
      role: userInfo.role
    })
  } else {
    // 如果联系人不在列表中，可能是新联系人，重新加载列表
    console.log('收到新联系人的消息，重新加载联系人列表，发送者ID:', targetContactId)
    loadContacts()
  }
}

// 连接WebSocket（如果还没有连接）
const connectWebSocket = () => {
  const userInfo = userStore.userInfo || userStore.userDetail
  if (!userInfo) {
    console.log('ChatList: 用户信息不存在，无法连接WebSocket')
    return
  }

  // 严格验证userId
  if (!userInfo.id || userInfo.id === null || userInfo.id === undefined || userInfo.id === 'null' || userInfo.id === 'undefined') {
    console.error('ChatList: 无效的userId', userInfo.id, 'userInfo:', userInfo)
    return
  }

  const userId = Number(userInfo.id)
  if (!isFinite(userId) || userId <= 0 || isNaN(userId)) {
    console.error('ChatList: 无效的userId数值', userInfo.id, '转换后:', userId)
    return
  }

  // 严格验证role
  let role = 0
  if (userInfo.role !== undefined && userInfo.role !== null && userInfo.role !== 'null' && userInfo.role !== 'undefined') {
    role = Number(userInfo.role)
    if (!isFinite(role) || isNaN(role) || role < 0) {
      console.warn('ChatList: role无效，使用默认值0', userInfo.role, '转换后:', role)
      role = 0
    }
  }

  console.log('ChatList: 准备连接WebSocket', {
    userId,
    role,
    isConnected: wsManager.isConnected(),
    wsState: wsManager.ws?.readyState
  })

  // 如果WebSocket已经连接，直接添加消息处理器
  if (wsManager.isConnected()) {
    wsManager.addMessageHandler(handleWebSocketMessage)
    console.log('ChatList: WebSocket已连接，添加消息处理器', {
      userId,
      role,
      handlerCount: wsManager.messageHandlers.length
    })
    return
  }

  // 如果WebSocket未连接，先连接再添加消息处理器
  console.log('ChatList: WebSocket未连接，开始连接', { userId, role })
  wsManager.connect(
    userId,
    role,
    handleWebSocketMessage,
    (error) => {
      console.error('ChatList: WebSocket连接错误', error, { userId, role })
    }
  )
  
  // 等待连接建立后再次检查
  setTimeout(() => {
    if (wsManager.isConnected()) {
      console.log('ChatList: WebSocket连接成功，消息处理器已添加')
    } else {
      console.warn('ChatList: WebSocket连接可能失败', {
        userId,
        role,
        wsState: wsManager.ws?.readyState
      })
    }
  }, 2000)
}

onMounted(() => {
  loadContacts()
  // 连接WebSocket以接收实时消息
  connectWebSocket()
  
  // 初始化当前聊天对象ID
  const currentRoute = router.currentRoute.value
  if (currentRoute.name === 'Chat' && currentRoute.params.userId) {
    currentChatUserId.value = Number(currentRoute.params.userId)
  }
})

onUnmounted(() => {
  // 移除消息处理器，避免内存泄漏
  wsManager.removeMessageHandler(handleWebSocketMessage)
  // 注意：不要断开WebSocket连接，因为聊天页面可能还在使用
})

// 增加未读消息计数
const incrementUnreadCount = (contactId) => {
  const contactIndex = contactList.value.findIndex(contact => Number(contact.id) === Number(contactId))
  if (contactIndex !== -1) {
    contactList.value[contactIndex].unreadCount++
  }
}

// 重置未读消息计数
const resetUnreadCount = (contactId) => {
  const contactIndex = contactList.value.findIndex(contact => Number(contact.id) === Number(contactId))
  if (contactIndex !== -1) {
    contactList.value[contactIndex].unreadCount = 0
  }
}

// 格式化时间显示
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
  
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  // 今年
  else if (date.getFullYear() === now.getFullYear()) {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
  // 往年
  else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped>
.chat-list-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.chat-list-container {
  padding: 8px 0;
}

.unread-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.last-message-time {
  font-size: 11px;
  color: #c8c9cc;
  margin-bottom: 2px;
}

.unread-badge {
  margin-top: 2px;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-cell__left-icon) {
  margin-right: 12px;
}

:deep(.van-cell__title) {
  font-size: 16px;
  font-weight: 500;
}

:deep(.van-cell__label) {
  margin-top: 4px;
  color: #969799;
  font-size: 14px;
}
</style>