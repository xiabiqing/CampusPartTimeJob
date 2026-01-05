package fun.xiabiqing.websocket;

import fun.xiabiqing.common.ErrorCode;
import fun.xiabiqing.config.SpringContextHolder;
import fun.xiabiqing.exception.BusinessException;
import fun.xiabiqing.model.domain.ChatMessage;
import fun.xiabiqing.model.domain.User;
import fun.xiabiqing.service.ChatMessageService;
import fun.xiabiqing.service.UserService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
//路径接收参数(类级别)
@ServerEndpoint("/chat/{userId}/{role}")
@Slf4j
public class ChatServer {
    // WebSocket端点不是Spring管理的Bean，@Autowired不会生效，需要使用SpringContextHolder手动获取
    private UserService userService;
    private ChatMessageService chatMessageService;
    
    public static final ConcurrentHashMap<Long,ChatServer> ONLINE_USER_MAP = new ConcurrentHashMap<>();
    private Session session;
    private Long userId;
    private Integer role;

    /**
     * 初始化服务（在需要使用服务时调用）
     */
    private void initServices() {
        if (userService == null) {
            userService = SpringContextHolder.getBean(UserService.class);
        }
        if (chatMessageService == null) {
            chatMessageService = SpringContextHolder.getBean(ChatMessageService.class);
        }
    }
    /**
     * 建立成功执行的方法，主要是将将用户的id和chatserver存储在全局聊天对象里面,注意这里@pathparam必须写，springmvc重名时做了自动映射，websocket可没与
     * 使用String类型接收路径参数，然后在方法内部解析，避免路径参数为null时解析失败
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr, @PathParam("role") String roleStr){
        // 解析和验证userId
        try {
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                log.error("WebSocket连接失败：userId参数为空或null userIdStr={}", userIdStr);
                throw new IllegalArgumentException("userId参数为空或null");
            }
            userId = Long.parseLong(userIdStr.trim());
        } catch (IllegalArgumentException e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无效的用户ID"));
            } catch (IOException ioException) {
                log.error("关闭WebSocket会话失败", ioException);
            }
            return;
        }

        // 解析和验证role
            if (roleStr == null || roleStr.trim().isEmpty()) {
                log.warn("role参数为空，使用默认值0");
                role = 0;
            } else {
                role = Integer.parseInt(roleStr.trim());
            }


        // 设置实例变量
        this.session = session;
        this.userId = userId;
        this.role = role;

        // 初始化服务
        try {
            initServices();
        } catch (Exception e) {
            log.error("初始化服务失败", e);
            try {
                session.close();
            } catch (IOException ioException) {
                throw new BusinessException(ErrorCode.WEBSOCKET_CLOSE);
            }
            return;
        }

        // 添加到在线用户Map
        try {
            ONLINE_USER_MAP.put(userId, this);
        } catch (Exception e) {
            try {
                session.close();
            } catch (IOException ioException) {
                throw new BusinessException(ErrorCode.WEBSOCKET_CLOSE);
            }
            return;
        }

    }

    /**
     * 这个messsage里面不仅仅是发送的内容还有，发送者
     * @param entireMessage 发送完全信息(接收者id和发送的信息)
     */
    @OnMessage
    public void onMessage(String entireMessage){
        // 确保服务已初始化
        initServices();



        // 检查是否是心跳消息（JSON格式：{"type":"heartbeat","timestamp":...}）
        if (entireMessage != null && entireMessage.trim().startsWith("{") && entireMessage.contains("\"type\":\"heartbeat\"")) {
            return;
        }
        
        // 解析消息格式：接收者ID|消息内容
        String[] split = entireMessage.split("\\|", 2);

            Long receiveUserId = Long.valueOf(split[0]);
            String message = split[1].trim();
            
            if (message.isEmpty()) {
                log.warn("消息内容为空，忽略");
                return;
            }
            
            // 构建消息格式：【role-userId】：message
            String sendMsg = "【" + this.role + "-" + this.userId + "】：" + message;
            
            // 查询接收方用户信息
            User receiveUser = userService.getById(receiveUserId);
            if (receiveUser == null) {
                throw new BusinessException(400050,"接收者人不存在");
            }
            
            // 保存到 ChatMessage 表（持久化）
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSendUserId(this.userId);
            chatMessage.setSendRole(this.role);
            chatMessage.setReceiveUserId(receiveUserId);
            chatMessage.setReceiveRole(receiveUser.getRole());
            chatMessage.setMsgContent(sendMsg);
            chatMessageService.save(chatMessage);
            
            // 1. 发送消息回显给发送者（让发送者看到自己发送的消息）
            try {
                this.sendMsgToSelf(sendMsg);
            } catch (Exception e) {
                log.error("发送消息回显失败", e);
            }
            
            // 2. 发送消息给接收者
            ChatServer receiveChatServer = ONLINE_USER_MAP.get(receiveUserId);
            if(receiveChatServer != null){
                    receiveChatServer.sendMsgToSelf(sendMsg);
            }


    }

    /**
     * 本质就是把自己的信息展示给前端让自己看到,谁接收消息谁调用
     * @param message
     */
    private void sendMsgToSelf(String message){
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new BusinessException(400030,"发送信息失败");
        }
    }
    @OnClose
    public void onClose(){
            // 只有在userId不为null时才从Map中移除
            if (this.userId != null) {
                try {
                    ONLINE_USER_MAP.remove(this.userId);
                } catch (Exception e) {
                    log.error("在线用户Map中移除用户失败，userId={}", this.userId, e);
                }
            } else {
                log.warn("WebSocket连接关闭，但userId为null（可能是连接建立失败）");
            }

    }

    //自动触发
    @OnError
    public void onError(Session session, Throwable error) {
            // 只有在userId不为null时才从Map中移除
            if (this.userId != null) {
                try {
                    ONLINE_USER_MAP.remove(this.userId);
                } catch (Exception e) {
                    log.error("从在线用户Map中移除用户失败，userId={}", this.userId, e);
                }
            }

    }


}




