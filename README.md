# 住院 HIS 管理平台

本项目是一个用于毕业设计演示的住院 HIS 管理系统，包含 Spring Boot 后端和 Vue 3 前端。系统覆盖患者建档、入院登记、床位分配、医嘱管理、护士执行、药房发药、费用账单、预交金、出院结算，以及检查检验、病历记录、手术管理和基础配置模块。

## 技术栈

- 后端：Spring Boot 3、Java 21、Maven、MyBatis、PostgreSQL、Validation、Lombok
- 前端：Vue 3、Vite、Element Plus、Axios、Vue Router
- 数据库：PostgreSQL，数据库编码要求为 `UTF8`

## 后端模块

- 患者管理
- 入院登记
- 床位管理
- 医嘱管理
- 护士医嘱执行
- 药品管理
- 药房发药
- 费用账单
- 预交金/住院押金
- 出院结算
- 检查检验申请
- 病历/病程记录
- 手术管理
- 科室管理
- 人员管理
- 基础字典
- 操作日志/审计日志

## 前端页面

- 首页工作台
- 患者管理、入院登记、床位管理
- 医嘱管理、护士执行
- 药品管理、药房发药
- 费用账单、预交金管理、出院结算
- 检查检验、病历记录、手术管理
- 科室管理、人员管理、基础字典、操作日志

## 推荐启动顺序

```powershell
.\build-all.ps1
.\reset-demo-data.ps1
.\start-backend.ps1
.\start-frontend.ps1
```

说明：

- `build-all.ps1` 会先构建后端，再构建前端；任意一步失败都会停止。
- `reset-demo-data.ps1` 会清空演示业务数据并插入一组标准中文演示数据，保留表结构。
- `start-backend.ps1` 启动前会自动检测 8080 端口。如果 8080 被占用，脚本会输出占用进程 PID，并尝试自动结束该进程后再启动后端。
- `start-frontend.ps1` 会自动进入 `frontend` 目录；如果 `node_modules` 不存在会自动执行 `npm install`；如果 5173 被占用，会提示并尝试释放端口。

## 手动启动

启动 PostgreSQL，并确认数据库存在：

```powershell
psql -U postgres -c "CREATE DATABASE his_inpatient ENCODING 'UTF8';"
```

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问：

```text
http://localhost:5173
```

前端 Vite 代理已配置：`/api -> http://localhost:8080`。

## 演示主流程

患者建档 -> 入院登记 -> 床位分配 -> 医嘱管理 -> 护士执行 -> 药房发药 -> 费用账单 -> 预交金 -> 出院结算

检查检验、病历和手术模块可作为住院期间的补充业务演示；科室、人员、基础字典和操作日志用于系统支撑配置。

首页工作台已补充答辩演示入口：

- 统计卡片：今日在院人数、可用床位、今日入院、今日出院、今日费用、待核对医嘱、待结算患者。
- 主流程导航卡片：患者管理、入院登记、床位分配、医嘱管理、费用管理、出院结算，可直接跳转到对应页面。
- 右上角角色演示：管理员、入院登记员、护士、医生、药房人员、收费员；当前为前端菜单级演示控制，后端保留 RBAC 扩展空间。
- 首页“重置演示数据”按钮会调用 `POST /api/demo/reset`，用于恢复标准中文演示数据。该接口默认仅作为开发演示入口使用，可通过配置 `demo.reset.enabled=false` 关闭。

## P0 主流程接口说明

入院登记已补充专用业务接口：

- `POST /api/admissions/{id}/admit`：办理入院，状态从 `DRAFT` 或 `REGISTERED` 变为 `IN_HOSPITAL`。
- `POST /api/admissions/{id}/cancel`：取消入院，仅 `DRAFT`、`REGISTERED` 可取消；已分配床位、已有医嘱或费用时返回明确业务错误。

出院结算已补齐业务闭环：

- `POST /api/discharges/{id}/settle`：执行结算时重新计算费用、预交金、退费、余额和欠费。
- 结算成功后，出院结算状态变为 `SETTLED`。
- 对应入院记录状态自动变为 `DISCHARGED`，并写入 `dischargeTime`。
- 当前占用床位会自动释放，床位状态变为 `AVAILABLE`，`currentAdmissionId` 清空。

操作日志已通过统一拦截器覆盖主要写操作，包括患者、入院、床位、医嘱、护士执行、药品、药房发药、费用、预交金、出院结算、检查检验、病历、科室、人员、字典等模块。

## 手术模块接口说明

手术模块支持申请、安排、开始、完成、计费和取消，状态流转为：

```text
APPLIED 已申请 -> SCHEDULED 已安排 -> IN_PROGRESS 手术中 -> COMPLETED 已完成 -> BILLED 已计费
APPLIED / SCHEDULED 可取消为 CANCELLED
```

接口清单：

- `GET /api/surgeries`：分页查询手术列表，支持患者、住院记录、手术编号、手术名称、状态筛选。
- `GET /api/surgeries/patient/{patientId}`：查询某患者手术列表。
- `GET /api/surgeries/admission/{admissionId}`：查询某次入院手术列表。
- `GET /api/surgeries/{id}`：查询手术详情。
- `POST /api/surgeries`：新增手术申请。
- `PUT /api/surgeries/{id}`：修改已申请或已安排的手术。
- `DELETE /api/surgeries/{id}`：删除已申请手术。
- `POST /api/surgeries/{id}/schedule`：安排手术。
- `POST /api/surgeries/{id}/start`：开始手术。
- `POST /api/surgeries/{id}/complete`：完成手术。
- `POST /api/surgeries/{id}/bill`：生成手术费用，费用来源为 `SURGERY`，费用类别为 `SURGERY`，自动进入收费模块。
- `POST /api/surgeries/{id}/cancel`：取消已申请或已安排手术。

演示数据重置后会包含 4 条手术记录：已申请、已安排、已完成、已计费各类状态，并包含 1 条手术费用记录。医生站会展示当前患者的手术申请与记录，手术管理页面可完成状态流转和费用生成。

主流程回归测试：

```powershell
.\test-main-flow.ps1
```

脚本会完整覆盖：创建患者、创建入院登记、办理入院、创建并分配床位、创建和核对 DRUG 医嘱、护士执行、药品和发药、费用生成、预交金、账户汇总、出院结算、结算后入院状态和床位释放校验、操作日志校验。

## 重置演示数据

旧患者 `address` 显示乱码属于历史错误编码数据，不是当前接口或前端编码问题。当前新建中文地址已经验证正常。

如需清理旧乱码测试数据并恢复标准中文演示数据，运行：

```powershell
.\reset-demo-data.ps1
```

脚本会要求输入 `RESET` 确认。执行后会插入：

- 患者：李四，地址：江苏省南京市玄武区
- 科室：住院内科
- 人员：王医生、赵护士
- 床位：一病区 101 / B-DEMO-001
- 药品：阿莫西林胶囊
- 入院记录：ZY-DEMO-001，状态在院
- 医嘱：YZ-DEMO-001，状态已核对
- 费用：药品费用、床位费
- 手术：已申请、已安排、已完成、已计费记录各一条，并包含手术费用
- 预交金：500 元现金预交

如果 `reset-demo-data.ps1` 报 PowerShell 语法错误，说明运行的不是当前修复后的最新版脚本，请先更新脚本后再执行。

## 中文乱码处理

后端已配置 UTF-8：

- `server.servlet.encoding.charset=UTF-8`
- `server.servlet.encoding.enabled=true`
- `server.servlet.encoding.force=true`
- `server.tomcat.uri-encoding=UTF-8`
- `spring.sql.init.encoding=UTF-8`
- `spring.messages.encoding=UTF-8`
- `logging.charset.console=UTF-8`
- `logging.charset.file=UTF-8`
- PostgreSQL 连接使用 `client_encoding=UTF8`

PowerShell 调用接口前建议执行：

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

请求 JSON 使用 UTF-8 字节发送：

```powershell
$body = @{
  patientNo = "PUTF8TEST001"
  name = "中文测试"
  gender = "MALE"
  idCard = "320102199001010001"
  phone = "13900000001"
  birthDate = "1990-01-01"
  address = "江苏省南京市玄武区"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/patients" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

## 常见问题

### 后端 8080 端口占用

直接运行：

```powershell
.\start-backend.ps1
```

脚本会自动检测并尝试释放 8080 端口，然后启动后端。

### 前端 5173 端口占用

直接运行：

```powershell
.\start-frontend.ps1
```

脚本会提示端口占用并尝试释放；Vite 也可能自动选择可用备用端口。

### 数据库连接失败

确认 PostgreSQL 已启动，并检查 `backend/src/main/resources/application.yml`：

- 数据库：`his_inpatient`
- 用户名：`postgres`
- 密码：`123456`

### 前端接口 404 或跨域

确认后端已启动在 `http://localhost:8080`，前端通过 Vite 代理访问 `/api`。代理配置文件为 `frontend/vite.config.js`。
