# OpusNocturne API 接口文档

本文档详细描述了 OpusNocturne 博客系统的后端 API 接口。

> **最后更新**: 2026-03-01
> **版本**: v2.5.0

## 目录

1. [通用说明](#1-通用说明)
2. [幂等 & 并发策略](#2-幂等--并发策略)
3. [认证模块 (Auth)](#3-认证模块-auth)
4. [系统用户管理 (System User)](#4-系统用户管理-system-user)
5. [文章管理 (Blog Article)](#5-文章管理-blog-article)
6. [分类与标签 (Category & Tag)](#6-分类与标签-category--tag)
7. [评论互动 (Comment)](#7-评论互动-comment)
8. [系统角色管理 (System Role)](#8-系统角色管理-system-role)
9. [系统权限管理 (System Permission)](#9-系统权限管理-system-permission)
10. [系统设置 (System Setting)](#10-系统设置-system-setting)
11. [站点统计 (Site Statistics)](#11-站点统计-site-statistics)
12. [友情链接 (Friend Link)](#12-友情链接-friend-link)
13. [多媒体管理 (Media - Admin)](#13-多媒体管理-media---admin)
14. [系统管理 (System - Admin)](#14-系统管理-system---admin)
15. [待实现接口 (Project Roadmap)](#15-待实现接口-project-roadmap)

---

## 1. 通用说明

### 1.1 验证码说明

系统采用数学计算验证码，用于防止暴力破解和恶意刷评。

**验证码类型**：
- `LOGIN`：登录场景验证码
- `COMMENT`：评论场景验证码

**获取验证码流程**：
1. 前端调用 `GET /api/common/captcha/math` 接口获取验证码
2. 后端生成数学算式并返回，同时将答案存储到 Redis
3. 前端展示算式，用户输入答案
4. 提交表单时，将验证码答案和相关标识一起提交
5. 后端验证验证码正确性

**验证码有效期**：5分钟

**安全策略**：
- 验证码答案不存储在前端
- 验证后立即删除 Redis 中的验证码记录，防止重放攻击
- 验证码错误不提示具体原因，防止枚举攻击

### 基础 URL
开发环境：`http://localhost:8080`

### 认证方式
系统采用 JWT 无状态认证，基于 Spring Security + JWT 实现。
- **无需认证**: 
  - 前台展示类接口（所有 `/api/blog/**` 下的 GET 请求）
  - 登录接口：`/api/admin/auth/login`
  - 刷新 Token 接口：`/api/admin/auth/refresh`
  - 静态资源：`/uploads/**`, `/static/**`, `/favicon.ico`
  - 文档资源：`/doc.html`, `/webjars/**`, `/v3/api-docs/**`, `/swagger-resources/**`, `/swagger-ui/**`
- **必须认证**: 
  - 前台交互类接口：发表评论 (`/api/blog/comment`)、申请友链 (`/api/blog/friend-link`) 的 POST 请求
  - 所有后台管理接口：以 `/api/admin/**` 开头的接口（登录和刷新 Token 除外）

请在 HTTP 请求头中携带 Token：
```http
Authorization: Bearer <Your-Token>
```

### 安全策略系统

#### Token 管理
- **Token 格式**: JWT (JSON Web Token)，使用 HS256 算法签名
- **Token 有效期**: 由配置文件定义（默认 15 分钟）
- **Token 存储**: 服务端将 Token 存储在 Redis 中，用于验证 Token 有效性
- **Token 验证流程**: 
  1. 从请求头获取 Token
  2. 移除 Bearer 前缀
  3. 解析并验证 Token 签名
  4. 检查 Redis 中是否存在该 Token
  5. 验证 Token 是否与 Redis 中存储的一致

#### 多端登录控制
- **单设备登录**: 同一账号在新设备登录时，旧设备的 Token 会被失效
- **Token 失效机制**: 新登录时会更新 Redis 中存储的 Token，旧 Token 会被视为无效
- **被踢出响应**: 
  ```json
  {
    "code": 2007,
    "message": "您的账号已在其他设备登录，如非本人操作请及时修改密码",
    "data": null
  }
  ```

#### 审计日志
- **记录范围**: 登录、退出、修改密码、权限变更等敏感操作
- **记录内容**: 操作人、操作时间、操作类型、操作结果、IP 地址、请求路径等
- **实现方式**: 通过 AOP 切面 `LogAspect` 实现，自动记录敏感操作

### HTTP 状态码策略

| HTTP 状态码 | 含义 | 适用场景 |
|:---:|:---|:---|
| **200** | 业务成功 | 所有成功响应（包括带数据和无数据） |
| **400** | 请求参数非法 | 缺少字段、类型错误、自定义校验失败 |
| **401** | 未认证 | 未携带 Token、Token 过期、Token 无效、被踢出 |
| **403** | 权限不足 | 已认证但无权限执行操作 |
| **404** | 资源不存在 | 访问的资源不存在 |
| **405** | 方法不允许 | 请求方法与接口不匹配 |
| **429** | 请求过于频繁 | 触发接口限流 |

> ⚠️ **注意**：系统所有接口统一返回 HTTP 200 状态码，业务错误通过响应体中的 `code` 字段区分。

### 统一响应结构
所有接口均返回统一的 JSON 格式，基于 `Result` 类设计：

**成功（带数据）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... }
}
```

**成功（无数据）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

### 错误码一览表

| 错误码 | 枚举名 | 提示信息 | 使用场景 |
|:---:|:---|:---|:---|
| **0** | SUCCESS | 操作成功 | 所有成功响应 |
| **1000** | SYSTEM_ERROR | 系统繁忙，请稍后再试 | 未预期的系统异常 |
| **1001** | PARAM_ERROR | 请求参数有误，请检查后重试 | 参数校验失败 |
| **1002** | NOT_FOUND | 您访问的内容不存在 | 404 资源不存在 |
| **1003** | OPERATION_FAILED | 操作失败，请稍后再试 | 操作执行失败 |
| **1004** | METHOD_NOT_ALLOWED | 不支持该请求方式 | 405 请求方法错误 |
| **1005** | TOO_MANY_REQUESTS | 请求过于频繁，请稍后再试 | 限流拦截 |
| **1006** | DATA_ALREADY_EXISTS | 数据已存在，请勿重复操作 | 唯一约束冲突 |
| **2001** | UNAUTHORIZED | 请先登录后再操作 | 未登录或 Token 无效 |
| **2003** | FORBIDDEN | 抱歉，您没有权限执行此操作 | 权限不足 |
| **2004** | LOGIN_FAILED | 用户名或密码错误，请重新输入 | 登录失败 |
| **2005** | TOKEN_EXPIRED | 登录已过期，请重新登录 | Token 过期 |
| **2006** | TOKEN_INVALID | 登录凭证无效，请重新登录 | Token 解析失败 |
| **2007** | TOKEN_REPLACED | 您的账号已在其他设备登录，如非本人操作请及时修改密码 | 多端登录被顶替 |
| **3001** | USER_NOT_FOUND | 用户不存在 | 用户查询失败 |
| **3002** | USER_DISABLED | 该账号已被禁用，请联系管理员 | 账号被禁 |
| **3003** | USER_EXISTS | 该用户名已被注册 | 用户名重复 |
| **3004** | PASSWORD_ERROR | 密码错误 | 密码校验失败 |
| **3005** | OLD_PASSWORD_ERROR | 原密码不正确，请重新输入 | 修改密码校验 |
| **3006** | PASSWORD_NOT_MATCH | 两次输入的密码不一致，请重新输入 | 密码确认校验 |
| **4001** | ROLE_NOT_FOUND | 角色不存在 | 角色查询失败 |
| **4002** | ROLE_EXISTS | 该角色名称已存在 | 角色名重复 |
| **4003** | PERMISSION_NOT_FOUND | 权限不存在 | 权限查询失败 |
| **5001** | ARTICLE_NOT_FOUND | 文章不存在或已被删除 | 文章查询失败 |
| **5002** | CATEGORY_NOT_FOUND | 分类不存在 | 分类查询失败 |
| **5003** | TAG_NOT_FOUND | 标签不存在 | 标签查询失败 |
| **5004** | CATEGORY_EXISTS | 该分类名称已存在 | 分类名重复 |
| **5005** | TAG_EXISTS | 该标签名称已存在 | 标签名重复 |
| **5006** | CATEGORY_HAS_ARTICLES | 该分类下还有文章，无法删除 | 删除分类时存在关联文章 |
| **5007** | ARTICLE_CREATE_FAILED | 文章创建失败，请稍后再试 | 文章创建操作失败 |
| **5008** | ARTICLE_DELETE_EMPTY | 请选择要删除的文章 | 批量删除文章时未选择文章 |
| **5009** | CATEGORY_DELETE_EMPTY | 请选择要删除的分类 | 批量删除分类时未选择分类 |
| **5010** | TAG_DELETE_EMPTY | 请选择要删除的标签 | 批量删除标签时未选择标签 |
| **6001** | COMMENT_NOT_FOUND | 评论不存在或已被删除 | 评论查询失败 |
| **6002** | COMMENT_AUDIT_FAILED | 评论审核失败 | 评论审核操作失败 |
| **6003** | COMMENT_CONTENT_EMPTY | 评论内容不能为空 | 评论校验 |
| **6004** | COMMENT_DELETE_EMPTY | 请选择要删除的评论 | 批量删除评论时未选择评论 |
| **6005** | COMMENT_AUDIT_EMPTY | 请选择要审核的评论 | 批量审核评论时未选择评论 |
| **7001** | FILE_UPLOAD_FAILED | 文件上传失败，请稍后再试 | 文件上传异常 |
| **7002** | FILE_TYPE_ERROR | 不支持该文件格式，请上传正确的文件类型 | 文件类型校验 |
| **7003** | FILE_SIZE_EXCEEDED | 文件大小超出限制，请压缩后重试 | 文件过大 |
| **7004** | FILE_NOT_FOUND | 文件不存在 | 文件查询失败 |
| **7005** | FILE_PARTIAL_NOT_FOUND | 部分附件不存在，请刷新后重试 | 附件查询失败 |
| **8001** | FRIEND_LINK_NOT_FOUND | 该友链不存在 | 友链查询失败 |
| **8002** | FRIEND_LINK_ALREADY_APPLIED | 该链接已申请过,请勿重复申请 | 友链申请重复 |
| **9001** | CAPTCHA_INVALID | 验证码错误 | 验证码验证失败 |

### 分页规则
分页查询接口使用统一的分页参数，基于 `BasePageQueryDTO` 类：

**查询参数**：
- `current`: 当前页码，默认 1
- `size`: 每页条数，默认 10
- `keyword`: 可选，搜索关键词（如有）

**分页响应数据结构**：
```json
{
  "records": [ ... ],  // 数据列表
  "total": 100,        // 总记录数
  "size": 10,          // 每页条数
  "current": 1,        // 当前页码
  "pages": 10          // 总页数
}
```

### 时间格式约定

所有时间字段统一使用 **Jackson 全局配置** 的格式，时区为 `Asia/Shanghai`：

| 类型 | 格式 | 示例 |
|:---|:---|:---|
| 日期时间 | `yyyy-MM-dd HH:mm:ss` | `2026-02-20 10:30:00` |
| 日期 | `yyyy-MM-dd` | `2026-02-20` |
| 时间 | `HH:mm:ss` | `10:30:00` |

> ⚠️ **注意**：前端发送的时间参数请同样使用上述格式，避免因格式差异导致解析失败。
> 
> 实现方式：通过 `JacksonConfig` 配置类统一设置 Java 8 时间类型的序列化和反序列化格式。

### 空值处理约定

| 场景 | 处理方式 |
|:---|:---|
| 响应中的 `null` 字段 | 字段保留，值为 `null`。前端应做相应的空值判断 |
| 请求中的可选字段 | 不传或传 `null` 均表示不修改/不筛选 |
| 空字符串 `""` | 视为有效值（非 `null`），可能触发校验 |
| 分页默认值 | `current` 不传默认为 `1`，`size` 不传默认为 `10` |

---

## 1.2 验证码接口

### 1.2.1 获取数学计算验证码

- **接口路径**: `GET /api/common/captcha/math`
- **是否认证**: 否
- **功能说明**: 获取数学计算验证码，用于登录和评论场景

**查询参数**

| 名称 | 类型 | 必填 | 说明 | 示例 |
|:---|:---|:---|:---|:---|
| type | string | 是 | 验证码类型：`LOGIN` 或 `COMMENT` | `LOGIN` |
| uuid | string | 否 | 登录场景下的临时标识，由前端生成 | `uuid123456` |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "question": "8 × 5 = ?",
    "captchaKey": "captcha:login:127.0.0.1:uuid123456"
  }
}
```

**失败响应**
- **参数错误**
```json
{
  "code": 1001,
  "message": "登录场景下必须提供uuid",
  "data": null
}
```

---

## 2. 幂等 & 并发策略

### 2.1 幂等矩阵表

| HTTP Method | 天然幂等 | 说明 |
|:---:|:---:|:---|
| **GET** | ✓ | 天然幂等，多次调用返回相同结果 |
| **POST** | ✗ | 非幂等，多次调用可能创建重复资源 |
| **PUT** | ✓ | 天然幂等，多次调用最终状态一致 |
| **DELETE** | ✓ | 天然幂等，多次调用最终状态一致 |

### 2.2 接口限流策略

- **实现方式**: 基于 Redis 的滑动窗口限流
- **适用场景**: 登录、注册、修改密码、评论、友链申请等敏感接口
- **配置**: 通过 `@RateLimit` 注解设置限流规则
- **示例**: 
  ```java
  @RateLimit(maxCount = 5, message = "登录尝试过于频繁，请稍后再试")
  @PostMapping("/login")
  public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
      // 登录逻辑
  }
  ```

### 2.3 安全防护

#### XSS 防护
- **实现方式**: 自定义 `XssFilter` 过滤器
- **处理逻辑**: 过滤请求参数中的恶意脚本

#### CSRF 防护
- **实现方式**: 禁用 CSRF（使用 JWT 无需 CSRF 保护）
- **配置**: 在 `SecurityConfig` 中禁用 CSRF

#### SQL 注入防护
- **实现方式**: 使用 MyBatis-Plus 的参数绑定
- **处理逻辑**: 自动转义 SQL 参数



---

## 3. 认证模块 (Auth)

### 3.1 用户登录

- **接口路径**: `POST /api/admin/auth/login`
- **是否认证**: 否
- **HTTP 状态码**: 200 (成功), 401 (认证失败), 429 (限流)
- **审计日志**: 本接口会产生审计日志

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|:---|:---|:---|:---|:---|
| username | string | 是 | 用户名 | `admin` |
| password | string | 是 | 密码 | `admin123` |
| answer | string | 是 | 验证码答案 | `40` |
| captchaKey | string | 是 | 验证码key，从获取验证码接口返回 | `captcha:login:127.0.0.1:uuid123456` |

**前端调用示例**
```javascript
// 1. 获取验证码
axios.get('/api/common/captcha/math', {
  params: {
    type: 'LOGIN',
    uuid: 'uuid123456'
  }
}).then(captchaResponse => {
  const question = captchaResponse.data.data.question;
  const captchaKey = captchaResponse.data.data.captchaKey;
  
  // 2. 显示验证码问题，用户输入答案
  const answer = prompt(question);
  
  // 3. 登录
  return axios.post('/api/admin/auth/login', {
    username: 'admin',
    password: 'admin123',
    answer: answer,
    captchaKey: captchaKey
  });
}).then(loginResponse => {
  const token = loginResponse.data.data.token;
  localStorage.setItem('token', token);
});
```

**后端处理逻辑**：
1. 验证验证码正确性
2. 验证用户名和密码
3. 检查用户状态
4. 生成JWT Token
5. 更新用户最后登录时间
6. 返回登录结果

**成功响应（200）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

**错误响应（401）**
```json
{
  "code": 2004,
  "message": "用户名或密码错误，请重新输入",
  "data": null
}
```

**错误响应（429）**
```json
{
  "code": 1005,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

---

### 3.2 退出登录

- **接口路径**: `POST /api/admin/auth/logout`
- **是否认证**: 是
- **HTTP 状态码**: 204 (成功), 401 (认证失败)

**成功响应（204）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**错误响应（401）**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

---

### 3.3 获取当前用户信息

- **接口路径**: `GET /api/admin/auth/info`
- **是否认证**: 是
- **HTTP 状态码**: 200 (成功), 401 (认证失败)

**成功响应（200）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "...",
    "email": "...",
    "status": 1,
    "roleIds": [1],
    "createTime": "2026-01-01 12:00:00",
    "permissions": ["article:create", "article:update"]
  }
}
```

**错误响应（401）**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

---

### 3.4 刷新 Token

- **接口路径**: `POST /api/admin/auth/refresh`
- **是否认证**: 是
- **HTTP 状态码**: 200 (成功), 401 (认证失败)

**成功响应（200）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenHead": "Bearer "
  }
}
```

**错误响应（401）**
```json
{
  "code": 2005,
  "message": "登录已过期，请重新登录",
  "data": null
}
```

---

### 3.5 修改密码

- **接口路径**: `PUT /api/admin/auth/change-password`
- **是否认证**: 是
- **HTTP 状态码**: 200 (成功), 400 (参数校验失败), 401 (认证失败), 422 (业务校验失败)
- **审计日志**: 本接口会产生审计日志

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 | 校验规则 |
|:---|:---|:---|:---|:---|
| oldPassword | string | 是 | 旧密码 | 不能为空 |
| newPassword | string | 是 | 新密码 | 不能为空，长度 6-20 位 |
| confirmPassword | string | 是 | 确认新密码 | 不能为空，需与 newPassword 一致 |

**前端调用示例**
```javascript
axios.put('/api/admin/auth/change-password', {
  oldPassword: 'oldPass123',
  newPassword: 'newPass456',
  confirmPassword: 'newPass456'
});
```

**成功响应（200）**
```json
{
  "code": 0,
  "message": "密码修改成功，请重新登录",
  "data": null
}
```

> ⚠️ **注意**：密码修改成功后，服务端会自动注销当前用户的 Token，前端需引导用户重新登录。

**错误响应 — 两次密码不一致（422）**
```json
{
  "code": 3004,
  "message": "两次输入的密码不一致",
  "data": null
}
```

**错误响应 — 旧密码错误（422）**
```json
{
  "code": 3005,
  "message": "原密码不正确，请重新输入",
  "data": null
}
```

**错误响应 — 用户不存在（404）**
```json
{
  "code": 3001,
  "message": "用户不存在",
  "data": null
}
```

**错误响应 — 参数校验失败（400）**
```json
{
  "code": 1001,
  "message": "密码长度必须在6-20位之间",
  "data": null
}
```

---

## 4. 系统用户管理 (System User)

### 4.1 分页获取用户列表

- **接口路径**: `GET /api/admin/user/page`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| username | string | 否 | `admin` | 用户名搜索 |
| nickname | string | 否 | `管理员` | 昵称搜索 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "超级管理员",
        "avatar": "http://...",
        "email": "admin@example.com",
        "status": 1,
        "createTime": "2026-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 4.2 创建用户

- **接口路径**: `POST /api/admin/user`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| username | string | 是 | 用户名，不可重复 |
| password | string | 是 | 初始密码 |
| nickname | string | 否 | 昵称 |
| email | string | 否 | 邮箱 |
| roleIds | array | 否 | 关联角色ID列表，如 `[1, 2]` |
| status | int | 否 | 状态：1-启用，0-禁用 (默认1) |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 3003,
  "message": "该用户名已被注册",
  "data": null
}
```

---

### 4.3 更新用户

- **接口路径**: `PUT /api/admin/user/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 用户ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| nickname | string | 否 | 昵称 |
| email | string | 否 | 邮箱 |
| roleIds | array | 否 | 关联角色ID列表 |
| status | int | 否 | 状态：1-启用，0-禁用 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 3001,
  "message": "用户不存在",
  "data": null
}
```

---

### 4.4 删除用户

- **接口路径**: `DELETE /api/admin/user/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 用户ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 4.5 获取用户详情

- **接口路径**: `GET /api/admin/user/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 用户ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "http://...",
    "email": "admin@example.com",
    "status": 1,
    "roleIds": [1],
    "createTime": "2026-01-01 12:00:00"
  }
}
```

---

### 4.6 重置用户密码

- **接口路径**: `PUT /api/admin/user/{id}/reset-password`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 用户ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| password | string | 是 | 新密码 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 3001,
  "message": "用户不存在",
  "data": null
}
```

---

## 5. 文章管理 (Blog Article)

### 5.1 创建文章

- **接口路径**: `POST /api/admin/article`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| title | string | 是 | 文章标题 |
| content | string | 是 | 文章内容 (Markdown格式) |
| summary | string | 否 | 摘要 |
| categoryId | long | 是 | 分类ID |
| tagIds | array | 否 | 标签ID列表，如 `[1, 3]` |
| coverImg | string | 否 | 封面图片URL |
| isTop | int | 否 | 是否置顶：1-是，0-否 |
| status | int | 是 | 状态：0-草稿，1-发布，2-下架 |
| slug | string | 否 | URL别名(SEO)，如 `spring-boot-3-practice`。**如果不填写，系统会根据标题自动生成** |
| keywords | string | 否 | SEO关键词，多个关键词用逗号分隔，如 `Spring Boot,Java`。**- 由作者完全手动填写，系统不会自动生成，多个关键词用逗号分隔，如 Spring Boot,Java,JDK21** |

**Markdown图片处理说明**
- 文章中的图片需要先通过 `/api/admin/attachment/upload` 接口上传
- 上传成功后获取返回的图片URL
- 将获取到的URL插入到Markdown内容中，格式为 `![图片描述](图片URL)`
- 直接复制粘贴的图片不会自动上传，需要手动处理

**标签关联说明**
- 通过 `tagIds` 参数管理 `article_tag` 关联关系
- 创建时根据 `tagIds` 生成关联记录
- 更新时会先删除旧关联再重建

**前端调用示例**
```javascript
axios.post('/api/admin/article', {
  title: 'Spring Boot 实战',
  content: '# Hello World\n...',
  categoryId: 1,
  tagIds: [101, 102],
  status: 1
});
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 100,
    "title": "Spring Boot 实战"
  }
}
```

**失败响应**
```json
{
  "code": 1001,
  "message": "标题不能为空",
  "data": null
}
```

---

### 5.2 后台文章列表

- **接口路径**: `GET /api/admin/article/page`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| title | string | 否 | `Spring` | 文章标题搜索 |
| categoryId | long | 否 | `1` | 按分类筛选 |
| status | int | 否 | `1` | 按状态筛选 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 100,
        "title": "Spring Boot 实战",
        "summary": "本文介绍...",
        "coverImg": "http://...",
        "viewCount": 120,
        "isTop": 0,
        "status": 1,
        "categoryName": "后端技术",
        "authorNickname": "Admin",
        "publishTime": "2026-02-01 10:00:00",
        "createTime": "2026-01-30 16:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 5.3 更新文章

- **接口路径**: `PUT /api/admin/article/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| title | string | 是 | 文章标题 |
| content | string | 是 | 文章内容 (Markdown格式) |
| summary | string | 否 | 摘要 |
| categoryId | long | 是 | 分类ID |
| tagIds | array | 否 | 标签ID列表 |
| coverImg | string | 否 | 封面图片URL |
| isTop | int | 否 | 是否置顶 |
| status | int | 是 | 状态：0-草稿，1-发布，2-下架 |
| slug | string | 否 | URL别名(SEO)，如 `spring-boot-3-practice`。**如果不填写，系统会根据标题自动生成** |
| keywords | string | 否 | SEO关键词，多个关键词用逗号分隔，如 `Spring Boot,Java`。**由作者手动填写，用于SEO优化** |

**Markdown图片处理说明**
- 文章中的图片需要先通过 `/api/admin/attachment/upload` 接口上传
- 上传成功后获取返回的图片URL
- 将获取到的URL插入到Markdown内容中，格式为 `![图片描述](图片URL)`
- 直接复制粘贴的图片不会自动上传，需要手动处理

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5001,
  "message": "文章不存在或已被删除",
  "data": null
}
```

---

### 5.4 删除文章

- **接口路径**: `DELETE /api/admin/article/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 5.5 批量删除文章

- **接口路径**: `DELETE /api/admin/article/batch-delete`
- **是否认证**: 是
- **审计日志**: 本接口会产生审计日志

**请求体 (JSON)**
直接传递待删除的文章 ID 数组。

| 字段名 | 类型  | 必填 | 说明         |
| ------ | ----- | ---- | ------------ |
| -      | array | 是   | 文章ID列表，如 `[100, 101, 102]` |

**前端调用示例**
```javascript
axios.delete('/api/admin/article/batch-delete', {
  data: [100, 101, 102] // 注意：在 DELETE 请求中，数组需放在 data 属性里
}).then(response => {
  console.log('批量删除成功');
});
```

**成功响应 (204)**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
- **未选择文章**
```json
{
  "code": 1001,
  "message": "请选择要删除的文章",
  "data": null
}
```
- **部分文章不存在或已被删除**
```json
{
  "code": 5001,
  "message": "文章【100, 102】不存在或已被删除",
  "data": null
}
```

---

### 5.6 文章详情 (后台)

- **接口路径**: `GET /api/admin/article/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 100,
    "title": "Spring Boot 实战",
    "content": "# 详细内容...",
    "summary": "本文介绍...",
    "categoryId": 1,
    "categoryName": "后端技术",
    "tagIds": [1, 2, 3],
    "tags": [
      { "id": 1, "name": "Java" },
      { "id": 2, "name": "Spring Boot" }
    ],
    "coverImg": "http://...",
    "isTop": 0,
    "status": 1,
    "publishTime": "2026-02-01 10:00:00",
    "createTime": "2026-01-30 16:00:00",
    "updateTime": "2026-02-01 10:00:00"
  }
}
```

---

### 5.7 文章置顶/取消置顶

- **接口路径**: `PUT /api/admin/article/{id}/top`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| isTop | int | 是 | 是否置顶：1-是，0-否 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 5.8 更新文章状态

- **接口路径**: `PUT /api/admin/article/{id}/status`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| status | int | 是 | 状态：0-草稿，1-发布，2-下架 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 5.9 前台文章列表 (Portal)

- **接口路径**: `GET /api/blog/article/page`
- **是否认证**: 否

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| categoryId | long | 否 | `1` | 按分类筛选 |
| tagId | long | 否 | `5` | 按标签筛选 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 100,
        "title": "Spring Boot 实战",
        "summary": "本文介绍...",
        "coverImg": "http://...",
        "viewCount": 120,
        "likeCount": 50,
        "publishTime": "2026-02-01 10:00:00",
        "categoryName": "后端技术",
        "tags": [
           { "id": 1, "name": "Java" }
        ]
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 5.10 前台文章详情 (Portal)

- **接口路径**: `GET /api/blog/article/{id}`
- **是否认证**: 否

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `100` | 文章ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 100,
    "title": "Spring Boot 实战",
    "content": "# 详细内容...",
    "summary": "本文介绍...",
    "coverImg": "http://...",
    "viewCount": 121,
    "likeCount": 50,
    "publishTime": "2026-02-01 10:00:00",
    "categoryId": 1,
    "categoryName": "后端技术",
    "authorNickname": "Admin",
    "tags": [ ... ],
    "prevArticle": { "id": 99, "title": "上一篇" },
    "nextArticle": { "id": 101, "title": "下一篇" }
  }
}
```

**失败响应**
```json
{
  "code": 5001,
  "message": "文章不存在或已被删除",
  "data": null
}
```

### 5.11 获取文章归档 (Portal)

- **接口路径**: `GET /api/blog/article/archive`
- **是否认证**: 否
- **功能说明**: 将文章按照“年份-月份”进行分组展示，用于归档页面。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "year": "2023",
      "months": [
        {
          "month": "10",
          "count": 5,
          "articles": [
            {
              "id": 1,
              "title": "Spring Boot 3 实战",
              "createTime": "2023-10-01 12:00:00",
              "day": "01"
            }
          ]
        }
      ]
    }
  ]
}
```

### 5.12 文章点赞/取消点赞 (Portal)

- **接口路径**: `POST /api/blog/article/{id}/like`
- **是否认证**: 否（通过 IP 限制重复点赞）

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 文章ID |

**功能说明**：
- 首次点击：执行点赞操作，返回最新点赞数
- 再次点击：执行取消点赞操作，返回最新点赞数
- 系统会通过 IP 地址识别用户，防止重复点赞

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": 122  // 返回最新的点赞数
}
```

---

## 6. 分类与标签 (Category & Tag)

### 6.1 获取全部分类 (Portal)

- **接口路径**: `GET /api/blog/category/list`
- **是否认证**: 否

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "Java",
      "articleCount": 15
    },
    {
      "id": 2,
      "name": "随笔",
      "articleCount": 3
    }
  ]
}
```

---

### 6.2 后台分类管理接口

#### 6.2.1 分页获取分类列表

- **接口路径**: `GET /api/admin/category/list`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| name | string | 否 | `Java` | 分类名称搜索 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "Java",
        "description": "Java相关技术",
        "sort": 0,
        "status": 1,
        "createTime": "2026-01-01 12:00:00",
        "updateTime": "2026-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 6.2.2 创建分类

- **接口路径**: `POST /api/admin/category`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| name | string | 是 | 分类名称 |
| description | string | 否 | 描述 |
| sort | int | 否 | 排序（升序） |
| status | int | 否 | 状态：1-启用，0-禁用 (默认1) |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5004,
  "message": "该分类名称已存在",
  "data": null
}
```

#### 6.2.3 更新分类

- **接口路径**: `PUT /api/admin/category/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 分类ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| name | string | 是 | 分类名称 |
| description | string | 否 | 描述 |
| sort | int | 否 | 排序（升序） |
| status | int | 否 | 状态：1-启用，0-禁用 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5002,
  "message": "分类不存在",
  "data": null
}
```

#### 6.2.4 删除分类

- **接口路径**: `DELETE /api/admin/category/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 分类ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5006,
  "message": "该分类下还有文章，无法删除",
  "data": null
}
```

#### 6.2.5 批量删除分类

- **接口路径**: `DELETE /api/admin/category/batch-delete`
- **是否认证**: 是

**请求体 (JSON)**

直接传递 ID 数组：
```json
[1, 2, 3]
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5006,
  "message": "分类【Java】下存在文章，无法删除",
  "data": null
}
```

---

### 6.3 标签管理接口

#### 6.3.1 获取所有标签 (Portal)

- **接口路径**: `GET /api/blog/tag/list`
- **是否认证**: 否

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "Spring Boot",
      "color": "#1890ff",
      "articleCount": 10
    }
  ]
}
```

#### 6.3.2 分页获取标签列表 (后台)

- **接口路径**: `GET /api/admin/tag/list`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| name | string | 否 | `Spring` | 标签名称搜索 |

#### 6.3.3 创建标签

- **接口路径**: `POST /api/admin/tag`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| name | string | 是 | 标签名称 |
| color | string | 否 | 标签颜色，默认 `#1890ff` |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5005,
  "message": "该标签名称已存在",
  "data": null
}
```

#### 6.3.4 更新标签

- **接口路径**: `PUT /api/admin/tag/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 标签ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| name | string | 是 | 标签名称 |
| color | string | 否 | 标签颜色 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 5003,
  "message": "标签不存在",
  "data": null
}
```

#### 6.3.5 删除标签

- **接口路径**: `DELETE /api/admin/tag/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 标签ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

#### 6.3.6 批量删除标签

- **接口路径**: `DELETE /api/admin/tag/batch-delete`
- **是否认证**: 是

**请求体 (JSON)**

直接传递 ID 数组：
```json
[1, 2, 3]
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

## 7. 评论互动 (Comment)

### 7.1 分页获取文章评论树 (Portal)

- **接口路径**: `GET /api/blog/comment/tree/{articleId}`
- **是否认证**: 否

> 💡 **优化说明（两级分页策略）**：
> 为避免热门文章评论量过大时的 OOM 风险，接口已从"全量返回"改为"分页返回"。
> - **外层分页**：先查当前页的顶级评论（`rootParentId IS NULL`）。
> - **内层批量查**：通过 `IN(rootIds)` 一次查出这些顶级评论下的所有子评论并组装到树中。

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| articleId | `100` | 文章ID。若为 `0` 则获取留言板评论 |

**查询参数**

| 名称 | 类型 | 必填 | 默认值 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 当前页码，从 1 开始 |
| size | int | 否 | `10` | 每页顶级评论条数 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "total": 128,
    "list": [
      {
        "id": 501,
        "nickname": "用户A",
        "content": "写的真好！",
        "createTime": "2026-02-17 10:00:00",
        "replyNickname": null,
        "childCount": 5,
        "children": []
      }
    ]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| total | long | 顶级评论总数，用于前端计算总页数 |
| list | array | 当前页的顶级评论列表，每条内部嵌套其子评论 |
| list[].id | long | 评论ID（仅用于前端技术处理，不建议在界面上展示） |
| list[].nickname | string | 评论人昵称 |
| list[].content | string | 评论内容 |
| list[].createTime | string | 发表时间 |
| list[].replyNickname | string | 被回复人昵称（顶级评论为 `null`） |
| list[].childCount | integer | 子评论总数 |
| list[].children | array | 子评论列表（初始为空，需要通过分页接口加载） |


### 7.2 获取文章评论统计 (Portal)

- **接口路径**: `GET /api/blog/comment/stats/{articleId}`
- **是否认证**: 否

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| articleId | `1` | 文章ID。若为 `0` 则获取留言板的评论统计 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "total": 15
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| total | long | 该文章的评论总数（审核通过的），包括所有父评论和子评论 |

### 7.3 分页获取子评论 (Portal)

- **接口路径**: `GET /api/blog/comment/child/{rootParentId}`
- **是否认证**: 否

> 💡 **分页策略**：
> - 第一次加载：默认显示 3 条子评论
> - 后续加载：每次点击加载 10 条子评论

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| rootParentId | `501` | 顶级评论ID |

**查询参数**

| 名称 | 类型 | 必填 | 默认值 | 说明 |
|:---|:---|:---|:---|:---|
| articleId | long | 是 | - | 文章ID |
| current | int | 否 | `1` | 当前页码，从 1 开始 |
| size | int | 否 | `3` | 每页条数（第一次3条，后续10条） |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 502,
      "nickname": "作者",
      "replyNickname": "用户A",
      "content": "谢谢支持",
      "createTime": "2026-02-17 10:30:00",
      "children": []
    }
  ]
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| id | long | 评论ID（仅用于前端技术处理，不建议在界面上展示） |
| nickname | string | 评论人昵称 |
| content | string | 评论内容 |
| createTime | string | 发表时间 |
| replyNickname | string | 被回复人昵称 |
| children | array | 子评论列表（始终为空，因为子评论已平铺） |

### 7.4 发表评论 / 留言 (Portal)


- **接口路径**: `POST /api/blog/comment`
- **是否认证**: 是

> 💡 **树洞/留言板说明**：
> 当 `articleId` 传 `0` 或 `null` 时，该条内容将被视为**站点留言（树洞）**。
>
> **审核机制**：
> 系统会根据 `系统设置` 中的 `commentAudit` 配置决定评论是否需要审核：
> - **开启审核**（`commentAudit: true`）：评论状态默认为 `0`（待审核），需后台管理员批准后才会在前台显示。
> - **关闭审核**（`commentAudit: false`）：评论状态默认为 `1`（审核通过），提交后直接在前台显示。
>
> **审核状态说明**：
> - `0`：待审核 - 等待管理员处理
> - `1`：审核通过 - 已在前台显示
> - `2`：审核未通过 - 被管理员拒绝，不会显示

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|:---|:---|:---|:---|:---|
| articleId | long | 否 | 文章ID。**传 `0` 代表留言板/树洞** | `100` |
| content | string | 是 | 评论内容 | `非常有深度的文章！` |
| parentId | long | 否 | 父评论ID，回复时必填 | `501` |
| rootParentId | long | 否 | 根评论ID，回复楼中楼时必填 | `501` |
| answer | string | 是 | 验证码答案 | `40` |

**后端处理逻辑**：
1. **身份提取**：后端自动从解析后的 JWT 中提取 `userId`。
2. **验证码验证**：使用 `userId` 构建验证码 Key，验证验证码正确性。
3. **信息关联**：系统根据 `userId` 自动从 `sys_user` 表中查询该用户的 `nickname` 和 `email`。
4. **环境记录**：自动获取请求者的 `ip_address` 和 `user_agent`。

**请求示例**
```http
POST /api/blog/comment
Authorization: Bearer <Access-Token>
Content-Type: application/json

{
  "articleId": 100,
  "content": "写的不错，赞一个！",
  "parentId": null,
  "answer": "40"
}
```

**前端调用示例**
```javascript
// 1. 获取验证码（已登录状态）
axios.get('/api/common/captcha/math', {
  params: {
    type: 'COMMENT'
  },
  headers: {
    Authorization: 'Bearer ' + localStorage.getItem('token')
  }
}).then(captchaResponse => {
  const question = captchaResponse.data.data.question;
  
  // 2. 显示验证码问题，用户输入答案
  const answer = prompt(question);
  
  // 3. 发表评论
  return axios.post('/api/blog/comment', {
    articleId: 100,
    content: '写的不错，赞一个！',
    parentId: null,
    answer: answer
  }, {
    headers: {
      Authorization: 'Bearer ' + localStorage.getItem('token')
    }
  });
}).then(response => {
  console.log('评论发表成功');
});
```

**成功响应 (200)**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**错误响应 - 未登录 (401)**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

---

### 7.5 后台评论管理

> **审核流程说明**：
> 1. **评论提交**：用户提交评论后，系统根据 `commentAudit` 配置自动设置初始状态
> 2. **后台审核**：管理员在后台查看待审核评论，进行审核操作
> 3. **状态更新**：审核通过或拒绝后，评论状态更新，前台根据状态显示或隐藏评论
> 4. **批量操作**：支持批量审核和删除评论，提高管理效率

#### 7.5.1 分页获取评论列表

- **接口路径**: `GET /api/admin/comment/page`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| articleId | long | 否 | `100` | 按文章ID筛选 |
| status | int | 否 | `0` | 按状态筛选：0-待审核，1-审核通过，2-审核未通过 |
| nickname | string | 否 | `用户` | 按昵称搜索 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 501,
        "articleId": 100,
        "articleTitle": "Spring Boot 实战",
        "nickname": "用户A",
        "email": "user@example.com",
        "content": "写的真好！",
        "status": 1,
        "ipAddress": "127.0.0.1",
        "createTime": "2026-02-01 10:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 7.5.2 审核评论

- **接口路径**: `PUT /api/admin/comment/{id}/audit`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `501` | 评论ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| status | int | 是 | 审核状态：1-审核通过，2-审核未通过 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 6001,
  "message": "评论不存在或已被删除",
  "data": null
}
```

#### 7.5.3 删除评论

- **接口路径**: `DELETE /api/admin/comment/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `501` | 评论ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

#### 7.5.4 批量审核评论

- **接口路径**: `PUT /api/admin/comment/batch-audit`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| ids | array | 是 | 评论ID列表，如 `[501, 502]` |
| status | int | 是 | 审核状态：1-审核通过，2-审核未通过 |

**前端调用示例**
```javascript
axios.put('/api/admin/comment/batch-audit', {
  ids: [501, 502, 503],
  status: 1
});
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 1001,
  "message": "请选择要审核的评论",
  "data": null
}
```

#### 7.5.5 批量删除评论

- **接口路径**: `DELETE /api/admin/comment/batch-delete`
- **是否认证**: 是

**请求体 (JSON)**

直接传递 ID 数组：
```json
[501, 502, 503]
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**
```json
{
  "code": 1001,
  "message": "请选择要删除的评论",
  "data": null
}
```

---

## 8. 系统角色管理 (System Role)

### 8.1 获取所有角色

- **接口路径**: `GET /api/admin/role/list`
- **是否认证**: 是

> ⚠️ **注意**：返回全量角色列表（`List`），非分页。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "roleName": "超级管理员",
      "roleCode": "super_admin",
      "description": "拥有系统全部权限",
      "status": 1,
      "createTime": "2026-01-01 12:00:00"
    },
    {
      "id": 2,
      "roleName": "编辑",
      "roleCode": "editor",
      "description": "文章编辑权限",
      "status": 1,
      "createTime": "2026-01-01 12:00:00"
    }
  ]
}
```

---

### 8.2 获取角色详情

- **接口路径**: `GET /api/admin/role/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 角色ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "roleName": "超级管理员",
    "roleCode": "super_admin",
    "description": "拥有系统全部权限",
    "status": 1,
    "createTime": "2026-01-01 12:00:00",
    "updateTime": "2026-01-01 12:00:00"
  }
}
```

---

### 8.3 创建角色

- **接口路径**: `POST /api/admin/role`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| roleName | string | 是 | 角色名称，不可重复 |
| roleCode | string | 是 | 角色编码，不可重复 |
| description | string | 否 | 描述 |
| status | int | 否 | 状态：1-启用，0-禁用 (默认1) |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 8.4 更新角色

- **接口路径**: `PUT /api/admin/role/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 角色ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| roleName | string | 是 | 角色名称 |
| roleCode | string | 是 | 角色编码 |
| description | string | 否 | 描述 |
| status | int | 否 | 状态：1-启用，0-禁用 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 8.5 删除角色

- **接口路径**: `DELETE /api/admin/role/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 角色ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 8.6 分配角色权限

- **接口路径**: `PUT /api/admin/role/{id}/permissions`
- **是否认证**: 是
- **审计日志**: 本接口会产生审计日志

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 角色ID |

**请求体 (JSON)**

直接传递权限 ID 数组：
```json
[1, 2, 101, 102, 201]
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 8.7 获取角色权限ID列表

- **接口路径**: `GET /api/admin/role/{id}/permissions`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 角色ID |

> ⚠️ **注意**：返回该角色拥有的权限 ID 列表（`List<Long>`），前端可配合权限树实现勾选。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [1, 2, 101, 102, 201, 202, 301]
}
```

---

## 9. 系统权限管理 (System Permission)

### 9.1 获取权限列表

- **接口路径**: `GET /api/admin/permission/tree`
- **是否认证**: 是

> ⚠️ **注意**：当前返回平铺的权限列表（`List<SysPermission>`），前端需自行根据 `parentId` 构建树形结构。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "name": "仪表盘",
      "code": "dashboard",
      "type": 1,
      "path": "/dashboard",
      "icon": "DashboardOutlined",
      "sort": 1,
      "status": 1
    },
    {
      "id": 2,
      "parentId": 0,
      "name": "文章管理",
      "code": "article:manage",
      "type": 1,
      "path": "/article",
      "icon": "FileTextOutlined",
      "sort": 2,
      "status": 1
    },
    {
      "id": 101,
      "parentId": 2,
      "name": "查看文章",
      "code": "article:list",
      "type": 2,
      "sort": 1,
      "status": 1
    }
  ]
}
```

---

### 9.2 创建权限

- **接口路径**: `POST /api/admin/permission`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| parentId | long | 是 | 父级ID，0表示顶级 |
| name | string | 是 | 权限名称 |
| code | string | 否 | 权限标识，按钮类型必填 |
| type | int | 是 | 类型：1-菜单，2-按钮 |
| path | string | 否 | 路由地址，菜单类型必填 |
| component | string | 否 | 组件路径，菜单类型必填 |
| icon | string | 否 | 图标 |
| sort | int | 否 | 排序 |
| status | int | 否 | 状态：1-启用，0-禁用 (默认1) |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 9.3 更新权限

- **接口路径**: `PUT /api/admin/permission/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 权限ID |

**请求体 (JSON)**: 同 9.2 创建权限

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

### 9.4 删除权限

- **接口路径**: `DELETE /api/admin/permission/{id}`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 权限ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

## 10. 系统设置 (System Setting)

### 10.1 获取系统设置

- **接口路径**: `GET /api/admin/setting`
- **是否认证**: 是

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "siteName": "OpusNocturne",
    "siteDescription": "个人技术博客",
    "siteKeywords": "Java,Spring Boot,前端",
    "footerText": "© 2026 OpusNocturne",
    "adminEmail": "admin@opusnocturne.com",
    "commentAudit": true,
    "articlePageSize": 10,
    "commentPageSize": 20,
    "aboutMe": "# About Me\n这里是关于我的介绍..."
  }
}
```

**字段说明**
| 字段名 | 类型 | 说明 | 对应数据库字段 |
|:---|:---|:---|:---|
| siteName | string | 站点名称 | `site_name` |
| siteDescription | string | 站点描述 | `site_description` |
| siteKeywords | string | 站点关键词 | `site_keywords` |
| footerText | string | 页脚文本 | `footer_text` |
| adminEmail | string | 管理员邮箱 | `admin_email` |
| commentAudit | boolean | **评论审核开关**：控制评论是否需要后台审核。开启后，所有评论需管理员审核通过后才显示；关闭后，评论提交后直接显示。 | `comment_audit` |
| articlePageSize | int | 文章列表每页条数 | `article_page_size` |
| commentPageSize | int | 评论列表每页条数 | `comment_page_size` |
| aboutMe | string | 关于我内容（Markdown） | `about_me` |

---

### 10.2 更新系统设置

- **接口路径**: `PUT /api/admin/setting`
- **是否认证**: 是

**请求体 (JSON)**: 同 10.1 字段说明表中的字段，所有字段均为选填。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

---

## 11. 站点统计 (Site Statistics)

### 11.1 获取站点概览统计

- **接口路径**: `GET /api/admin/statistics/overview`
- **是否认证**: 是

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "articleCount": 100,
    "categoryCount": 10,
    "tagCount": 50,
    "commentCount": 200,
    "userCount": 5,
    "totalViewCount": 5000
  }
}
```

---

### 11.2 获取文章发布趋势

- **接口路径**: `GET /api/admin/statistics/article-trend`
- **是否认证**: 是

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "labels": ["1月", "2月", "3月"],
    "data": [10, 15, 8]
  }
}
```

---

### 11.3 获取访问统计

- **接口路径**: `GET /api/admin/statistics/visit`
- **是否认证**: 是

**请求参数**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| topPagesLimit | Integer | 否 | 10 | 热门页面数量限制 |

**请求示例**
```bash
# 默认获取 10 个热门页面
GET /api/admin/statistics/visit

# 自定义获取 5 个热门页面
GET /api/admin/statistics/visit?topPagesLimit=5
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "totalVisits": 1000,
    "totalPageViews": 2000,
    "trend": [
      { "visitDate": "2026-02-10", "pv": 250, "uv": 120 },
      { "visitDate": "2026-02-11", "pv": 300, "uv": 150 }
    ],
    "topPages": [
      { "pageUrl": "/blog/article/1", "count": 500 },
      { "pageUrl": "/blog/article/2", "count": 300 }
    ]
  }
}
```

---

## 12. 友情链接 (Friend Link)

### 12.1 申请友情链接 (Portal)

- **接口路径**: `POST /api/blog/friend-link`
- **是否认证**: 是

> **审核说明**：所有申请默认进入**待审核**状态，需管理员后台通过后才会显示在友链页面。

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 | 示例 |
|:---|:---|:---|:---|:---|
| name | string | 是 | 您的网站名称 | `玄的实验室` |
| url | string | 是 | 您的网站地址 | `https://xuan.com` |
| icon | string | 否 | 网站图标URL | `https://xuan.com/logo.png` |
| description | string | 否 | 简短的网站描述 | `记录代码与生活` |
| email | string | 否 | 站长联络邮箱 | `admin@xuan.com` |

**请求示例**
```http
POST /api/blog/friend-link
Authorization: Bearer <Access-Token>
Content-Type: application/json

{
  "name": "玄的实验室",
  "url": "https://xuan.com",
  "icon": "https://xuan.com/logo.png",
  "description": "代码与生活"
}
```

**成功响应**
```json
{
  "code": 0,
  "message": "申请已提交，请等待管理员审核",
  "data": null
}
```

**错误响应 - 未登录 (401)**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

---

### 12.2 获取现有友链列表 (Portal)

- **接口路径**: `GET /api/blog/friend-link/list`
- **是否认证**: 否

> ⚠️ **注意**：仅返回状态为 `1`（已审核上线）的链接。

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "Google",
      "url": "https://www.google.com",
      "icon": "https://www.google.com/favicon.ico",
      "description": "全球最大的搜索引擎"
    }
  ]
}
```

---

### 12.3 后台友链管理 (Admin)

#### 12.3.1 分页获取友链列表

- **接口路径**: `GET /api/admin/friend-link/page`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| status | int | 否 | `0` | 状态：0-待审核，1-上线，2-下架 |
| name | string | 否 | `博客` | 名称搜索 |

#### 12.3.2 审核友链

- **接口路径**: `PUT /api/admin/friend-link/{id}/audit`
- **是否认证**: 是

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 友链ID |

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| status | int | 是 | 审核状态：1-上线，2-下架/拒绝 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

#### 12.3.3 修改友链

- **接口路径**: `PUT /api/admin/friend-link/{id}`
- **是否认证**: 是

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| name | string | 是 | 网站名称 |
| url | string | 是 | 网站地址 |
| icon | string | 否 | 图标 |
| description | string | 否 | 描述 |
| sort | int | 否 | 排序 |

#### 12.3.4 删除友链

- **接口路径**: `DELETE /api/admin/friend-link/{id}`
- **是否认证**: 是

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```






---

## 13. 多媒体管理 (Media - Admin)

### 13.1 上传文件 (Admin)

- **接口路径**: `POST /api/admin/attachment/upload`
- **是否认证**: 是
- **Content-Type**: `multipart/form-data`

**请求参数**

| 名称 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| file | file | 是 | 上传的文件 |
| bizType | string | 否 | 业务类型，如 `article`、`avatar`，用于分类归档 |
| bizId | long | 否 | 业务关联ID，如文章ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileName": "cover.png",
    "fileUrl": "http://localhost:8080/uploads/2026/02/27/cover.png",
    "filePath": "/uploads/2026/02/27/cover.png",
    "fileType": "image/png",
    "fileSize": 102400,
    "bizType": "article",
    "bizId": 100,
    "createTime": "2026-02-27 10:00:00"
  }
}
```

**字段说明**

| 字段名 | 类型 | 说明 |
|:---|:---|:---|
| id | long | 附件ID |
| fileName | string | 原始文件名 |
| fileUrl | string | 文件访问 URL（前端直接使用此地址渲染） |
| filePath | string | 服务器存储路径（内部使用，用于物理删除） |
| fileType | string | MIME 类型，如 `image/png`、`video/mp4` |
| fileSize | long | 文件大小（字节） |
| bizType | string | 业务类型标识，如 `article`（文章封面）、`avatar`（用户头像） |
| bizId | long | 关联的业务记录ID |
| createTime | string | 上传时间 |

> ⚠️ **注意**：上传成功后，前端应将返回的 `fileUrl` 填入对应的文章封面、内容图片等字段，而非 `filePath`。

---

### 13.2 分页获取附件列表 (Admin)

- **接口路径**: `GET /api/admin/attachment/page`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码，默认 `1` |
| size | int | 否 | `10` | 每页条数，默认 `10` |
| fileName | string | 否 | `logo` | 文件名模糊搜索 |
| fileType | string | 否 | `image/png` | 文件 MIME 类型精确匹配 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "cover.png",
        "fileUrl": "http://localhost:8080/uploads/2026/02/27/cover.png",
        "filePath": "/uploads/2026/02/27/cover.png",
        "fileType": "image/png",
        "fileSize": 102400,
        "bizType": "article",
        "bizId": 100,
        "createTime": "2026-02-27 10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

> 💡 **说明**：结果按 `createTime` 倒序（最新上传排在前）。

---

### 13.3 删除附件 (Admin)

- **接口路径**: `DELETE /api/admin/attachment/{id}`
- **是否认证**: 是
- **说明**: 删除数据库记录；若后端接入对象存储/本地存储，同时清理物理文件。

**路径参数**

| 名称 | 示例 | 说明 |
|:---|:---|:---|
| id | `1` | 附件ID |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**

| 场景 | HTTP 状态码 | code | message |
|:---|:---|:---|:---|
| 附件不存在 | 400 | 400 | 附件不存在 |
| 未登录 | 401 | 401 | 未登录或 Token 已过期 |

---

### 13.4 批量删除附件 (Admin)

- **接口路径**: `DELETE /api/admin/attachment/batch`
- **是否认证**: 是
- **说明**: 按 ID 列表批量删除附件记录，逐个执行删除与存在性校验。

**请求体 (JSON)**

直接传递附件 ID 数组：
```json
[1, 2, 3]
```

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": null
}
```

**失败响应**

| 场景 | HTTP 状态码 | code | message |
|:---|:---|:---|:---|
| 某个 ID 不存在 | 400 | 400 | 附件不存在 |
| 未登录 | 401 | 401 | 未登录或 Token 已过期 |

---

## 14. 系统管理 (System - Admin)

### 14.1 修改登录密码 (Admin)

- **接口路径**: `PUT /api/admin/auth/change-password`
- **是否认证**: 是
- **审计日志**: 本接口会产生审计日志

**请求体 (JSON)**

| 字段名 | 类型 | 必填 | 说明 |
|:---|:---|:---|:---|
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码（长度6-20位） |
| confirmPassword | string | 是 | 确认新密码 |

**成功响应**
```json
{
  "code": 0,
  "message": "密码修改成功，请重新登录",
  "data": null
}
```

### 14.2 查看操作日志 (Admin)

- **接口路径**: `GET /api/admin/log/operation`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| module | string | 否 | `文章管理` | 模块名称 |
| status | int | 否 | `1` | 状态：1-成功；0-失败 |
| startTime | string | 否 | `2023-10-01 00:00:00` | 开始时间 |
| endTime | string | 否 | `2023-10-02 00:00:00` | 结束时间 |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "module": "文章管理",
        "operation": "发布文章",
        "operator": "admin",
        "ip": "127.0.0.1",
        "status": 1,
        "costTime": 50,
        "createTime": "2023-10-01 10:00:00"
      }
    ],
    "total": 100
  }
}
```



---

### 14.3 获取服务器监控信息 (Admin)

- **接口路径**: `GET /api/admin/monitor/server`
- **是否认证**: 是
- **HTTP 状态码**: 200 (成功), 401 (未认证), 403 (权限不足)
- **说明**: 返回当前服务器的实时监控快照，包含 CPU、内存、操作系统三个维度的数据。
  数据由后台定时任务每 **2 秒**刷新一次并缓存，本接口为纯读缓存操作，响应延迟极低。

---

### 14.4 查看访问日志 (Admin)

- **接口路径**: `GET /api/admin/log/visit`
- **是否认证**: 是

**查询参数**

| 名称 | 类型 | 必填 | 示例 | 说明 |
|:---|:---|:---|:---|:---|
| current | int | 否 | `1` | 页码 |
| size | int | 否 | `10` | 每页条数 |
| startTime | string | 否 | `2023-10-01 00:00:00` | 开始时间 |
| endTime | string | 否 | `2023-10-02 00:00:00` | 结束时间 |
| pageUrl | string | 否 | `/blog/article/1` | 访问页面URL |
| ipAddress | string | 否 | `江苏省 南京市` | IP地址（已解析为地理位置，如"江苏省 南京市"） |

**成功响应**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "ipAddress": "本地",
        "userAgent": "Mozilla/5.0...",
        "visitTime": "2023-10-01 10:00:00",
        "pageUrl": "/blog/article/1",
        "referer": "https://www.google.com"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

**成功响应（200）**
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "cpu": {
      "name": "Intel(R) Core(TM) i9-13900K CPU @ 3.00GHz",
      "packages": 1,
      "cores": 32,
      "usage": 15.63
    },
    "memory": {
      "total": "32.00 GB",
      "used": "18.42 GB",
      "free": "13.58 GB",
      "usage": 57.56
    },
    "system": {
      "os": "Windows 11",
      "arch": "amd64",
      "uptime": "3天 2小时 15分钟"
    }
  }
}
```

**响应字段说明**

| 字段路径 | 类型 | 说明 |
|:---|:---|:---|
| `cpu.name` | string | CPU 型号名称 |
| `cpu.packages` | int | 物理 CPU 数量（路数） |
| `cpu.cores` | int | 逻辑核心数（含超线程） |
| `cpu.usage` | double | CPU 使用率（%），保留两位小数 |
| `memory.total` | string | 物理内存总量（人类可读，如 `16.00 GB`） |
| `memory.used` | string | 已用内存 |
| `memory.free` | string | 可用内存 |
| `memory.usage` | double | 内存使用率（%），保留两位小数 |
| `system.os` | string | 操作系统名称（来自 JVM 系统属性 `os.name`） |
| `system.arch` | string | CPU 指令集架构（如 `amd64`、`aarch64`） |
| `system.uptime` | string | 系统持续运行时长（如 `3天 2小时 15分钟`） |

**错误响应（401）**
```json
{
  "code": 2001,
  "message": "请先登录后再操作",
  "data": null
}
```

---

## 15. 待实现接口 (Project Roadmap)

以下功能将在后续版本中逐步完善：

1. **操作日志 (Operation Log)**
    - 数据库表结构已创建 (`sys_oper_log`)
    - 待实现: Aspect 切面记录入库、查询接口 `GET /api/admin/log/operation`

2. **全文搜索 (Full-text Search)**
    - 计划使用 Elasticsearch 或 MySQL FullText 实现文章内容的全文检索。


---

## 变更记录

| 版本号 | 日期 | 变更人 | 变更摘要 | 兼容级别 |
|:---:|:---:|:---:|:---|:---|
| **2.5.0** | 2026-02-28 | Admin | 合并了若干接口，删除了冗余的不必要接口；更新了目录结构 | Compatible |
| **2.4.0** | 2026-02-28 | Admin | 删除旧第8节「文件上传」接口（已由第15节多媒体管理 `POST /api/admin/attachment/upload` 替代）；章节编号整体前移（原9-19节 → 8-18节）；同步更新目录与变更记录引用 | Compatible |
| **2.3.0** | 2026-02-28 | Admin | 新增 16.5 节「获取服务器监控信息」接口文档（`GET /api/admin/monitor/server`），补充 CPU/内存/系统三维度响应字段说明；更新目录子条目 | Compatible |
| **2.2.0** | 2026-02-27 | Admin | 重构第15节多媒体管理：新增上传(15.1)、批量删除(15.4)接口；补全附件字段说明、bizType/bizId 说明及失败响应；接口编号整体顺移 | Compatible |
| **2.1.0** | 2026-02-20 | Admin | 统一时间格式约定、补充 likeCount/aboutMe 字段、完善关于我接口文档、增加空值处理和时间格式全局约定 | Compatible |
| **1.1.0** | 2026-02-20 | Admin | 升级为平台级API规范，包含HTTP语义化改造、幂等性与并发控制、数据模型抽象层等 | Breaking |
| **1.0.0** | 2026-01-01 | Admin | 初始版本 | Compatible |

---
**End of Document**
