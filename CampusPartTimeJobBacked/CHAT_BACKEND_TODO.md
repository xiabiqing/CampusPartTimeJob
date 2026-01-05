# 实时聊天后端需要实现的功能

## 1. 创建聊天消息实体类（ChatMessage）

需要创建一个新的实体类来持久化存储所有聊天记录，因为 `OfflineMsg` 主要用于离线消息暂存。

**文件位置：** `src/main/java/fun/xiabiqing/model/domain/ChatMessage.java`

**字段：**
- `id` (Long) - 主键，自增
- `sendUserId` (Long) - 发送方ID
- `sendRole` (Integer) - 发送方角色（0学生，1商家）
- `receiveUserId` (Long) - 接收方ID
- `receiveRole` (Integer) - 接收方角色（0学生，1商家）
- `msgContent` (String) - 消息内容
- `createTime` (Date) - 创建时间
- `isDelete` (Integer) - 逻辑删除标记（0未删除，1已删除）

**注解：**
- `@TableName("chat_message")`
- `@TableId(type = IdType.AUTO)`
- `@TableLogic` 用于 `isDelete` 字段

## 2. 创建 ChatController

**文件位置：** `src/main/java/fun/xiabiqing/controller/ChatController.java`

### 2.1 获取联系人列表接口

**路径：** `GET /chat/contacts`

**功能：**
- 根据当前用户的角色和申请记录获取联系人列表
- **学生用户：** 从 `JobApplication` 表中查询 `stuId = 当前用户ID` 的记录，获取对应的商家信息
  - 通过 `recruId` 关联 `Recruitment` 表
  - 通过 `Recruitment.busIdRecru` 关联 `User` 表获取商家信息
- **商家用户：** 从 `JobApplication` 表中查询该商家发布的招聘信息对应的申请记录
  - 通过 `recruId` 关联 `Recruitment` 表（`busIdRecru = 当前用户ID`）
  - 通过 `JobApplication.stuId` 关联 `User` 表获取学生信息

**返回数据格式：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "商家名称",
      "role": 1,
      "avatar": "头像URL",
      "lastMessage": "最后一条消息",
      "unreadCount": 0
    }
  ]
}
```

**实现逻辑：**
1. 从 session 获取当前登录用户
2. 根据用户角色查询对应的 `JobApplication` 记录
3. 根据申请记录关联查询联系人信息（User表）
4. 查询每个联系人的最后一条消息（从 `ChatMessage` 表）
5. 统计未读消息数（可选，需要添加未读标记字段）

### 2.2 获取聊天历史记录接口

**路径：** `GET /chat/history/{targetUserId}`

**功能：**
- 查询当前用户与目标用户之间的聊天历史记录
- 从 `ChatMessage` 表中查询：
  - `(sendUserId = 当前用户ID AND receiveUserId = targetUserId)`
  - 或 `(sendUserId = targetUserId AND receiveUserId = 当前用户ID)`
- 按 `createTime` 升序排序
- 支持分页（可选）

**返回数据格式：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "sendUserId": 1,
      "sendRole": 0,
      "receiveUserId": 2,
      "receiveRole": 1,
      "msgContent": "【0-1】：你好",
      "createTime": "2024-01-01 12:00:00",
      "isSelf": false
    }
  ]
}
```

**实现逻辑：**
1. 从 session 获取当前登录用户
2. 构建查询条件：双向查询（发送方或接收方）
3. 按时间排序
4. 返回消息列表

## 3. 修改 ChatServer 以持久化消息

**文件位置：** `src/main/java/fun/xiabiqing/websocket/ChatServer.java`

**修改点：**
- 在 `onMessage` 方法中，无论是发送给在线用户还是离线用户，都需要将消息保存到 `ChatMessage` 表中
- 离线消息仍然保存到 `OfflineMsg` 表中用于推送

**修改后的逻辑：**

```java
@OnMessage
public void onMessage(String entireMessage) {
    // 1. 解析消息
    String[] split = entireMessage.split("\\|", 2);
    Long targetUserId = Long.valueOf(split[0]);
    String message = String.valueOf(split[1]);
    
    // 2. 构建消息内容
    String sendMsg = "【" + role + "-" + this.userId + "】：" + message;
    
    // 3. 保存到 ChatMessage 表（持久化）
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.setSendUserId(this.userId);
    chatMessage.setSendRole(this.role);
    chatMessage.setReceiveUserId(targetUserId);
    // 需要查询接收方的角色
    chatMessage.setReceiveRole(receiveRole);
    chatMessage.setMsgContent(sendMsg);
    chatMessageService.save(chatMessage);
    
    // 4. 如果接收方在线，直接发送
    ChatServer chatServer = ONLINE_USER_MAP.get(targetUserId);
    if (chatServer != null) {
        chatServer.sendMsgToSelf(sendMsg);
    } else {
        // 5. 如果接收方离线，保存到 OfflineMsg 表（用于推送）
        OfflineMsg offlineMsg = new OfflineMsg();
        offlineMsg.setSendUserId(this.userId);
        offlineMsg.setSendRole(this.role);
        offlineMsg.setReceiveUserId(targetUserId);
        offlineMsg.setMsgContent(sendMsg);
        offlineMsgService.save(offlineMsg);
    }
}
```

**注意：** 需要在 `ChatServer` 中注入 `ChatMessageService` 和查询接收方角色的方法。

## 4. 创建 ChatMessageService 和 ChatMessageMapper

**文件位置：**
- `src/main/java/fun/xiabiqing/service/ChatMessageService.java`
- `src/main/java/fun/xiabiqing/service/impl/ChatMessageServiceImpl.java`
- `src/main/java/fun/xiabiqing/mapper/ChatMessageMapper.java`

**Service 方法：**
- `save(ChatMessage)` - 保存消息
- `listChatHistory(Long currentUserId, Long targetUserId)` - 查询聊天历史
- `getLastMessage(Long userId1, Long userId2)` - 获取最后一条消息（用于联系人列表）

## 5. 创建 VO 类用于返回联系人信息

**文件位置：** `src/main/java/fun/xiabiqing/model/vo/ChatContactVO.java`

**字段：**
- `id` (Long) - 用户ID
- `name` (String) - 用户名
- `role` (Integer) - 角色
- `avatar` (String) - 头像
- `lastMessage` (String) - 最后一条消息
- `unreadCount` (Integer) - 未读消息数（可选，需要添加未读标记字段）

## 6. 数据库表结构

需要创建 `chat_message` 表：

```sql
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `send_user_id` BIGINT NOT NULL COMMENT '发送方ID',
  `send_role` INT NOT NULL COMMENT '发送方角色 0学生 1商家',
  `receive_user_id` BIGINT NOT NULL COMMENT '接收方ID',
  `receive_role` INT NOT NULL COMMENT '接收方角色 0学生 1商家',
  `msg_content` TEXT NOT NULL COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` INT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
  PRIMARY KEY (`id`),
  INDEX `idx_send_receive` (`send_user_id`, `receive_user_id`),
  INDEX `idx_receive_send` (`receive_user_id`, `send_user_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
```

## 7. 实现优先级

### 高优先级（必须实现）：
1. ✅ ChatMessage 实体类
2. ✅ ChatMessageService 和 Mapper
3. ✅ 修改 ChatServer 持久化消息
4. ✅ ChatController 联系人列表接口
5. ✅ ChatController 聊天历史记录接口

### 中优先级（可选，但建议实现）：
6. ChatContactVO
7. 未读消息数统计（需要在 ChatMessage 表中添加 `is_read` 字段）

### 低优先级（后续优化）：
8. 消息分页查询
9. 消息搜索功能
10. 消息撤回功能

## 8. 注意事项

1. **接收方角色查询：** 在 `ChatServer.onMessage` 中保存消息时，需要查询接收方的角色。可以通过 `UserService` 查询 `User` 表获取。

2. **联系人去重：** 在联系人列表接口中，同一个用户可能有多条申请记录，需要去重。

3. **消息格式：** 消息内容格式为 `【role-userId】：message`，前端需要解析这个格式来显示发送者信息。

4. **Session 管理：** 所有接口都需要从 `HttpServletRequest` 的 session 中获取当前登录用户。

5. **异常处理：** 需要处理用户未登录、参数错误等异常情况。

