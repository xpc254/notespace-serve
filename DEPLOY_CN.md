# NoteSpace 国内部署指南

## 国内免费/低价部署方案对比

| 平台 | 免费额度 | 优点 | 缺点 | 推荐度 |
|------|----------|------|------|--------|
| **腾讯云轻量服务器** | 新用户约50元/3个月 | 稳定、国内访问快 | 需要备案 | ⭐⭐⭐⭐⭐ |
| **阿里云ECS学生机** | 9.5元/月（学生） | 稳定、阿里生态 | 需要备案 | ⭐⭐⭐⭐⭐ |
| **华为云ECS** | 新用户免费试用 | 稳定 | 需要备案 | ⭐⭐⭐⭐ |
| **Rainbond** | 个人免费 | 国产PaaS、简单 | 需自备数据库 | ⭐⭐⭐⭐ |
| **Zeabur** | 有免费额度 | 国内节点、快速 | 额度有限 | ⭐⭐⭐⭐ |

---

## 方案一：腾讯云轻量服务器（推荐）

### 为什么选择腾讯云？

- ✅ 新用户优惠：约 **50元/3个月**
- ✅ 国内访问速度快
- ✅ 自带公网IP
- ✅ 支持 Docker/传统部署
- ✅ 提供 MySQL 镜像

### 步骤 1: 购买服务器

1. 访问 [腾讯云轻量服务器](https://cloud.tencent.com/product/lighthouse)
2. 选择配置：
   - **区域**：选择离你最近的（如北京/上海/广州）
   - **套餐**：2核2G（新用户优惠约50元/3个月）
   - **镜像**：Ubuntu 22.04
   - **购买时长**：3个月起

### 步骤 2: 服务器环境配置

SSH 登录服务器后执行：

```bash
# 安装 JDK 8
sudo apt update
sudo apt install openjdk-8-jdk -y

# 安装 Maven
sudo apt install maven -y

# 安装 MySQL 8
sudo apt install mysql-server -y

# 安装 Nginx（反向代理）
sudo apt install nginx -y

# 验证安装
java -version
mvn -version
mysql --version
```

### 步骤 3: 部署后端应用

```bash
# 创建应用目录
mkdir -p /opt/notespace
cd /opt/notespace

# 上传代码（使用 git 或 scp）
# 方式1: git clone
git clone https://github.com/xpc254/notespace-serve.git

# 方式2: 使用 scp 从本地上传
# scp -r notespace-serve root@your-server-ip:/opt/notespace/

# 编译打包
cd notespace-serve
mvn clean package -DskipTests

# 创建生产配置文件
cat > src/main/resources/application-prod.yml << EOF
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notespace?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
  redis:
    host: localhost
    port: 6379
    password:
    database: 0

jwt:
  secret: $(openssl rand -base64 32)
  access-expiration: 7200000
  refresh-expiration: 604800000
EOF

# 使用生产配置重新打包
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

### 步骤 4: 初始化数据库

```bash
# 创建数据库和用户
sudo mysql -e "
CREATE DATABASE IF NOT EXISTS notespace;
CREATE USER IF NOT EXISTS 'notespace'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON notespace.* TO 'notespace'@'localhost';
FLUSH PRIVILEGES;
"

# 导入初始表结构（如果有 init.sql）
sudo mysql notespace < src/main/resources/sql/init.sql

# 添加 share_id 字段（分享功能）
sudo mysql notespace -e "
ALTER TABLE notes ADD COLUMN IF NOT EXISTS share_id VARCHAR(36) UNIQUE DEFAULT NULL;
"
```

### 步骤 5: 创建系统服务（开机自启）

```bash
sudo cat > /etc/systemd/system/notespace.service << EOF
[Unit]
Description=NoteSpace Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/notespace/notespace-serve
ExecStart=/usr/bin/java -jar target/notespace-serve-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable notespace
sudo systemctl start notespace

# 查看状态
sudo systemctl status notespace
```

### 步骤 6: 配置 Nginx 反向代理

```bash
sudo cat > /etc/nginx/sites-available/notespace << EOF
server {
    listen 80;
    server_name your-domain.com;  # 如果有域名，替换成你的域名

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# 启用配置
sudo ln -s /etc/nginx/sites-available/notespace /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 步骤 7: 防火墙配置

```bash
# 开放必要端口
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

### 步骤 8: 获取公网 IP

在腾讯云控制台查看服务器公网 IP，API 地址为：
```
http://你的公网IP/api
```

---

## 方案二：Zeabur（国内节点）

### 特点

- 支持国内节点（上海）
- 自动部署
- 内置 MySQL/PostgreSQL
- 新用户有免费额度

### 部署步骤

1. 访问 [zeabur.com](https://zeabur.com)
2. GitHub 账号登录
3. 创建项目 → 选择国内区域
4. 添加 Service → Git → 选择 `notespace-serve` 仓库
5. Zeabur 自动检测 Spring Boot 并配置
6. 添加 MySQL 服务
7. 配置环境变量（参考 Railway 配置）
8. 获取 API 地址

---

## 方案三：Rainbond（国产 PaaS）

### 特点

- 完全开源国产 PaaS
- 个人版免费
- 支持源码部署
- 可私有化部署

### 部署步骤

1. 访问 [rainbond.com](https://www.rainbond.com)
2. 注册并创建团队
3. 创建应用 → 从源码部署
4. 连接 GitHub 仓库
5. Rainbond 自动识别 Spring Boot 项目
6. 配置数据库（需要自己准备 MySQL）
7. 一键部署

---

## 备案说明

⚠️ **重要提示**：使用国内云服务器部署并提供公网访问，根据中国法律规定需要完成 **ICP 备案**。

### 备案流程

1. 购买域名（如 .com/.cn）
2. 在云服务商进行实名认证
3. 提交备案申请
4. 等待管局审核（约 7-20 个工作日）

### 临时方案（无域名）

如果没有域名，可以：
- 使用服务器的公网 IP 直接访问
- 或使用 Zeabur 等平台提供的临时域名

---

## 费用对比

| 方案 | 月费用 | 备案要求 | 推荐场景 |
|------|--------|----------|----------|
| 腾讯云轻量 | ~17元 | 需要 | 生产环境、长期使用 |
| 阿里云学生机 | 9.5元 | 需要 | 学生、测试环境 |
| Zeabur | 免费额度用完付费 | 不需要 | 快速测试 |
| Railway | 不限 | 不需要 | 海外用户 |

---

## 推荐方案总结

| 场景 | 推荐方案 |
|------|----------|
| 国内用户长期使用 | 腾讯云轻量服务器 |
| 学生/个人项目 | 阿里云学生机 |
| 快速测试 | Zeabur |
| 企业/团队 | 阿里云/腾讯云 ECS |

---

## 更新前端 API 配置

后端部署成功后，记得更新前端的 API 地址：

1. 访问：https://github.com/xpc254/vue-notespace/settings/secrets/actions
2. 添加 Secret：
   - Name: `API_BASE_URL`
   - Value: 你的后端地址（如 `http://你的IP/api`）
