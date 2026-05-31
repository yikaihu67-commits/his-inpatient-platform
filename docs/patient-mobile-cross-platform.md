# 患者移动端跨平台说明

## 当前实现

当前患者移动端采用 H5 方式集成在现有 Vue 3 前端中，入口为：

```text
http://localhost:5173/mobile
```

主要页面：

- `/mobile/login` 患者绑定/登录
- `/mobile/home` 移动端首页
- `/mobile/bill` 我的账单
- `/mobile/bill-detail` 费用明细
- `/mobile/appointments` 预约服务
- `/mobile/appointments/new` 新增预约
- `/mobile/reports` 检查检验报告
- `/mobile/surgery` 手术安排
- `/mobile/discharge` 出院服务

## 运行与部署

开发运行：

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

生产构建：

```bash
cd frontend
npm run build
```

将 `frontend/dist` 发布到 Nginx 或静态文件服务，并将 `/api` 反向代理到后端服务。

## API 复用

移动端接口统一使用：

```text
/api/patient-mobile/**
```

其中账单、报告、手术、出院状态复用患者自助服务的只读查询能力；预约服务使用 `patient_appointment` 表，后续可以与检查检验排班、出院结算窗口联动。

## 迁移到 UniApp

后续迁移到 UniApp 时建议：

- 保留 `/api/patient-mobile/**` 接口不变
- 将 Vue 页面拆为 `pages/mobile/*`
- 将 `localStorage` 替换为 `uni.setStorageSync / uni.getStorageSync`
- 将 Element Plus 组件替换为 Uni UI 或自定义移动端组件

## 迁移到 Taro

后续迁移到 Taro 时建议：

- 保留移动端 API 请求参数和响应结构
- 将页面拆为 Taro pages
- 将本地绑定信息保存到 `Taro.setStorageSync`
- 使用 Taro UI 或 NutUI 组件重建表单、列表和卡片

## 微信小程序适配建议

- 登录阶段可继续使用住院号、手机号、身份证后四位做轻量绑定
- 后续可接入微信手机号授权，但不建议直接替代住院身份校验
- 报告、账单、预约接口继续走 `/api/patient-mobile/**`
- 注意小程序 request 域名白名单和 HTTPS 证书配置

## 支付宝小程序适配建议

- 使用支付宝小程序本地缓存保存 `sessionKey`
- API 与 H5 保持一致
- 手机号授权可作为辅助身份信息，不作为唯一校验条件

## 移动端和自助机端区别

- 自助机端 `/kiosk`：适合医院大厅或病区固定设备，强调大按钮、全屏展示、只读查询。
- 移动端 `/mobile`：适合患者手机访问，除查询外支持预约服务，后续可扩展为微信/支付宝小程序。
