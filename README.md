# 外卖平台后端

微信小程序外卖平台的 Spring Boot 基础工程。

## 技术栈

- Java 21
- Spring Boot 4.0.7
- MyBatis 4.1.0
- MySQL
- Redis
- Spring Security + JWT
- Maven + Git

## 本地启动

MySQL 和 Redis 启动后，在 IntelliJ IDEA 中打开本目录，选择 Java 21，运行
`WaimaiPlatformApplication`。

也可以在终端执行：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./mvnw spring-boot:run
```

环境检查接口：`GET http://localhost:8081/api/health`

默认连接本机的 `waimai_platform` 数据库（root 空密码）和 Redis 0 号库。可通过
`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、
`REDIS_PASSWORD` 环境变量覆盖，敏感配置不要提交到 Git。
