# 患者微信小程序 / patient-miniapp 本地演示说明

## 当前状态

`patient-miniapp` 是本地演示版，不是正式上线版。当前目标是让患者端可以在 H5 和微信开发者工具中演示登录、查询账单、查看报告、查看手术安排、查看出院状态和提交预约。

正式医院上线还需要补充真实 AppID、HTTPS 接口域名、微信合法域名配置、隐私协议、实名认证、微信审核、正式登录绑定和安全风控。

## Demo 登录信息

- 住院号：`ZY-DEMO-002`
- 患者编号：`P-DEMO-002`
- 手机号：`13900000002`
- 身份证后四位：`0002`

登录页已提供“填入演示患者”按钮。使用前请先重置演示数据：

```powershell
.\reset-main-flow.ps1
```

## 启动后端

在项目根目录运行：

```powershell
.\start-backend.ps1
```

后端默认地址：

```text
http://localhost:8080
```

健康检查：

```text
http://localhost:8080/api/health
```

## H5 运行

```powershell
cd patient-miniapp
npm install
npm run dev:h5
```

H5 开发环境默认使用 `/api`，由 `manifest.json` 代理到 `http://localhost:8080`。

如需手动指定接口地址：

```powershell
$env:VITE_API_BASE="http://localhost:8080/api"
npm run dev:h5
```

## H5 构建

```powershell
cd patient-miniapp
npm run build:h5
```

构建目录：

```text
patient-miniapp/dist/build/h5
```

## 微信小程序开发运行

```powershell
cd patient-miniapp
npm run dev:mp-weixin
```

开发产物目录通常为：

```text
patient-miniapp/dist/dev/mp-weixin
```

在微信开发者工具中选择“导入项目”，项目目录选择上面的 `mp-weixin` 目录。

## 微信小程序构建

```powershell
cd patient-miniapp
npm run build:mp-weixin
```

构建目录：

```text
patient-miniapp/dist/build/mp-weixin
```

在微信开发者工具中选择“导入项目”，项目目录选择 `patient-miniapp/dist/build/mp-weixin`。

## 微信开发者工具设置

本地演示阶段使用 `http://localhost:8080/api` 或电脑局域网 IP 地址访问后端。请在微信开发者工具中打开：

```text
详情 -> 本地设置 -> 不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书
```

正式上线不能依赖该选项，必须配置 HTTPS 域名和微信 request 合法域名。

## API 地址配置

统一配置文件：

```text
patient-miniapp/src/utils/config.js
```

默认规则：

- H5：`/api`
- 微信小程序：`http://localhost:8080/api`

如果手机预览无法访问电脑的 `localhost`，请改用电脑局域网 IP，例如：

```powershell
$env:VITE_API_BASE="http://192.168.1.10:8080/api"
npm run build:mp-weixin
```

对应电脑和手机需要在同一局域网，防火墙需要允许访问 8080 端口。

## 接口和存储兼容

- 请求统一封装在 `patient-miniapp/src/utils/api.js`
- 跨端请求使用 `uni.request`
- 未使用 axios
- 患者登录会话使用 `uni.setStorageSync` / `uni.getStorageSync`
- 小程序端只访问 `/api/patient-mobile/**` 患者接口

## 可演示功能

- 患者绑定登录
- 查询住院信息
- 查询账单汇总
- 查询费用明细
- 查询检查检验报告
- 查询手术安排
- 查询出院状态
- 新增预约
- 取消预约

小程序新增预约后，HIS 管理端的“患者预约管理”和首页预约统计可查看对应预约数据，管理端可确认、完成、取消预约。

## 常见问题

### 登录失败

先执行：

```powershell
.\reset-main-flow.ps1
```

确认输入：

- 住院号：`ZY-DEMO-002`
- 手机号：`13900000002`
- 身份证后四位：`0002`

### 接口失败

检查后端是否启动：

```text
http://localhost:8080/api/health
```

如果 H5 正常但小程序失败，检查微信开发者工具是否勾选“不校验合法域名”。

### localhost 问题

微信开发者工具本机运行可使用 `localhost`。真机预览时，手机里的 `localhost` 指的是手机自身，不是电脑。真机预览请使用电脑局域网 IP，例如 `http://192.168.1.10:8080/api`。

### 手机访问失败

确认电脑和手机在同一网络，关闭或放行防火墙 8080 端口，并使用局域网 IP 重新构建小程序。

## 正式上线需补充

- 真实微信小程序 AppID
- HTTPS 后端域名
- 微信 request 合法域名
- 隐私协议和用户授权说明
- 实名认证或医院就诊人绑定
- 生产级身份校验、访问频控和审计日志
- 微信审核材料
