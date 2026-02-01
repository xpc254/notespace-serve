# Zeabur Jar 包部署指南

## 部署流程

```
本地打包 → 创建 Docker 镜像 → 推送到 Zeabur → 配置环境变量 → 完成
```

---

## 步骤 1: 本地打包

```bash
cd e:\AI学习\notebook\project\notespace-serve

# 清理并打包
mvn clean package -DskipTests

# 确认 jar 文件生成
ls -lh target/notespace-serve-1.0.0.jar
```

打包成功后，`target/notespace-serve-1.0.0.jar` 就是你的应用文件。

---

## 步骤 2: 准备 Dockerfile

已在项目根目录创建 `Dockerfile`，内容如下：

```dockerfile
FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/notespace-serve-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 步骤 3: 在 Zeabur 创建服务

### 方式 A：使用 Dockerfile 部署（推荐）

1. 在 Zeabur 项目中，点击 **+ Add Service** → **Dockerfile**
2. 选择 **上传代码** 或 **连接 Git 仓库**（私有仓库也可以）
3. Zeabur 会自动读取 Dockerfile 并构建

**问题**：Zeabur 的 Dockerfile 部署仍然需要从 Git 仓库拉取代码。

---

### 方式 B：使用预构建镜像（真正的离线部署）

#### 3.1 本地构建 Docker 镜像

```bash
# 进入项目目录
cd e:\AI学习\notebook\project\notespace-serve

# 构建 Docker 镜像
docker build -t notespace-backend:latest .

# 查看镜像
docker images | grep notespace
```

#### 3.2 推送到镜像仓库

推送到 Docker Hub（需要注册）：

```bash
# 登录 Docker Hub
docker login

# 给镜像打标签（替换成你的用户名）
docker tag notespace-backend:latest 你的用户名/notespace-backend:latest

# 推送到 Docker Hub
docker push 你的用户名/notespace-backend:latest
```

或者使用国内镜像仓库：

| 仓库 | 地址 | 说明 |
|------|------|------|
| 阿里云容器镜像 | https://cr.console.aliyun.com | 国内速度快 |
| 腾讯云容器镜像 | https://console.cloud.tencent.com/tke2 | 国内速度快 |

#### 3.3 在 Zeabur 部署镜像

1. 在 Zeabur 项目中，点击 **+ Add Service** → **Image**
2. 输入镜像地址：`你的用户名/notespace-backend:latest`
3. 点击部署

---

## 步骤 4: 添加 MySQL 服务

1. 在 Zeabur 项目中，点击 **+ Add Service** → **MySQL**
2. Zeabur 会自动创建 MySQL 数据库
3. 点击 MySQL 服务 → **Variables** 查看连接信息

---

## 步骤 5: 配置环境变量

在后端服务中添加以下环境变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `SPRING_DATASOURCE_URL` | 从 MySQL 服务获取 | 点击 MySQL 的 "Connect" 按钮 |
| `SPRING_DATASOURCE_USERNAME` | 从 MySQL 服务获取 | 自动提供 |
| `SPRING_DATASOURCE_PASSWORD` | 从 MySQL 服务获取 | 自动提供 |
| `JWT_SECRET` | 自定义密钥 | 如：`my-secret-key-12345` |

### 获取 MySQL 连接信息

在 Zeabur 中：
1. 点击 MySQL 服务
2. 点击 **Variables** 标签
3. 复制 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`

然后组合成 JDBC URL：
```
jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}
```

---

## 步骤 6: 初始化数据库

1. 在 Zeabur 中点击 MySQL 服务
2. 点击 **Console** 标签
3. 执行以下 SQL：

```sql
-- 添加分享功能字段
ALTER TABLE notes ADD COLUMN share_id VARCHAR(36) UNIQUE DEFAULT NULL;
```

---

## 步骤 7: 获取 API 地址

1. 部署成功后，点击后端服务
2. 在 **Networking** 标签查看域名
3. API 地址格式：`https://你的域名.zeabur.app/api`

---

## 完整命令汇总

```bash
# 1. 打包
cd e:\AI学习\notebook\project\notespace-serve
mvn clean package -DskipTests

# 2. 构建镜像
docker build -t notespace-backend:latest .

# 3. 登录并推送（替换用户名）
docker login
docker tag notespace-backend:latest 你的用户名/notespace-backend:latest
docker push 你的用户名/notespace-backend:latest

# 4. 在 Zeabur 部署镜像
# 添加服务 → Image → 输入镜像地址
```

---

## 如果完全不想用 Docker

可以考虑其他支持直接上传 jar 的平台：

| 平台 | 部署方式 |
|------|----------|
| **Render** | 上传 jar + 配置启动命令 |
| **Heroku** | 上传 jar + Procfile |
| **国内云服务器** | 直接运行 java -jar |

### 腾讯云轻量服务器部署（无 Docker）

```bash
# SSH 登录服务器
ssh root@your-server-ip

# 安装 JDK
sudo apt install openjdk-8-jdk -y

# 上传 jar 文件
scp target/notespace-serve-1.0.0.jar root@your-server-ip:/opt/notespace/

# 启动服务
nohup java -jar /opt/notespace/notespace-serve-1.0.0.jar &
```
