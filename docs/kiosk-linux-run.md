# 患者自助机 Linux 运行说明

## 适用系统

推荐 Ubuntu、Debian、UOS、Kylin 等 Linux 发行版。自助机采用 Web Kiosk 模式，不需要安装桌面客户端。

## 后端启动

先准备 PostgreSQL，并确认 `application.yml` 中数据库连接可用。

```bash
cd backend
mvn -q -DskipTests package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

默认后端地址：

```text
http://localhost:8080/api
```

健康检查：

```bash
curl http://localhost:8080/api/health
```

## 前端构建与运行

开发演示模式：

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

访问地址：

```text
http://localhost:5173/kiosk
```

生产静态文件构建：

```bash
cd frontend
npm install
npm run build
```

可以将 `frontend/dist` 部署到 Nginx，并将 `/api` 反向代理到后端 `http://localhost:8080`。

## 浏览器全屏 Kiosk 模式

Chromium / Chrome 示例：

```bash
chromium-browser --kiosk http://localhost:5173/kiosk
```

如果系统命令是 `chromium`：

```bash
chromium --kiosk http://localhost:5173/kiosk
```

如果系统命令是 `google-chrome`：

```bash
google-chrome --kiosk http://localhost:5173/kiosk
```

## 一键启动脚本

项目提供：

```bash
bash scripts/start-kiosk-linux.sh
```

脚本会检查 Node/npm，检查后端健康状态，安装前端依赖并启动 Vite，然后输出 kiosk 访问地址和浏览器全屏命令。

## 常见问题

- 后端连接失败：先检查 PostgreSQL 是否启动，再检查 `backend/src/main/resources/application.yml` 数据库地址、用户名、密码。
- 前端接口 404：确认 Vite 代理或 Nginx `/api` 反向代理指向 `http://localhost:8080`。
- 中文显示异常：确认 Linux 终端、数据库和浏览器均使用 UTF-8。
- 浏览器无法全屏：确认已安装 Chromium/Chrome，并使用 `--kiosk` 参数启动。
- 触摸屏输入不方便：可启用系统屏幕键盘，或外接扫码/读卡设备后填入住院号、手机号、身份证后四位。
