# 校园兼职工作平台

## 📖 项目简介

校园兼职工作平台是一个前后端分离的企业级校园兼职招聘管理系统。系统支持学生、商家和管理员三种角色，提供招聘信息发布、工作申请、实时聊天等核心功能。

## 🛠️ 技术栈

### 后端技术栈

#### 核心框架
- **Spring Boot**: 3.4.0
- **Java**: 17
- **Maven**: 项目构建工具

#### 核心依赖
- **MyBatis-Plus**: 3.5.5 - ORM框架，简化数据库操作
- **MySQL Connector**: 数据库驱动
- **Spring Data Redis**: 3.4.12 - Redis缓存支持
- **Redisson**: 3.19.0 - 分布式锁和缓存
- **Spring Session**: 3.4.1 - Session管理
- **Spring WebSocket**: WebSocket实时通信
- **Knife4j**: 4.5.0 - API文档生成（基于OpenAPI 3）
- **Lombok**: 简化Java代码
- **Jakarta Validation**: 参数校验

### 前端技术栈

#### 核心框架
- **Vue**: 3.4.0 - 渐进式JavaScript框架
- **Vue Router**: 4.2.5 - Vue.js官方路由管理器
- **Pinia**: 2.1.7 - Vue的状态管理库（Vuex的替代品）

#### UI组件库
- **Vant**: 4.8.0 - 轻量、可靠的移动端Vue组件库
  - 使用的主要组件：NavBar、Form、Button、Tabbar、Cell、Tag、Toast、Dialog、Popup等

#### 工具库
- **Axios**: 1.6.2 - 基于Promise的HTTP客户端，用于API请求
- **Vite**: 5.0.0 - 下一代前端构建工具，提供极速的开发体验

#### 移动端适配
- **PostCSS**: 8.4.32 - CSS后处理器
- **postcss-px-to-viewport**: 1.1.1 - px转vw的移动端适配方案（基于375px设计稿）
- **Autoprefixer**: 10.4.16 - 自动添加CSS浏览器前缀

#### 前端特性
- ✅ **移动端优先**：使用Vant组件库，专为移动端设计
- ✅ **响应式设计**：使用px转vw实现移动端适配
- ✅ **状态管理**：使用Pinia管理全局状态（用户信息、管理员信息等）
- ✅ **路由管理**：使用Vue Router实现SPA单页应用
- ✅ **HTTP拦截**：Axios拦截器统一处理请求和响应
- ✅ **WebSocket支持**：实时聊天功能
- ✅ **开发代理**：Vite开发服务器配置API和WebSocket代理

## 📁 项目结构

```
CampusPartTimeJobBacked/
├── src/
│   ├── main/
│   │   ├── java/fun/xiabiqing/
│   │   │   ├── CampusPartTimeJobBackedApplication.java  # 启动类
│   │   │   ├── common/                                  # 通用类
│   │   │   │   ├── BaseResponse.java                    # 统一响应格式
│   │   │   │   ├── ErrorCode.java                       # 错误码枚举
│   │   │   │   └── ResultUtils.java                     # 响应工具类
│   │   │   ├── config/                                  # 配置类
│   │   │   │   ├── Knife4jConfig.java                   # API文档配置
│   │   │   │   ├── RedisConfig.java                     # Redis配置
│   │   │   │   ├── WebSocketConfig.java                 # WebSocket配置
│   │   │   │   └── SpringContextHolder.java             # Spring上下文工具
│   │   │   ├── controller/                              # 控制器层
│   │   │   │   ├── UserController.java                  # 用户相关接口
│   │   │   │   ├── UserBusinessController.java          # 商家相关接口
│   │   │   │   ├── UserStudentController.java           # 学生相关接口
│   │   │   │   ├── AdminController.java                 # 管理员接口
│   │   │   │   ├── ChatMessageController.java           # 聊天消息接口
│   │   │   │   └── RoleApplicationController.java       # 角色申请接口
│   │   │   ├── service/                                 # 服务层接口
│   │   │   │   └── impl/                                # 服务层实现
│   │   │   ├── mapper/                                  # 数据访问层
│   │   │   ├── model/                                   # 数据模型
│   │   │   │   ├── domain/                              # 实体类
│   │   │   │   ├── request/                             # 请求DTO
│   │   │   │   └── vo/                                  # 视图对象
│   │   │   ├── exception/                               # 异常处理
│   │   │   │   ├── BusinessException.java               # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java          # 全局异常处理器
│   │   │   ├── constant/                                # 常量类
│   │   │   └── websocket/                               # WebSocket服务
│   │   │       └── ChatServer.java                      # 聊天服务器
│   │   └── resources/
│   │       ├── application.yml                           # 主配置文件
│   │       ├── application-local.yml                     # 本地环境配置
│   │       └── generator/mapper/                         # MyBatis XML映射文件
│   └── test/                                             # 测试代码
└── pom.xml                                               # Maven依赖配置
```

## 🎯 核心功能模块

### 1. 用户管理模块
- **用户注册/登录**: 支持账号密码登录，Session会话管理
- **角色管理**: 支持学生(0)、商家(1)、管理员(2)三种角色
- **信息完善**: 学生和商家可分别完善个人信息
- **角色申请**: 学生可申请成为商家，管理员审核

### 2. 招聘信息管理
- **发布招聘**: 商家可发布兼职招聘信息
- **招聘列表**: 支持查询、更新、删除招聘信息
- **标签管理**: 支持为招聘信息添加标签分类

### 3. 工作申请模块
- **申请职位**: 学生可申请商家发布的职位
- **申请管理**: 商家可查看和处理工作申请
- **申请状态**: 支持同意/拒绝申请操作

### 4. 实时聊天模块
- **WebSocket通信**: 基于WebSocket的实时消息推送
- **消息持久化**: 所有聊天消息保存到数据库
- **聊天历史**: 支持查询历史聊天记录
- **联系人列表**: 根据工作申请关系自动生成联系人

### 5. 管理员模块
- **角色申请审核**: 管理员可审核学生转商家申请
- **系统管理**: 管理员权限管理

## 🗄️ 数据库设计

### 核心表结构

#### user（用户表）
- `id`: 主键ID
- `username`: 用户名
- `user_account`: 账号
- `user_password`: 密码（MD5加密）
- `role`: 角色（0学生/1商家/2管理员）
- `image`: 头像
- `creat_time`: 创建时间
- `update_time`: 更新时间
- `is_delete`: 逻辑删除标记

#### recruitment（招聘表）
- `id`: 主键ID
- `bus_id_recru`: 商家ID（外键）
- `title`: 标题
- `salary`: 薪水
- `company`: 公司名称
- `count`: 招聘人数
- `tags`: 标签（JSON格式）
- `job_details`: 岗位描述
- `create_time`: 创建时间
- `update_time`: 更新时间
- `is_delete`: 逻辑删除标记

#### job_application（工作申请表）
- `id`: 主键ID
- `stu_id`: 学生ID（外键）
- `recru_id`: 招聘ID（外键）
- `status`: 申请状态
- `create_time`: 创建时间
- `is_delete`: 逻辑删除标记

#### chat_message（聊天消息表）
- `id`: 主键ID
- `send_user_id`: 发送方ID
- `send_role`: 发送方角色
- `receive_user_id`: 接收方ID
- `receive_role`: 接收方角色
- `msg_content`: 消息内容
- `create_time`: 创建时间
- `is_delete`: 逻辑删除标记

#### role_application（角色申请表）
- `id`: 主键ID
- `stu_id`: 学生ID（外键）
- `status`: 申请状态
- `create_time`: 创建时间
- `is_delete`: 逻辑删除标记

## ⚙️ 环境要求

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 5.0+
- **IDE**: IntelliJ IDEA / Eclipse（推荐IDEA）

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/your-username/CampusPartTimeJob.git
cd CampusPartTimeJob/CampusPartTimeJobBacked
```

### 2. 配置数据库
创建MySQL数据库，并执行SQL脚本创建表结构。

### 3. 配置本地环境
在 `src/main/resources/application-local.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如果有密码
```

### 4. 运行项目
```bash
# 使用Maven运行
mvn spring-boot:run

# 或使用IDE直接运行 CampusPartTimeJobBackedApplication
```

### 5. 访问API文档
启动成功后，访问：`http://localhost:8080/api/swagger-ui.html`

## 📝 API接口说明

### 统一响应格式
```json
{
  "code": 200,
  "data": {},
  "msg": "操作成功",
  "description": ""
}
```

### 主要接口

#### 用户相关 (`/api/user`)
- `POST /register` - 用户注册
- `POST /login` - 用户登录
- `POST /updateDefaultInfo` - 更新学生信息
- `POST /updateBusInfo` - 更新商家信息
- `GET /current` - 获取当前用户信息
- `GET /application` - 获取角色申请列表

#### 商家相关 (`/api/business`)
- `POST /createMenu` - 创建招聘信息
- `GET /getMenuList` - 获取招聘列表
- `PUT /updateMenu` - 更新招聘信息
- `DELETE /deleteMenu` - 删除招聘信息
- `GET /listJobApp` - 获取工作申请列表
- `POST /dealJobApp` - 处理工作申请
- `GET /chat/contacts` - 获取联系人列表

#### 学生相关 (`/api/student`)
- `GET /getMenuList` - 获取招聘列表
- `POST /applyJob` - 申请工作
- `GET /myApplication` - 我的申请列表

#### 聊天相关 (`/api/chat`)
- `GET /history/{targetUserId}` - 获取聊天历史

#### WebSocket
- `ws://localhost:8080/api/chat/{userId}/{role}` - WebSocket连接地址

## 🔧 配置说明

### application.yml 主要配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  profiles:
    active: local  # 激活本地环境配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      port: 6379
      database: 2

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDelete
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

## 🏗️ 架构设计

### 分层架构
- **Controller层**: 处理HTTP请求，参数校验
- **Service层**: 业务逻辑处理
- **Mapper层**: 数据访问，MyBatis-Plus封装
- **Model层**: 数据模型（实体类、DTO、VO）

### 异常处理
- 全局异常处理器统一处理异常
- 自定义业务异常 `BusinessException`
- 统一错误码 `ErrorCode`

### 会话管理
- 基于Spring Session + Redis实现分布式Session
- Session中存储用户登录状态

### WebSocket实现
- 使用Jakarta WebSocket实现实时通信
- 通过 `SpringContextHolder` 解决WebSocket端点无法注入Spring Bean的问题
- 消息格式：`接收者ID|消息内容`

## 📋 开发规范

### 代码规范
- 使用Lombok简化代码
- 统一使用 `BaseResponse` 作为响应格式
- 使用 `@Valid` 进行参数校验
- 使用逻辑删除，不物理删除数据

### 命名规范
- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- Mapper: `XxxMapper`
- Entity: 实体类名（如 `User`, `Recruitment`）
- DTO: `XxxRequest` / `XxxVO`

## 🐛 常见问题

### 1. WebSocket连接失败
- 检查用户ID和角色参数是否正确
- 确认WebSocket配置是否正确

### 2. Session失效
- 检查Redis连接是否正常
- 确认Session超时时间配置

### 3. 数据库连接失败
- 检查 `application-local.yml` 配置
- 确认数据库服务是否启动

## 📄 许可证

本项目采用 MIT 许可证。

## 👥 贡献者

- xiabiqing - 2632493933@qq.com

## 📮 联系方式

如有问题或建议，请通过以下方式联系：
- Email: 2632493933@qq.com
- GitHub: https://github.com/xiabiqing

---

**注意**: 本项目仅供学习交流使用，请勿用于商业用途。

