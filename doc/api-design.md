# z-cosmos 后端 RESTful API 设计文档

> 版本：v1.0.0
> 最后更新：2026-04-30
> 维护：本文档与前端 `app/src/views/*.vue` 页面功能一一对应

## 通用约定

### 基础信息
- **Base URL**：`http://{host}:8888`
- **统一前缀**：`/api`
- **媒体类型**：`application/json;charset=UTF-8`（文件上传：`multipart/form-data`）
- **编码**：UTF-8
- **时区**：GMT+8（`yyyy-MM-dd HH:mm:ss`）

### 认证方式
- **Access Token**：JWT，置于 `Authorization: Bearer <token>` 或 HttpOnly Cookie（`accessToken`）
- **Refresh Token**：仅 Body 传递（`POST /api/auth/refresh`）
- **受保护接口**：未登录返回 `401 Result.unauthorized`

### 统一响应包装
所有接口返回 `Result<T>`：
```json
{ "code": 200, "msg": "success", "data": { } }
```
常见 code：
| code | 含义 |
|------|------|
| 200  | 成功 |
| 400  | 参数错误 |
| 401  | 未登录 / Token 过期 |
| 403  | 无权限 |
| 404  | 资源不存在 |
| 429  | 请求过于频繁 |
| 500  | 系统异常 |

### 分页响应
```json
{
  "code": 200, "msg": "success",
  "data": {
    "records": [ ... ],
    "total": 123,
    "size": 20,
    "current": 1,
    "pages": 7
  }
}
```

### 鉴权列
| 标记 | 含义 |
|------|------|
| 公开 | 无需登录 |
| 登录 | 需携带有效 Token |
| 作者 | 需登录且为资源作者 |
| 管理 | 需管理员权限（userType=4） |

---

## 1. 认证模块（已实现，参考 `cn.kong.cosmos.auth`）

路径前缀：`/api/auth`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| POST | `/login` | 公开 | 账号密码登录（手机号 + 密码） |
| POST | `/logout` | 登录 | 登出（Token 入黑名单 + 清 Cookie） |
| POST | `/refresh` | 公开 | Refresh Token 换 Access Token |
| POST | `/oauth/url` | 公开 | 获取第三方 OAuth 授权链接（github/gitee/qq） |
| GET  | `/oauth/callback` | 公开 | 第三方回调换取 Token |
| GET  | `/wechat/qr` | 公开 | 获取微信登录二维码 |
| GET  | `/wechat/status` | 公开 | 轮询微信扫码状态 |

---

## 2. 用户中心（Profile.vue）

路径前缀：`/api/user`

### 2.1 获取当前用户信息
- `GET /me`
- **鉴权**：登录
- **响应**：`UserProfileDTO`
```json
{
  "id": 1, "userId": "18000000001",
  "phone": "138****0001", "nickname": "星际旅人",
  "avatarUrl": "/static/avatar/xxx.png",
  "gender": 1, "bio": "探索星辰大海", "location": "北京", "birthday": "2000-01-01",
  "userType": 1, "userLevel": 3, "verifiedStatus": 0,
  "followerCount": 120, "followingCount": 56, "postCount": 18,
  "likeCount": 240, "collectCount": 32,
  "createdAt": "2026-01-01 00:00:00"
}
```

### 2.2 更新个人资料
- `PUT /profile`
- **鉴权**：登录
- **Body**：`UpdateProfileReq`
```json
{
  "nickname": "星际旅人", "gender": 1,
  "bio": "探索星辰大海", "location": "北京", "birthday": "2000-01-01"
}
```
- **校验**：nickname 2-20 字；bio ≤ 200 字；location ≤ 50 字；gender ∈ {0,1,2}

### 2.3 修改密码
- `PUT /password`
- **鉴权**：登录
- **Body**：`ChangePasswordReq`
```json
{ "oldPassword": "old123", "newPassword": "new123456" }
```
- **校验**：newPassword 6-32 字；旧密码必须匹配

### 2.4 上传头像
- `POST /avatar`
- **鉴权**：登录
- **Content-Type**：`multipart/form-data`
- **字段**：`file`（图片，≤5MB，jpg/png/webp）
- **响应**：`{ "avatarUrl": "/static/avatar/xxx.png" }`

### 2.5 我的帖子
- `GET /posts?page=1&size=10`
- **鉴权**：登录
- **响应**：`Page<PostSummaryDTO>`

### 2.6 我的收藏
- `GET /collections?page=1&size=10`
- **鉴权**：登录
- **响应**：`Page<PostSummaryDTO>`

---

## 3. 社区模块（Community.vue）

### 3.1 帖子 `/api/posts`

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET  | `` | 公开 | 帖子列表 |
| GET  | `/{postId}` | 公开 | 帖子详情 |
| POST | `` | 登录 | 发帖 |
| PUT  | `/{postId}` | 作者 | 编辑帖子 |
| DELETE | `/{postId}` | 作者 | 删除帖子 |
| POST | `/{postId}/like` | 登录 | 点赞 |
| DELETE | `/{postId}/like` | 登录 | 取消点赞 |
| POST | `/{postId}/collect` | 登录 | 收藏 |
| DELETE | `/{postId}/collect` | 登录 | 取消收藏 |

#### 3.1.1 列表查询
- `GET /api/posts?categoryCode=tech&sort=hot&keyword=ai&page=1&size=10`
- **Query 参数**：
  - `categoryCode`：分类代码（可选，空=全部）
  - `sort`：排序（`latest` | `hot` | `most_commented`，默认 `latest`）
  - `keyword`：关键词（可选，标题/内容 LIKE）
  - `page`：页码（默认 1）
  - `size`：每页（默认 10，上限 50）
- **响应**：`Page<PostSummaryDTO>`
```json
{
  "postId": "7264...", "title": "...", "summary": "前 120 字...",
  "coverImage": "https://...",
  "author": { "userId": "...", "nickname": "...", "avatarUrl": "..." },
  "category": { "code": "tech", "name": "技术", "color": "#7890b5" },
  "viewCount": 1230, "likeCount": 56, "commentCount": 12, "collectCount": 8,
  "isTop": 0, "isEssence": 1,
  "createdAt": "2026-04-30 12:00:00"
}
```

#### 3.1.2 帖子详情
- `GET /api/posts/{postId}`
- **响应**：`PostDetailDTO`（包含完整 content + images + 当前用户是否点赞/收藏）
- **副作用**：view_count + 1（事务外异步或 setSql）

#### 3.1.3 发布帖子
- `POST /api/posts`
- **Body**：`CreatePostReq`
```json
{
  "categoryCode": "tech",
  "title": "为什么要仰望星空",
  "content": "<p>...</p>",
  "images": ["https://...", "https://..."]
}
```
- **校验**：
  - title 4-100 字
  - content 10-10000 字（`community.post.min/max-content-length`）
  - images ≤ 9 张
  - 每日发帖 ≤ `community.post.daily-limit`（Redis 计数）
  - 内容通过 Jsoup 清洗防 XSS

#### 3.1.4 编辑帖子
- `PUT /api/posts/{postId}`
- **Body**：`UpdatePostReq`（字段同 CreatePostReq，均可选，传什么改什么）
- **鉴权**：仅作者可改；超过 24h 禁止编辑

#### 3.1.5 删除帖子
- `DELETE /api/posts/{postId}`
- **鉴权**：作者或管理员
- **副作用**：逻辑删除（is_deleted=1），级联隐藏评论

#### 3.1.6 点赞 / 取消点赞
- `POST /api/posts/{postId}/like` → 幂等，已点赞返回 200
- `DELETE /api/posts/{postId}/like`

#### 3.1.7 收藏 / 取消收藏
- `POST /api/posts/{postId}/collect`
- `DELETE /api/posts/{postId}/collect`

### 3.2 评论

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET  | `/api/posts/{postId}/comments?page=1&size=20` | 公开 | 评论树形列表 |
| POST | `/api/posts/{postId}/comments` | 登录 | 发评论 / 回复 |
| DELETE | `/api/comments/{commentId}` | 作者 | 删除评论 |
| POST | `/api/comments/{commentId}/like` | 登录 | 评论点赞 |

#### 3.2.1 评论列表（楼中楼）
- `GET /api/posts/{postId}/comments?page=1&size=20`
- **响应**：`Page<CommentTreeDTO>`
```json
{
  "commentId": "...", "content": "...",
  "author": { "userId": "...", "nickname": "...", "avatarUrl": "..." },
  "likeCount": 5, "createdAt": "...",
  "replies": [
    { "commentId": "...", "content": "...",
      "author": {...}, "replyToNickname": "星际旅人",
      "likeCount": 0, "createdAt": "..." }
  ],
  "replyCount": 3
}
```

#### 3.2.2 发评论 / 回复
- `POST /api/posts/{postId}/comments`
- **Body**：`CreateCommentReq`
```json
{
  "content": "说得好！",
  "parentId": null,
  "replyToUserId": null
}
```
- parentId=null → 顶层评论；非空 → 楼中楼回复

### 3.3 分类

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/categories` | 公开 | 分类列表（Redis 缓存） |

**响应**：`List<CategoryDTO>`
```json
[
  { "code": "tech", "name": "技术", "description": "...", "icon": "ri-code-line", "color": "#7890b5", "sort": 1 }
]
```

---

## 4. 搜索模块（Search.vue，仅设计）

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/search?q=&type=&page=&size=` | 公开 | 全文搜索 |
| GET | `/api/search/hot-keywords` | 公开 | 热门搜索词 Top10 |
| GET | `/api/search/suggestions?q=` | 公开 | 输入建议 |

**type** 枚举：`post` | `news` | `video` | `user`（省略时搜索全部，分段返回）

---

## 5. 热点新闻（Hot.vue，仅设计）

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/news?category=&page=&size=` | 公开 | 新闻列表 |
| GET | `/api/news/{newsId}` | 公开 | 新闻详情 |
| GET | `/api/news/ranking?period=day` | 公开 | 热门排行（day/week/month） |

---

## 6. 剧场模块（Theater.vue，仅设计）

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/videos?category=&page=&size=` | 公开 | 视频列表 |
| GET | `/api/videos/{videoId}` | 公开 | 视频详情（含剧集列表） |
| POST | `/api/videos/{videoId}/play` | 登录 | 播放记录上报 |

---

## 7. 工具模块（Tools.vue）

**纯前端模块**，数据存储于浏览器 localStorage，不提供后端接口。

---

## 8. 首页（Home.vue）

**无专属接口**，登录态仅依赖 `GET /api/user/me`（或本地缓存）判断显示"登录按钮"或"个人主页按钮"。

---

## 附录 A：鉴权拦截器白名单

由 `AuthSecurityConfig.addInterceptors` 管理。本期新增公开路径：

```
/api/posts                   (GET 列表)
/api/posts/{postId}          (GET 详情)
/api/posts/{postId}/comments (GET 评论)
/api/categories              (GET 分类)
/api/search/**               (全部公开)
/api/news/**                 (全部公开)
/api/videos                  (GET 列表)
/api/videos/{videoId}        (GET 详情)
```

## 附录 B：错误码详表

| code | 场景 |
|------|------|
| 400  | 参数校验失败 / 业务参数错误 |
| 401  | 未登录 / Token 过期 / Token 黑名单 |
| 403  | 非作者编辑他人帖子 / 封禁用户发帖 |
| 404  | 帖子/评论/分类不存在 |
| 409  | 密码旧值不匹配 / 重复点赞（非幂等时） |
| 429  | IP 频控 / 发帖日限 |
| 500  | 数据库异常 / 未分类异常 |

## 附录 C：前端调用适配建议

- 前端 axios 统一响应拦截：`response.data.code !== 200` 时抛出，`401` 自动跳转登录页
- 列表类请求 response.data.data.records 取数组
- 头像图片建议前端请求时拼接 `?t=${timestamp}` 绕过缓存
- 发帖前端内容编辑器输出 HTML，后端 Jsoup 清洗白名单
