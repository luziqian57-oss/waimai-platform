# 外卖平台后端

可独立部署的微信小程序外卖平台 REST API。后端使用 Java 21、Spring Boot、
MyBatis、MySQL、Redis、JWT、Maven 和 Git，实现了从用户登录到订单完成的业务闭环。

## 已实现功能

- 用户名密码注册、登录和 JWT 鉴权
- 微信 `code2Session` 登录；本地可显式开启 mock 模式
- Redis 登录失败限流：5 分钟内连续失败 5 次返回 HTTP 429
- 店铺、商品分类、商品和 SKU 菜单查询
- 管理员维护店铺、分类、商品、SKU，并使用软下架避免破坏历史订单
- 收货地址增删改查和默认地址
- 购物车添加、修改数量、删除和金额计算
- 订单主表/明细表、商品快照、事务扣库存、模拟支付、取消恢复库存
- 管理员订单状态流转：`PAID → CONFIRMED → PREPARING → DELIVERING → COMPLETED`
- Docker Compose、GitHub Actions、GHCR 镜像和 Railway 部署配置

## 主要接口

| 功能 | 方法与路径 | 权限 |
|---|---|---|
| 健康检查 | `GET /api/health` | 公开 |
| 注册/登录 | `POST /api/auth/register`、`POST /api/auth/login` | 公开 |
| 微信登录 | `POST /api/auth/wechat` | 公开 |
| 店铺/菜单 | `GET /api/shops`、`GET /api/shops/{id}/menu` | 公开 |
| 当前用户 | `GET /api/users/me` | JWT |
| 地址 | `/api/addresses` | JWT |
| 购物车 | `/api/cart`、`/api/cart/items` | JWT |
| 订单 | `/api/orders`、支付、取消 | JWT |
| 商品管理 | `/api/admin/catalog/**` | ADMIN |
| 订单管理 | `PUT /api/admin/orders/{orderNo}/status` | ADMIN |

完整请求示例见 `postman/Waimai-Platform.postman_collection.json`。

## 本地运行

MySQL 创建 `waimai_platform` 数据库并启动 Redis，然后在 IDEA 的
`WaimaiPlatformApplication` 运行配置中设置 `JWT_SECRET`。密钥不少于 32 字节，
同一环境应保持稳定，否则重启后旧 JWT 会失效。

终端启动：

```bash
export JWT_SECRET="$(openssl rand -hex 48)"
./mvnw spring-boot:run
```

默认地址为 `http://localhost:8081`。首次启动会幂等创建表和“星河厨房”演示菜单。

## Docker 一键运行

```bash
cp .env.example .env
# 编辑 .env，为 MySQL、Redis 和 JWT 设置独立随机密码
docker compose up --build -d
```

`.env` 已被 Git 忽略。不要把真实密码、JWT 密钥或微信 AppSecret 提交到仓库。

## 管理员初始化

首次部署时设置：

```text
ADMIN_BOOTSTRAP_ENABLED=true
ADMIN_BOOTSTRAP_USERNAME=<管理员账号>
ADMIN_BOOTSTRAP_PASSWORD=<至少12位随机密码>
```

管理员创建成功后，把 `ADMIN_BOOTSTRAP_ENABLED` 改回 `false` 并移除密码环境变量。

## 微信登录

正式环境设置 `WECHAT_APP_ID` 和 `WECHAT_APP_SECRET`，小程序把 `wx.login()` 返回的
`code` 提交到 `POST /api/auth/wechat`。`WECHAT_MOCK_ENABLED` 默认是 `false`。

仅本地闭环测试可设置 `WECHAT_MOCK_ENABLED=true`，再提交 `mock-` 开头的 code。
公网环境严禁开启 mock 模式。

## 发布到 GitHub 和公网

公开仓库：`https://github.com/luziqian57-oss/waimai-platform`

生产 API：`https://api-production-0977b.up.railway.app`

1. 在 GitHub 创建空仓库，把本项目推送到 `main`。
2. GitHub Actions 会使用 Java 21 运行测试并将 Docker 镜像发布到 GHCR。
3. 在 Railway 创建项目并连接该 GitHub 仓库。
4. 在同一项目添加 MySQL 和 Redis 服务。
5. 为 API 服务设置下列变量，并引用 Railway 数据库服务提供的内部地址：

```text
DB_URL=jdbc:mysql://<mysql-host>:<mysql-port>/<database>?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
DB_USERNAME=<mysql-user>
DB_PASSWORD=<mysql-password>
REDIS_HOST=<redis-host>
REDIS_PORT=<redis-port>
REDIS_PASSWORD=<redis-password>
JWT_SECRET=<至少32字节随机密钥>
CORS_ALLOWED_ORIGINS=https://<前端正式域名>
WECHAT_APP_ID=<微信AppID>
WECHAT_APP_SECRET=<微信AppSecret>
WECHAT_MOCK_ENABLED=false
```

6. 为 API 服务生成 Railway 公网域名。`railway.json` 已配置 `/api/health` 健康检查。
7. 将 Postman 的 `baseUrl` 或小程序的 API 地址改成生成的 HTTPS 域名。

GitHub Pages 只能托管静态前端，不能直接运行 Spring Boot、MySQL 和 Redis；因此
GitHub 用于代码与镜像，Railway 或其他支持 Docker 的云平台负责实际 API 服务。

## 生产环境注意事项

- 不使用 MySQL root 账号，所有密码与密钥只放云平台 Secret/Variables。
- `CORS_ALLOWED_ORIGINS` 设置为真实前端域名，不在生产环境长期使用 `*`。
- 当前支付接口是演示状态流转，接入真实支付前必须增加签名验签和支付回调幂等。
- 数据量增大后将 `schema.sql` 升级为 Flyway 版本化迁移，并配置数据库备份。
- Redis 当前只负责登录限流，没有缓存热门商品。
