/**
 * WebSocket工具类
 * 管理聊天连接的建立、消息发送和接收
 */

class WebSocketManager {
  constructor() {
    this.ws = null
    this.userId = null
    this.role = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.messageHandlers = []
    this.isConnecting = false
    this.heartbeatTimer = null
    this.heartbeatInterval = 10000
    this.connectionStatus = 'disconnected'
    this.statusChangeHandlers = []
    // 保存连接回调，用于重连
    this.reconnectCallbacks = {
      onMessage: null,
      onError: null,
      onOpen: null
    }
  }

  /**
   * 添加连接状态变化处理器
   * @param {Function} handler 状态变化处理函数
   */
  addStatusChangeHandler(handler) {
    if (typeof handler === 'function') {
      this.statusChangeHandlers.push(handler);
      // 立即触发一次当前状态
      handler(this.connectionStatus);
    }
  }
  
  /**
   * 移除连接状态变化处理器
   * @param {Function} handler 状态变化处理函数
   */
  removeStatusChangeHandler(handler) {
    const index = this.statusChangeHandlers.indexOf(handler);
    if (index > -1) {
      this.statusChangeHandlers.splice(index, 1);
    }
  }
  
  /**
   * 更新连接状态并通知所有处理器
   * @param {string} status 新状态: 'connecting', 'connected', 'disconnected', 'reconnecting'
   */
  updateConnectionStatus(status) {
    if (this.connectionStatus !== status) {
      this.connectionStatus = status;
      this.statusChangeHandlers.forEach(handler => {
        try {
          handler(status);
        } catch (error) {
          console.error('状态变化处理器执行失败', error);
        }
      });
    }
  }
  
  /**
   * 启动心跳检测
   */
  startHeartbeat() {
    // 清除现有定时器
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
    }
  
    // 每10秒发送一次心跳
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.send(JSON.stringify({ type: 'heartbeat', timestamp: Date.now() }));
        } catch (error) {
          console.error('发送心跳失败', error);
          // 发送失败可能表示连接已断开，触发重连
          this.handleConnectionError();
        }
      }
    }, this.heartbeatInterval);
  }
  
  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }
  
  /**
   * 处理连接错误，触发重连
   */
  handleConnectionError() {
    this.updateConnectionStatus('disconnected');
    this.stopHeartbeat();
  
    // 只有在userId和role都有效时才重连
    if (this.userId != null && this.role != null && 
        isFinite(this.userId) && this.userId > 0 && 
        isFinite(this.role) && this.role >= 0) {
      this.updateConnectionStatus('reconnecting');
      this.attemptReconnect(
        this.userId, 
        this.role,
        this.reconnectCallbacks.onMessage,
        this.reconnectCallbacks.onError,
        this.reconnectCallbacks.onOpen
      );
    } else {
      console.warn('无法重连：userId或role无效', { userId: this.userId, role: this.role });
    }
  }
  
   /**
    * 连接WebSocket
    * @param {Number} userId 用户ID
    * @param {Number} role 用户角色 0-学生 1-商家 2-管理员
    * @param {Function} onMessage 消息接收回调
    * @param {Function} onError 错误回调
    * @param {Function} onOpen 连接成功回调（可选）
    */
   connect(userId, role, onMessage, onError, onOpen) {
    // 严格的参数验证（必须在方法开头）
    // 检查原始值是否为null/undefined/字符串"null"/字符串"undefined"
    if (userId == null || userId === undefined || userId === 'null' || userId === 'undefined' || userId === '') {
      console.error('WebSocket连接失败：userId为null/undefined/空字符串', userId, typeof userId)
      if (onError) {
        onError(new Error('无效的用户ID'))
      }
      return
    }
    
    // 转换为数字
    const finalUserId = Number(userId)
    
    // 验证转换后的值
    if (!isFinite(finalUserId) || isNaN(finalUserId) || finalUserId <= 0) {
      console.error('WebSocket连接失败：userId不是有效的正数', { 
        original: userId, 
        converted: finalUserId, 
        type: typeof userId 
      })
      if (onError) {
        onError(new Error('无效的用户ID'))
      }
      return
    }
    
    // 处理role参数
    let finalRole = 0
    if (role != null && role !== undefined && role !== 'null' && role !== 'undefined' && role !== '') {
      finalRole = Number(role)
      if (!isFinite(finalRole) || isNaN(finalRole) || finalRole < 0) {
        console.warn('WebSocket连接：role无效，使用默认值0', { 
          original: role, 
          converted: finalRole 
        })
        finalRole = 0
      }
    }
    
    this.updateConnectionStatus('connecting');
    
     // 如果已经连接且用户ID和角色匹配，直接添加消息处理器
     if (this.ws && this.ws.readyState === WebSocket.OPEN && this.userId === finalUserId && this.role === finalRole) {
       console.log('WebSocket已连接，直接添加消息处理器', { userId: finalUserId, role: finalRole })
       if (onMessage) {
         const index = this.messageHandlers.indexOf(onMessage)
         if (index === -1) {
           this.messageHandlers.push(onMessage)
         }
       }
       if (onOpen) {
         onOpen()
       }
      this.updateConnectionStatus('connected');
       return
     }

    // 最后一次验证，确保值有效（防御性编程）
    if (!isFinite(finalUserId) || isNaN(finalUserId) || finalUserId <= 0 || !isFinite(finalRole) || isNaN(finalRole) || finalRole < 0) {
      console.error('WebSocket连接失败：参数验证失败', { 
        finalUserId, 
        finalRole, 
        originalUserId: userId,
        originalRole: role,
        userIdType: typeof userId,
        roleType: typeof role
      })
      if (onError) {
        onError(new Error('参数验证失败'))
      }
      return
    }
    
    // 转换为字符串并再次验证
    const userIdStr = String(finalUserId)
    const roleStr = String(finalRole)
    
    // 检查字符串是否包含无效值
    if (userIdStr === 'null' || userIdStr === 'undefined' || userIdStr === 'NaN' || 
        roleStr === 'null' || roleStr === 'undefined' || roleStr === 'NaN' ||
        userIdStr.trim() === '' || roleStr.trim() === '') {
      console.error('WebSocket连接失败：URL参数包含无效值', { 
        userIdStr, 
        roleStr, 
        finalUserId, 
        finalRole,
        originalUserId: userId,
        originalRole: role
      })
      if (onError) {
        onError(new Error('URL参数无效'))
      }
      return
    }
    
    // 设置实例变量（在验证通过后）
    this.userId = finalUserId
    this.role = finalRole
    this.isConnecting = true
    
    // 保存回调函数，用于重连
    this.reconnectCallbacks.onMessage = onMessage
    this.reconnectCallbacks.onError = onError
    this.reconnectCallbacks.onOpen = onOpen
    
    // 构建WebSocket URL
    // 后端配置了 context-path: /api，所以WebSocket路径需要加上 /api 前缀
    // 后端 @ServerEndpoint("/chat/{userId}/{role}") 使用斜杠分隔参数
    let wsUrl
    try {
      if (import.meta.env.DEV) {
        // 开发环境：使用相对路径，通过vite代理
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        wsUrl = `${protocol}//${window.location.host}/api/chat/${userIdStr}/${roleStr}`
      } else {
        // 生产环境：使用配置的WebSocket地址
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const host = import.meta.env.VITE_WS_HOST || window.location.host.replace(/:\d+$/, ':8080')
        wsUrl = `${protocol}//${host}/api/chat/${userIdStr}/${roleStr}`
      }
      
      // 最后检查URL是否包含null或undefined
      if (wsUrl.includes('null') || wsUrl.includes('undefined') || wsUrl.includes('NaN')) {
        console.error('WebSocket连接失败：URL包含无效值', { wsUrl, userIdStr, roleStr, finalUserId, finalRole })
        if (onError) {
          onError(new Error('URL包含无效值'))
        }
        return
      }
      
      console.log('WebSocket连接URL:', wsUrl, { 
        userId: finalUserId, 
        role: finalRole, 
        userIdStr, 
        roleStr,
        originalUserId: userId,
        originalRole: role
      })
    } catch (error) {
      console.error('构建WebSocket URL失败', error, { userIdStr, roleStr, finalUserId, finalRole })
      if (onError) {
        onError(new Error('构建URL失败'))
      }
      return
    }

    // 最后一次安全检查：确保URL有效
    if (!wsUrl || wsUrl.includes('null') || wsUrl.includes('undefined') || wsUrl.includes('NaN')) {
      console.error('WebSocket连接失败：URL无效', { wsUrl, userIdStr, roleStr, finalUserId, finalRole })
      this.isConnecting = false
      if (onError) {
        onError(new Error('URL无效'))
      }
      return
    }

    try {
      console.log('正在创建WebSocket连接...', { wsUrl, userIdStr, roleStr, finalUserId, finalRole })
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功', { userId: finalUserId, role: finalRole, wsUrl })
        this.isConnecting = false
        this.reconnectAttempts = 0
        this.updateConnectionStatus('connected');
        this.startHeartbeat();
         if (onMessage) {
           const index = this.messageHandlers.indexOf(onMessage)
           if (index === -1) {
             this.messageHandlers.push(onMessage)
           }
         }
         if (onOpen) {
           onOpen()
         }
       }

      this.ws.onmessage = (event) => {
        const message = event.data
        console.log('收到消息:', message)
        this.messageHandlers.forEach(handler => {
          try {
            handler(message)
          } catch (error) {
            console.error('消息处理失败', error)
          }
        })
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket错误', error)
        this.isConnecting = false
        this.handleConnectionError();
        if (onError) {
          onError(error)
        }
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭', { code: event.code, reason: event.reason })
        this.isConnecting = false
        this.stopHeartbeat();
        // 只有在非正常关闭时才自动重连，且确保userId和role有效
        if (event.code !== 1000 && 
            this.userId != null && this.role != null && 
            isFinite(this.userId) && this.userId > 0 && 
            isFinite(this.role) && this.role >= 0) {
          this.updateConnectionStatus('reconnecting');
          this.attemptReconnect(
            this.userId, 
            this.role, 
            this.reconnectCallbacks.onMessage, 
            this.reconnectCallbacks.onError,
            this.reconnectCallbacks.onOpen
          )
        } else {
          this.updateConnectionStatus('disconnected');
          if (event.code === 1000) {
            console.log('WebSocket正常关闭，不重连')
          } else {
            console.warn('WebSocket异常关闭，但参数无效，不重连', { 
              userId: this.userId, 
              role: this.role,
              code: event.code 
            })
          }
        }
      }
    } catch (error) {
      console.error('WebSocket连接失败', error)
      this.isConnecting = false
      if (onError) {
        onError(error)
      }
    }
  }

  /**
   * 尝试重连
   */
  attemptReconnect(userId, role, onMessage, onError, onOpen) {
    // 严格验证参数
    if (userId == null || userId === undefined || userId === 'null' || userId === 'undefined' || userId === '') {
      console.error('重连失败：无效的userId', userId, typeof userId)
      return
    }
    
    const finalUserId = Number(userId)
    if (!isFinite(finalUserId) || isNaN(finalUserId) || finalUserId <= 0) {
      console.error('重连失败：userId不是有效的正数', { original: userId, converted: finalUserId })
      return
    }
    
    let finalRole = 0
    if (role != null && role !== undefined && role !== 'null' && role !== 'undefined' && role !== '') {
      finalRole = Number(role)
      if (!isFinite(finalRole) || isNaN(finalRole) || finalRole < 0) {
        console.warn('重连：role无效，使用默认值0', { original: role, converted: finalRole })
        finalRole = 0
      }
    }
    
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('达到最大重连次数，停止重连')
      return
    }

    this.reconnectAttempts++
    console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`, { userId: finalUserId, role: finalRole })

    this.reconnectTimer = setTimeout(() => {
      this.connect(finalUserId, finalRole, onMessage, onError, onOpen)
    }, this.reconnectInterval)
  }

  /**
   * 发送消息
   * @param {Number} receiveUserId 接收者用户ID
   * @param {String} message 消息内容
   */
  sendMessage(receiveUserId, message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocket未连接')
      return false
    }

    if (!message || !message.trim()) {
      console.error('消息内容不能为空')
      return false
    }

    try {
      // 后端onMessage方法接收格式：目标用户ID|消息内容
      const entireMessage = `${receiveUserId}|${message.trim()}`
      this.ws.send(entireMessage)
      return true
    } catch (error) {
      console.error('发送消息失败', error)
      return false
    }
  }

  /**
   * 添加消息处理器
   * @param {Function} handler 消息处理函数
   */
  addMessageHandler(handler) {
    if (typeof handler === 'function') {
      this.messageHandlers.push(handler)
    }
  }

  /**
   * 移除消息处理器
   * @param {Function} handler 消息处理函数
   */
  removeMessageHandler(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  /**
   * 关闭连接
   */
  disconnect() {
    this.updateConnectionStatus('disconnected');
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    this.stopHeartbeat();

     if (this.ws) {
       this.ws.close()
       this.ws = null
     }

    this.messageHandlers = []
    this.isConnecting = false
    this.reconnectAttempts = 0
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 创建单例
const wsManager = new WebSocketManager()

export default wsManager