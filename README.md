# Blog System · 微服务博客系统（后端）

> 🌐 **在线演示**：https://interesting-sudo.github.io/blog-web/
> 前端演示模式构建，无需后端即可浏览首页、文章详情、评论等页面。
> 配套前端仓库：[blog-web](https://github.com/interesting-sudo/blog-web)

基于 **Spring Boot 2.7 + Spring Cloud Alibaba** 的博客系统后端，采用微服务架构拆分出 7 个独立服务，通过 Nacos 做注册发现、Spring Cloud Gateway 做统一入口，并集成 JWT 鉴权、RocketMQ 异步事件、Elasticsearch 全文检索、Seata 分布式事务与通义千问 AI 能力。

## 架构

```
                        ┌──────────────┐
   Vue3 前端 :5173 ───▶ │  Gateway     │  :8080   JWT 校验 / 路由 / 内部鉴权
                        └──────┬───────┘
             ┌─────────┬───────┼─────────┬──────────┬──────────┐
             ▼         ▼       ▼         ▼          ▼          ▼
          user      blog    comment  document   search       ai
         :8010     :8020    :8030     :8040     :8070      :8060
             │         │       │         │          │          │
        MySQL     MySQL   MySQL    OSS/上传   Elasticsearch  通义千问
             └─────────┴───────┴─────────┴──────────┴──────────┘
                          Nacos :8848   RocketMQ :9876   Seata :8091   Redis :6379
```

## 模块说明

| 模块 | 端口 | 职责 |
|---|---|---|
| `gateway-service` | 8080 | 统一入口，JWT 校验、路由转发、注入内部认证头 |
| `user-service` | 8010 | 注册登录、JWT / RefreshToken、邮箱验证码、用户资料 |
| `blog-service` | 8020 | 文章 CRUD、分类标签、点赞收藏、个人主页统计 |
| `comment-service` | 8030 | 评论与回复 |
| `document-service` | 8040 | 文件上传（阿里云 OSS）、文档转换 |
| `search-service` | 8070 | Elasticsearch 全文检索，消费 MQ 同步索引 |
| `ai-service` | 8060 | 基于通义千问（DashScope）的 AI 摘要 / 问答 |
| `common` | - | `common-core` 通用工具、`common-starter` 内部鉴权与自动装配 |

## 技术栈

- Spring Boot 2.7.6 / Spring Cloud 2021.0.5 / Spring Cloud Alibaba 2021.0.5.0
- Nacos（注册中心）、Spring Cloud Gateway、OpenFeign + LoadBalancer
- MyBatis-Plus + MySQL 8、Redis（Jedis 连接池）
- RocketMQ 4.9（用户资料变更事件 → 搜索索引同步）
- Elasticsearch 7.17、Seata 1.6（AT 模式）、Sentinel
- JJWT、Lombok、Java 17

## 本地运行

### 1. 准备中间件

| 组件 | 端口 | 说明 |
|---|---|---|
| MySQL 8 | 3306 | 需建库：`bs_user_db` / `bs_blog_db` / `bs_comment_db` |
| Redis | 6379 | 默认密码 `123456` |
| Nacos | 8848 | 单机模式 `startup.cmd -m standalone` |
| RocketMQ | 9876 / 10911 | 先起 NameServer 再起 Broker |
| Elasticsearch | 9200 | 7.x |
| Seata | 8091 | 可选，用到分布式事务时启动 |

### 2. 配置密钥

配置文件中所有敏感项都通过环境变量注入，仓库内不含任何真实凭据：

```bash
cp config/secrets.example.cmd config/secrets.local.cmd
# 然后填入 AI_DASHSCOPE_API_KEY / ALIYUN_OSS_ACCESS_KEY_ID
#         ALIYUN_OSS_ACCESS_KEY_SECRET / MAIL_AUTH_CODE
```

| 环境变量 | 用途 | 默认值 |
|---|---|---|
| `AI_DASHSCOPE_API_KEY` | 通义千问 API Key | 空 |
| `ALIYUN_OSS_ACCESS_KEY_ID` / `_SECRET` | 阿里云 OSS 上传 | 空 |
| `MAIL_AUTH_CODE` | QQ 邮箱 SMTP 授权码 | 空 |
| `JWT_SECRET` | JWT 签名密钥 | 开发默认值 |
| `INTERNAL_AUTH_SECRET` | 服务间内部认证 | 开发默认值 |
| `MYSQL_PASSWORD` / `REDIS_PASSWORD` | 数据库 / 缓存口令 | `root` / `123456` |

### 3. 启动

```bash
mvn clean install -DskipTests     # 首次构建
start-all.cmd                     # 一键启动 7 个服务（Windows）
stop-all.cmd                      # 一键停止
```

> **启动顺序**：RocketMQ 必须在 `blog-service` / `user-service` 之前就绪，否则消费端 Bean 初始化会超时失败。
>
> **注意**：若在 WorkBuddy 等注入了 `SERVER__PORT` 环境变量的宿主中启动，需先清除该变量，否则所有服务会争抢同一端口。

用 IDEA 启动时，请在 Run Configuration 的 Environment variables 中填入上表变量。

## 网关路由

| 路径 | 目标服务 |
|---|---|
| `/api/user/**` | user-service |
| `/api/blog/**` | blog-service |
| `/api/comment/**` | comment-service |
| `/api/document/**` | document-service |
| `/api/search/**` | search-service |
| `/api/ai/**` | ai-service |

除 `/api/user/login`、`/register`、`/auth/refresh` 等白名单外，其余请求都需要在 `Authorization` 头携带 JWT。服务间调用通过 `X-Internal-Auth` 头做二次校验，防止绕过网关直连内部服务。

## 前端

配套前端仓库：[blog-web](https://github.com/interesting-sudo/blog-web)（Vue 3 + Vite）+ 在线演示站点 https://interesting-sudo.github.io/blog-web/
