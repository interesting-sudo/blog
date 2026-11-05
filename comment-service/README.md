# Comment Service - 评论服务

## 功能概述

评论服务负责博客系统的评论管理功能，包括评论的增删改查、点赞、楼中楼回复等。

## 核心功能

1. **发表评论** - 用户可以对博客发表评论
2. **回复评论** - 支持楼中楼回复功能
3. **删除评论** - 用户和管理员可以删除评论
4. **点赞评论** - 支持评论点赞功能
5. **分页查询** - 支持按博客分页查询评论
6. **评论树** - 以树形结构展示评论及回复

## API 接口

### 1. 添加评论
```
POST /comment/add
Content-Type: application/json

{
  "blogId": 1,
  "userId": 1,
  "parentId": 0,  // 0表示顶级评论，否则为父评论ID
  "content": "这是一条评论"
}
```

### 2. 删除评论
```
DELETE /comment/delete/{id}
```

### 3. 点赞评论
```
POST /comment/like/{id}
```

### 4. 取消点赞评论
```
POST /comment/unlike/{id}
```

### 5. 分页查询评论
```
GET /comment/page?blogId=1&pageNum=1&pageSize=10
```

### 6. 获取评论树
```
GET /comment/tree/{blogId}
```

## 数据库表

### sys_comment (评论表)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| blog_id | BIGINT | 博客ID |
| user_id | BIGINT | 评论用户ID |
| user_name | VARCHAR(50) | 用户昵称(冗余) |
| user_avatar | VARCHAR(255) | 用户头像(冗余) |
| parent_id | BIGINT | 父评论ID(0表示顶级评论) |
| content | TEXT | 评论内容 |
| like_count | BIGINT | 点赞数 |
| status | TINYINT | 状态:0-禁用,1-启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 项目结构

```
comment-service/
├── src/main/java/com/example/dzl/
│   ├── CommentApplication.java          # 启动类
│   ├── config/
│   │   ├── FeignConfig.java             # Feign配置
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   └── MybatisPlusConfig.java       # MyBatis Plus配置
│   ├── controller/
│   │   └── CommentController.java      # 评论控制器
│   ├── enums/
│   │   └── CommentErrorEnum.java        # 错误枚举
│   ├── exception/
│   │   └── BusinessException.java        # 业务异常
│   ├── feign/
│   │   └── UserServiceClient.java       # 用户服务Feign客户端
│   ├── mapper/
│   │   └── CommentMapper.java           # 评论Mapper
│   ├── model/
│   │   ├── dto/
│   │   │   └── AddCommentDTO.java       # 添加评论DTO
│   │   ├── entity/
│   │   │   └── Comment.java             # 评论实体
│   │   ├── req/
│   │   │   └── CommentPageReq.java      # 分页查询请求
│   │   └── vo/
│   │       ├── CommentVO.java           # 评论视图对象
│   │       └── UserInfoVO.java           # 用户信息视图对象
│   ├── service/
│   │   ├── CommentService.java          # 评论服务接口
│   │   └── impl/
│   │       └── CommentServiceImpl.java  # 评论服务实现
│   └── utils/
│       ├── PageResult.java              # 分页结果
│       └── Result.java                  # 统一返回结果
└── src/main/resources/
    ├── application.yml                   # 应用配置
    └── schema.sql                       # 数据库建表脚本
```

## 使用说明

1. 启动 Nacos 服务注册中心
2. 启动 user-service (评论服务依赖用户服务)
3. 执行 `schema.sql` 创建数据库表
4. 配置 `application.yml` 中的数据库连接信息
5. 运行 `CommentApplication.java` 启动评论服务

## 注意事项

1. 评论服务通过 Feign 调用 user-service 获取用户信息
2. 用户昵称和头像在评论时冗余存储，提高查询效率
3. 支持楼中楼回复，parent_id 为 0 表示顶级评论
4. 状态字段支持软删除，status=0 表示禁用/已删除
