# NoteSpace 后端部署指南

## 快速开始

本指南提供多种部署方式，根据你的需求选择：

| 部署方式 | 难度 | 成本 | 适用场景 |
|----------|------|------|----------|
| **Railway** | ⭐ 简单 | 免费额度 | 海外访问、快速部署 |
| **Zeabur** | ⭐ 简单 | 免费额度 | 国内访问、快速部署 |
| **腾讯云轻量** | ⭐⭐⭐ | ~17元/月 | 国内生产环境 |

---

## 方案一：Railway（海外，最简单）

### 步骤概览

1. 访问 [railway.app](https://railway.app) 并登录
2. **New Project** → **Deploy from GitHub repo**
3. 选择 `notespace-serve` 仓库
4. **New** → **Database** → **MySQL**
5. 添加环境变量：`JWT_SECRET`
6. 初始化数据库：执行 `ALTER TABLE notes ADD COLUMN share_id VARCHAR(36) UNIQUE DEFAULT NULL;`
7. 获取 API 地址

### 详细步骤

参考：[DEPLOY_BACKEND.md](./DEPLOY_BACKEND.md)

---

## 方案二：Zeabur（国内节点）

### 步骤概览

1. 访问 [zeabur.com](https://zeabur.com) 并登录
2. 创建项目 → 选择国内区域（上海）
3. 添加 Service → Git → 选择 `notespace-serve` 仓库
4. 添加 MySQL 服务
5. 配置环境变量
6. 获取 API 地址

---

## 方案三：国内云服务器

### 腾讯云轻量服务器

详细步骤请参考：[DEPLOY_CN.md](./DEPLOY_CN.md)

快速命令：

```bash
# SSH 登录服务器
ssh root@your-server-ip

# 安装环境
sudo apt update && sudo apt install openjdk-8-jdk maven mysql-server nginx -y

# 部署应用
mkdir -p /opt/notespace
cd /opt/notespace
git clone https://github.com/xpc254/notespace-serve.git
cd notespace-serve
mvn clean package -DskipTests

# 创建数据库
sudo mysql -e "CREATE DATABASE IF NOT EXISTS notespace;"
sudo mysql notespace -e "ALTER TABLE notes ADD COLUMN IF NOT EXISTS share_id VARCHAR(36) UNIQUE DEFAULT NULL;"

# 启动服务
java -jar target/notespace-serve-1.0.0.jar &
```

---

## 环境变量配置

### 必需变量

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring 环境 | `production` |
| `JWT_SECRET` | JWT 密钥 | 随机字符串 |

### 数据库配置（如平台不自动注入）

| 变量名 | 说明 |
|--------|------|
| `SPRING_DATASOURCE_URL` | 数据库连接字符串 |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |

### Redis 配置（可选）

| 变量名 | 说明 |
|--------|------|
| `SPRING_REDIS_HOST` | Redis 主机 |
| `SPRING_REDIS_PORT` | Redis 端口（默认 6379） |
| `SPRING_REDIS_PASSWORD` | Redis 密码 |

---

## 数据库初始化

部署后需要执行以下 SQL：

```sql
-- 添加分享功能字段
ALTER TABLE notes ADD COLUMN IF NOT EXISTS share_id VARCHAR(36) UNIQUE DEFAULT NULL;
```

---

## 验证部署

### 健康检查

访问：`http://你的地址/api/actuator/health`

预期响应：
```json
{"status":"UP"}
```

### 注册测试

```bash
curl -X POST http://你的地址/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123","email":"test@example.com"}'
```

---

## 常见问题

**Q: 后端启动失败**
- 检查日志：`journalctl -u notespace -f`（服务器部署）
- 确认数据库连接配置正确

**Q: 首次访问很慢**
- 免费服务有休眠机制，首次访问需 30-60 秒唤醒

**Q: 前端无法连接后端**
1. 确认后端部署成功
2. 检查 GitHub Secrets 中的 `API_BASE_URL`
3. 查看浏览器控制台错误

---

## 更新前端配置

后端部署成功后，更新前端 API 地址：

1. 访问：https://github.com/xpc254/vue-notespace/settings/secrets/actions
2. 添加 Secret：
   - Name: `API_BASE_URL`
   - Value: 你的后端地址
