# AI Doctor 3.0 项目功能说明

## 一、用户管理

| 功能 | 说明 |
|------|------|
| 注册/登录 | 用户名+密码注册登录，JWT Token 鉴权 |
| 个人信息 | 编辑昵称、性别、年龄、身高、体重、血型、身份证号、联系电话、紧急联系人、邮箱 |
| 头像上传 | 支持上传自定义头像 |
| 修改密码 | 旧密码验证后修改新密码 |
| 注销账号 | 删除个人账户及关联数据 |

## 二、AI 智能问诊（Chat）

| 功能 | 说明 |
|------|------|
| 流式对话 | SSE 实时流式返回 AI 回复，2分钟超时 |
| 多轮记忆 | 基于 ChatMemory 实现对话上下文记忆（最近50轮） |
| 患者个性化 | 自动注入用户身高、体重、年龄、性别、血型等信息 |
| 联网搜索 | Tavily API 实时联网搜索补充医学信息 |
| 疾病分析 | 根据症状分析病因、鉴别诊断、就医建议 |
| 用药指导 | 药物作用机制、用法用量、副作用、禁忌症说明 |
| 检验解读 | 解读血常规、生化、影像等检查报告 |
| 健康管理 | 饮食营养、运动康复、心理健康、慢病管理建议 |
| 医学科普 | 通俗解释医学概念和前沿进展 |
| 多话题聊天 | 支持医疗之外的日常交流 |

## 三、仪表盘（Dashboard）

| 功能 | 说明 |
|------|------|
| AI 健康评分 | 根据健康数据由 AI 生成 0-100 综合评分及标签 |
| AI 每日建议 | 结合实时天气+用户个人信息生成每日健康建议 |
| 地理位置 | 自动获取用户地理位置用于天气查询 |

## 四、健康数据管理

| 功能 | 说明 |
|------|------|
| 生命体征录入 | 体温、心率、血压、呼吸频率 |
| 血常规录入 | 红细胞、血红蛋白、白细胞、血小板 |
| 血糖录入 | 空腹血糖、餐后血糖 |
| 血脂录入 | 总胆固醇、甘油三酯、HDL-C、LDL-C |
| AI 分析报告 | 自动生成生命体征分析、血常规分析、血糖分析、血脂分析、综合健康报告 |
| 数据预览 | 不保存即可预览 AI 分析结果 |
| 数据 CRUD | 查询、保存、删除历史健康数据 |

## 五、健康计划管理

| 功能 | 说明 |
|------|------|
| 计划 CRUD | 新建、编辑、查询、删除健康计划 |
| AI 自动生成 | 根据用户健康目标自动生成健康计划 |
| 计划字段 | 计划名称、目标、内容、起止日期、周期、状态 |
| 邮箱提醒 | 可开启/关闭邮箱提醒，设置提醒时间 |

## 六、用药计划管理

| 功能 | 说明 |
|------|------|
| 用药 CRUD | 新建、编辑、查询、删除用药计划 |
| 信息字段 | 药品名称、用途、每次用量、服药时间点 |

## 七、症状记录管理

| 功能 | 说明 |
|------|------|
| 症状记录 | 记录症状描述并自动生成 AI 分析 |
| 记录管理 | 查询、删除历史症状记录 |

## 八、聊天历史管理

| 功能 | 说明 |
|------|------|
| 会话保存 | 自动保存对话历史（含标题和完整消息数组） |
| 历史查看 | 查看、恢复历史会话 |
| 历史管理 | 更新标题、删除历史记录 |

## 九、定时邮件提醒

| 功能 | 说明 |
|------|------|
| 健康计划提醒 | 每秒轮询，到达设定时间自动发送 HTML 邮件 |
| 防重复 | 同一计划同一天不重复提醒 |
| 跨天重置 | 每天零点自动清理提醒记录 |
| 内容展示 | 包含计划名称、目标、内容、AI建议、周期、状态 |

## 十、API 设置管理

| 功能 | 说明 |
|------|------|
| DeepSeek Key | 配置 DeepSeek API 密钥 |
| Tavily Key | 配置 Tavily 联网搜索 API 密钥 |

---

## 项目目录结构

```
ai-doctor3.0/
├── ai-doctor3.0-db-init.sql          # 数据库初始化脚本（7张表）
├── ai-doctor-ui/                     # 前端 Vue 3 项目
│   └── src/
│       ├── api/                      # 接口层
│       │   ├── dashboard.js          # 仪表盘 API
│       │   ├── history.js            # 聊天历史 API
│       │   ├── medication.js         # 用药计划 API
│       │   ├── symptom.js            # 症状记录 API
│       │   └── user.js               # 用户 API
│       ├── components/               # 公共组件
│       │   ├── AiAdviceDisplay.vue   # AI 建议展示组件
│       │   └── ChatDoctor.vue        # AI 问诊对话组件
│       ├── views/                    # 页面视图
│       │   ├── Login.vue             # 登录/注册
│       │   ├── Main.vue              # 主布局框架
│       │   ├── Dashboard.vue         # 仪表盘首页
│       │   ├── Chat.vue              # AI 问诊页
│       │   ├── ChatHistory.vue       # 聊天历史
│       │   ├── HealthData.vue        # 健康数据录入+报告
│       │   ├── HealthPlan.vue        # 健康计划管理
│       │   ├── Reminder.vue          # 用药提醒
│       │   ├── SymptomRecord.vue     # 症状记录
│       │   ├── Settings.vue          # API 密钥设置
│       │   └── UserProfile.vue       # 个人信息/资料完善
│       ├── router/index.js           # 路由配置
│       ├── utils/                    # 工具函数
│       │   ├── request.js            # Axios 封装（拦截器）
│       │   ├── eventBus.js           # 事件总线
│       │   └── aiAdviceParser.js     # AI 建议解析器
│       ├── App.vue                   # 根组件
│       └── main.js                   # 入口文件
│
├── chat-aidoctor/                    # 后端 Spring Boot 项目
│   └── src/main/java/com/first/
│       ├── AiDoctorApplication.java  # 启动类（启用定时任务）
│       ├── config/                   # 配置类
│       │   ├── AiConfig.java         # AI 模型配置（DeepSeek/OpenAI）
│       │   └── WebConfig.java        # Web 跨域/拦截器配置
│       ├── controller/               # 控制器层（10个）
│       │   ├── UserController.java           # 用户注册/登录/资料/头像
│       │   ├── ChatController.java           # AI 对话（SSE 流式）
│       │   ├── DashboardController.java      # 健康评分 + 每日建议
│       │   ├── HealthDataController.java     # 健康数据 + AI 报告
│       │   ├── HealthPlanController.java     # 健康计划 + AI 生成
│       │   ├── MedicationPlanController.java # 用药计划 CRUD
│       │   ├── SymptomRecordController.java  # 症状记录
│       │   ├── ChatHistoryController.java    # 聊天历史
│       │   ├── ApiSettingsController.java    # API 密钥管理
│       │   └── SseEmitterUTF8.java           # UTF-8 编码 SSE 工具
│       ├── service/                  # 服务接口层（10个）
│       │   ├── UserService.java
│       │   ├── ChatService.java              # OpenAI 对话
│       │   ├── ChatMemoryService.java        # 带记忆流式对话
│       │   ├── ChatHistoryService.java
│       │   ├── HealthDataService.java
│       │   ├── HealthPlanService.java
│       │   ├── MedicationPlanService.java
│       │   ├── SymptomRecordService.java
│       │   ├── MailReminderService.java      # 邮件提醒接口
│       │   ├── TavilySearchService.java      # Tavily 联网搜索
│       │   ├── ApiSettingsService.java
│       │   └── impl/                         # 服务实现（8个）
│       ├── mapper/                   # MyBatis Mapper 层（7个）
│       ├── pojo/                     # 实体类（8个）
│       │   ├── User.java, HealthData.java, HealthPlan.java
│       │   ├── MedicationPlan.java, SymptomRecord.java
│       │   ├── ChatHistory.java, ApiSettings.java, Result.java
│       ├── utils/                    # 工具类（5个）
│       │   ├── JwtUtil.java                  # JWT 生成/解析
│       │   ├── Md5Util.java                  # MD5 加密
│       │   ├── ThreadLocalUtil.java          # 用户上下文
│       │   ├── LuckyColaMailUtil.java        # SMTP 邮件发送
│       │   └── XunfeiSparkWebSocketClient.java # 讯飞星火备用
│       ├── interceptors/             # 拦截器
│       │   └── Logininterceptor.java         # JWT 登录拦截
│       └── exception/                # 异常处理
│           └── GlobalExceptionHandler.java   # 全局异常捕获
│
└── uploads/                          # 用户头像上传目录
```
