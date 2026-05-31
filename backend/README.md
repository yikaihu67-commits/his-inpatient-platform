# Backend

住院 HIS 后端服务，使用 Java 21、Maven、Spring Boot 3、PostgreSQL、MyBatis。

## 启动

先创建数据库：

```powershell
psql -U postgres -c "CREATE DATABASE his_inpatient;"
```

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

健康检查：

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
```

PowerShell 建议先设置 UTF-8：

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

数据库初始化脚本：`sql/init.sql`。应用启动时会自动执行。

## 费用账单接口

状态值：

- sourceType：`ORDER`、`DRUG_DISPENSE`、`BED`、`MANUAL`、`OTHER`
- itemCategory：`DRUG`、`EXAM`、`LAB`、`TREATMENT`、`BED`、`NURSING`、`OTHER`
- status：`UNSETTLED`、`SETTLED`、`CANCELLED`

接口：

- `POST /api/fees` 新增费用
- `GET /api/fees` 分页查询费用，支持 `admissionId`、`patientId`、`itemName`、`itemCategory`、`status`
- `GET /api/fees/{id}` 查询费用详情
- `PUT /api/fees/{id}` 修改费用
- `DELETE /api/fees/{id}` 逻辑删除费用
- `POST /api/fees/{id}/cancel` 取消费用
- `GET /api/fees/admission/{admissionId}/summary` 查询某次住院费用汇总

手工新增费用示例：

```powershell
$body = @{
  admissionId = 1
  patientId = 1
  sourceType = "BED"
  itemCode = "BED001"
  itemName = "床位费"
  itemCategory = "BED"
  quantity = 1
  unit = "日"
  unitPrice = 80.00
  feeTime = "2026-05-28 12:00:00"
  remark = "手工床位费"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/fees" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

## 预交金接口

状态值：

- paymentMethod：`CASH`、`WECHAT`、`ALIPAY`、`BANK_CARD`、`MEDICAL_INSURANCE`、`OTHER`
- transactionType：`PAY`、`REFUND`
- status：`SUCCESS`、`CANCELLED`

接口：

- `POST /api/deposits/pay` 缴纳预交金
- `POST /api/deposits/refund` 退预交金
- `GET /api/deposits` 分页查询，支持 `admissionId`、`patientId`、`transactionType`、`status`
- `GET /api/deposits/{id}` 查询详情
- `POST /api/deposits/{id}/cancel` 取消预交金记录
- `GET /api/deposits/admission/{admissionId}/summary` 查询某次住院预交金汇总

缴纳预交金示例：

```powershell
$body = @{
  admissionId = 1
  patientId = 1
  amount = 500.00
  paymentMethod = "CASH"
  operatorName = "收费员"
  remark = "住院押金"
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/deposits/pay" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

## 住院账户汇总接口

```powershell
Invoke-RestMethod "http://localhost:8080/api/inpatient-accounts/1/summary"
```

## 入院状态接口

- `POST /api/admissions/{id}/admit` 办理入院：`DRAFT`、`REGISTERED` -> `IN_HOSPITAL`
- `POST /api/admissions/{id}/cancel` 取消入院：仅 `DRAFT`、`REGISTERED` 可取消

取消入院前会校验是否已分配床位、是否已有医嘱、是否已有费用。

## 出院结算闭环

`POST /api/discharges/{id}/settle` 执行结算时会重新计算费用、预交金、退费、余额和欠费。结算成功后：

- 出院结算状态变为 `SETTLED`
- 入院记录状态自动变为 `DISCHARGED`
- 入院记录写入 `dischargeTime`
- 当前占用床位自动释放，床位状态变为 `AVAILABLE`
- `bed.currentAdmissionId` 清空

## 主流程回归测试

在项目根目录执行：

```powershell
.\test-main-flow.ps1
```

返回字段包括：

- `totalFeeAmount` 总费用，统计未取消费用
- `totalDepositAmount` 成功预交总额
- `totalRefundAmount` 成功退费总额
- `depositBalance` 预交金余额
- `unpaidAmount` 欠费金额
- `availableBalance` 可用余额

## 完整流程测试

启动后端后执行下面脚本，覆盖患者、入院、床位、医嘱、发药自动计费、手工费用、预交金、退费、账户汇总、重复取消费用 400、超额退费 400。

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"

function PostJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 10
  Invoke-RestMethod -Uri "$base$path" -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}

function GetApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Get
}

function Expect400($scriptBlock) {
  try {
    & $scriptBlock
    throw "Expected HTTP 400 but request succeeded"
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 400) {
      return 400
    }
    throw
  }
}

$suffix = Get-Date -Format "HHmmssfff"

$patient = PostJson "/api/patients" @{
  patientNo = "PFEE$suffix"
  name = "费用测试"
  gender = "MALE"
  idCard = "110101199901$suffix"
  phone = "135$suffix"
  birthDate = "1999-01-01"
  address = "北京市海淀区"
}
$patientId = $patient.data.id

$admission = PostJson "/api/admissions" @{
  patientId = $patientId
  departmentName = "内科"
  wardName = "费用病区"
  admissionTime = "2026-05-28 09:00:00"
  admissionDiagnosis = "费用流程测试"
  chargeType = "医保"
  nursingLevel = "二级护理"
  doctorName = "测试医生"
  status = "REGISTERED"
}
$admissionId = $admission.data.id

$bed = PostJson "/api/beds" @{
  bedNo = "F$suffix"
  wardName = "费用病区"
  roomNo = "701"
  bedType = "普通床"
  status = "EMPTY"
}
PostJson "/api/beds/$($bed.data.id)/assign" @{ admissionId = $admissionId } | Out-Null

$order = PostJson "/api/orders" @{
  admissionId = $admissionId
  patientId = $patientId
  orderType = "LONG_TERM"
  orderCategory = "DRUG"
  itemName = "阿莫西林胶囊"
  dosage = "0.25"
  dosageUnit = "g"
  frequency = "每日三次"
  route = "口服"
  startTime = "2026-05-28 10:00:00"
  doctorName = "测试医生"
  remark = "费用流程药品医嘱"
}
$orderId = $order.data.id
PostJson "/api/orders/$orderId/submit" @{} | Out-Null
PostJson "/api/orders/$orderId/check" @{ nurseName = "核对护士"; remark = "已核对" } | Out-Null

$drug = PostJson "/api/drugs" @{
  drugCode = "DR$suffix"
  drugName = "阿莫西林胶囊"
  specification = "0.25g*24粒"
  unit = "盒"
  price = 15.50
  stockQuantity = 10
  status = "ENABLED"
}

$dispense = PostJson "/api/pharmacy/dispenses" @{
  orderId = $orderId
  drugId = $drug.data.id
  quantity = 2
  pharmacistName = "药师A"
  remark = "发药计费测试"
}
PostJson "/api/pharmacy/dispenses/$($dispense.data.id)/dispense" @{ pharmacistName = "药师A"; remark = "窗口发药" } | Out-Null

$drugFeeList = GetApi "/api/fees?admissionId=$admissionId&itemCategory=DRUG&page=1&pageSize=10"
if ($drugFeeList.data.total -lt 1) { throw "Auto drug fee was not created" }

$manualFee = PostJson "/api/fees" @{
  admissionId = $admissionId
  patientId = $patientId
  sourceType = "BED"
  itemCode = "BED001"
  itemName = "床位费"
  itemCategory = "BED"
  quantity = 1
  unit = "日"
  unitPrice = 80.00
  feeTime = "2026-05-28 12:00:00"
  remark = "手工床位费"
}

$feeSummary = GetApi "/api/fees/admission/$admissionId/summary"
$depositPay = PostJson "/api/deposits/pay" @{
  admissionId = $admissionId
  patientId = $patientId
  amount = 500.00
  paymentMethod = "CASH"
  operatorName = "收费员"
  remark = "住院押金"
}
$depositSummary1 = GetApi "/api/deposits/admission/$admissionId/summary"
$accountSummary1 = GetApi "/api/inpatient-accounts/$admissionId/summary"

$refund = PostJson "/api/deposits/refund" @{
  admissionId = $admissionId
  patientId = $patientId
  amount = 100.00
  paymentMethod = "CASH"
  operatorName = "收费员"
  remark = "退预交金"
}
$accountSummary2 = GetApi "/api/inpatient-accounts/$admissionId/summary"

PostJson "/api/fees/$($manualFee.data.id)/cancel" @{} | Out-Null
$repeatCancelStatus = Expect400 { PostJson "/api/fees/$($manualFee.data.id)/cancel" @{} | Out-Null }
$overRefundStatus = Expect400 { PostJson "/api/deposits/refund" @{ admissionId = $admissionId; patientId = $patientId; amount = 999999.00; paymentMethod = "CASH"; operatorName = "收费员"; remark = "超额退费" } | Out-Null }

[pscustomobject]@{
  patientId = $patientId
  admissionId = $admissionId
  bedId = $bed.data.id
  orderId = $orderId
  drugId = $drug.data.id
  dispenseId = $dispense.data.id
  autoDrugFeeCount = $drugFeeList.data.total
  manualFeeId = $manualFee.data.id
  totalFeeAmount = $feeSummary.data.totalFeeAmount
  depositBalanceBeforeRefund = $depositSummary1.data.balance
  unpaidBeforeRefund = $accountSummary1.data.unpaidAmount
  depositBalanceAfterRefund = $accountSummary2.data.depositBalance
  repeatCancelStatus = $repeatCancelStatus
  overRefundStatus = $overRefundStatus
} | Format-List
```

## 科室/人员/基础字典接口

科室：

- `POST /api/departments` 新增科室
- `GET /api/departments` 分页查询，支持 `deptCode`、`deptName`、`deptType`、`status`
- `GET /api/departments/{id}` 查询详情
- `PUT /api/departments/{id}` 修改科室
- `DELETE /api/departments/{id}` 逻辑删除科室
- `POST /api/departments/{id}/enable` 启用科室
- `POST /api/departments/{id}/disable` 停用科室

人员：

- `POST /api/staff` 新增人员
- `GET /api/staff` 分页查询，支持 `staffNo`、`staffName`、`departmentId`、`roleType`、`status`
- `GET /api/staff/{id}` 查询详情
- `PUT /api/staff/{id}` 修改人员
- `DELETE /api/staff/{id}` 逻辑删除人员
- `POST /api/staff/{id}/enable` 启用人员
- `POST /api/staff/{id}/disable` 停用人员

字典：

- `POST /api/dict-items` 新增字典项
- `GET /api/dict-items` 分页查询，支持 `dictType`、`dictCode`、`dictName`、`status`
- `GET /api/dict-items/{id}` 查询详情
- `PUT /api/dict-items/{id}` 修改字典项
- `DELETE /api/dict-items/{id}` 逻辑删除字典项
- `GET /api/dict-items/type/{dictType}` 查询指定类型的启用字典项

操作日志：

- `GET /api/operation-logs` 分页查询，支持 `moduleName`、`operationType`、`operatorName`、`businessId`、`resultStatus`
- `GET /api/operation-logs/{id}` 查询详情
- `DELETE /api/operation-logs/{id}` 删除日志

基础支撑模块完整 PowerShell 测试：

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"

function PostJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 10
  Invoke-RestMethod -Uri "$base$path" -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function PutJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 10
  Invoke-RestMethod -Uri "$base$path" -Method Put -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function GetApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Get
}
function DeleteApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Delete
}
function Expect400($scriptBlock) {
  try {
    & $scriptBlock
    throw "Expected HTTP 400 but request succeeded"
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 400) {
      return 400
    }
    throw
  }
}

$suffix = Get-Date -Format "HHmmssfff"

$deptIn = PostJson "/api/departments" @{
  deptCode = "DIN$suffix"
  deptName = "住院内科"
  deptType = "INPATIENT"
  status = "ENABLED"
  remark = "住院科室"
}

$deptTech = PostJson "/api/departments" @{
  deptCode = "DTECH$suffix"
  deptName = "检验科"
  deptType = "MEDICAL_TECH"
  status = "ENABLED"
  remark = "医技科室"
}

$deptList = GetApi "/api/departments?page=1&pageSize=10&deptName=科"

PutJson "/api/departments/$($deptIn.data.id)" @{
  deptCode = $deptIn.data.deptCode
  deptName = "住院内科修改"
  deptType = "INPATIENT"
  status = "ENABLED"
  remark = "已修改"
} | Out-Null

PostJson "/api/departments/$($deptIn.data.id)/disable" @{} | Out-Null

$staffOnDisabledStatus = Expect400 {
  PostJson "/api/staff" @{
    staffNo = "SFAIL$suffix"
    staffName = "停用科室医生"
    gender = "MALE"
    phone = "131$suffix"
    departmentId = $deptIn.data.id
    roleType = "DOCTOR"
    title = "主治医师"
    status = "ENABLED"
    remark = "应失败"
  } | Out-Null
}

PostJson "/api/departments/$($deptIn.data.id)/enable" @{} | Out-Null

$doctor = PostJson "/api/staff" @{
  staffNo = "DOC$suffix"
  staffName = "张医生"
  gender = "MALE"
  phone = "131$suffix"
  departmentId = $deptIn.data.id
  roleType = "DOCTOR"
  title = "主治医师"
  status = "ENABLED"
  remark = "医生"
}

$nurse = PostJson "/api/staff" @{
  staffNo = "NUR$suffix"
  staffName = "李护士"
  gender = "FEMALE"
  phone = "130$suffix"
  departmentId = $deptIn.data.id
  roleType = "NURSE"
  title = "护师"
  status = "ENABLED"
  remark = "护士"
}

$staffList = GetApi "/api/staff?page=1&pageSize=10&departmentId=$($deptIn.data.id)"
PostJson "/api/staff/$($doctor.data.id)/disable" @{} | Out-Null
$doctorEnabled = PostJson "/api/staff/$($doctor.data.id)/enable" @{}

$dict = PostJson "/api/dict-items" @{
  dictType = "charge_type_$suffix"
  dictCode = "SELF_PAY"
  dictName = "自费"
  sortOrder = 1
  status = "ENABLED"
  remark = "测试字典"
}

$enabledDict = GetApi "/api/dict-items/type/$($dict.data.dictType)"

$duplicateDictStatus = Expect400 {
  PostJson "/api/dict-items" @{
    dictType = $dict.data.dictType
    dictCode = "SELF_PAY"
    dictName = "重复自费"
    sortOrder = 2
    status = "ENABLED"
    remark = "应失败"
  } | Out-Null
}

Start-Sleep -Milliseconds 300
$logList = GetApi "/api/operation-logs?page=1&pageSize=20&resultStatus=SUCCESS"
$logId = $logList.data.records[0].id
$logDetail = GetApi "/api/operation-logs/$logId"
$deleteLog = DeleteApi "/api/operation-logs/$logId"
$deleteDict = DeleteApi "/api/dict-items/$($dict.data.id)"

[pscustomobject]@{
  inpatientDeptId = $deptIn.data.id
  medTechDeptId = $deptTech.data.id
  departmentListTotal = $deptList.data.total
  staffOnDisabledDeptStatus = $staffOnDisabledStatus
  doctorId = $doctor.data.id
  nurseId = $nurse.data.id
  staffListTotal = $staffList.data.total
  doctorStatusAfterEnable = $doctorEnabled.data.status
  dictId = $dict.data.id
  enabledDictCount = $enabledDict.data.Count
  duplicateDictStatus = $duplicateDictStatus
  operationLogTotal = $logList.data.total
  operationLogDetailId = $logDetail.data.id
  deleteLogSuccess = $deleteLog.success
  deleteDictSuccess = $deleteDict.success
} | Format-List
```

## 检查检验申请接口

状态值：

- requestType：`EXAM`、`LAB`
- itemCategory：`IMAGING`、`ULTRASOUND`、`ECG`、`BLOOD`、`URINE`、`BIOCHEMISTRY`、`OTHER`
- status：`DRAFT`、`SUBMITTED`、`SCHEDULED`、`COMPLETED`、`CANCELLED`

接口：

- `POST /api/exam-lab-requests` 新增检查检验申请
- `GET /api/exam-lab-requests` 分页查询，支持 `admissionId`、`patientId`、`requestType`、`itemCategory`、`status`、`itemName`
- `GET /api/exam-lab-requests/{id}` 查询详情
- `PUT /api/exam-lab-requests/{id}` 修改草稿申请
- `DELETE /api/exam-lab-requests/{id}` 删除草稿申请
- `POST /api/exam-lab-requests/{id}/submit` 提交申请
- `POST /api/exam-lab-requests/{id}/schedule` 预约检查/检验
- `POST /api/exam-lab-requests/{id}/complete` 完成并录入结果
- `POST /api/exam-lab-requests/{id}/cancel` 取消申请

完成检查/检验时可在请求体传 `unitPrice`，后端会自动生成一条费用账单；不传时单价为 `0`。

## 病历/病程记录接口

状态值：

- recordType：`ADMISSION_RECORD`、`PROGRESS_NOTE`、`OPERATION_RECORD`、`DISCHARGE_SUMMARY`、`CONSULTATION_RECORD`、`OTHER`
- status：`DRAFT`、`SUBMITTED`、`ARCHIVED`、`CANCELLED`

接口：

- `POST /api/medical-records` 新增病历记录
- `GET /api/medical-records` 分页查询，支持 `admissionId`、`patientId`、`recordType`、`status`、`doctorName`、`title`
- `GET /api/medical-records/{id}` 查询详情
- `PUT /api/medical-records/{id}` 修改草稿病历
- `DELETE /api/medical-records/{id}` 删除草稿病历
- `POST /api/medical-records/{id}/submit` 提交病历
- `POST /api/medical-records/{id}/archive` 归档病历
- `POST /api/medical-records/{id}/cancel` 作废病历

检查检验和病历完整 PowerShell 测试：

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"

function PostJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 12
  Invoke-RestMethod -Uri "$base$path" -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function PutJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 12
  Invoke-RestMethod -Uri "$base$path" -Method Put -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function GetApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Get
}
function DeleteApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Delete
}
function Expect400($scriptBlock) {
  try {
    & $scriptBlock
    throw "Expected HTTP 400 but request succeeded"
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 400) {
      return 400
    }
    throw
  }
}

$suffix = Get-Date -Format "HHmmssfff"

$patient = PostJson "/api/patients" @{
  patientNo = "PEMR$suffix"
  name = "检查病历测试"
  gender = "MALE"
  idCard = "110101198805$suffix"
  phone = "132$suffix"
  birthDate = "1988-05-05"
  address = "北京市海淀区"
}
$patientId = $patient.data.id

$admission = PostJson "/api/admissions" @{
  patientId = $patientId
  departmentName = "内科"
  wardName = "综合病区"
  admissionTime = "2026-05-28 09:00:00"
  admissionDiagnosis = "检查病历流程测试"
  chargeType = "医保"
  nursingLevel = "二级护理"
  doctorName = "主管医生"
  status = "REGISTERED"
}
$admissionId = $admission.data.id

$bed = PostJson "/api/beds" @{
  bedNo = "EM$suffix"
  wardName = "综合病区"
  roomNo = "901"
  bedType = "普通床"
  status = "EMPTY"
}
PostJson "/api/beds/$($bed.data.id)/assign" @{ admissionId = $admissionId } | Out-Null

$exam = PostJson "/api/exam-lab-requests" @{
  admissionId = $admissionId
  patientId = $patientId
  requestType = "EXAM"
  itemCode = "CT001"
  itemName = "胸部CT"
  itemCategory = "IMAGING"
  doctorName = "主管医生"
  requestTime = "2026-05-28 10:00:00"
  remark = "检查申请"
}
$examId = $exam.data.id
PostJson "/api/exam-lab-requests/$examId/submit" @{ remark = "提交检查" } | Out-Null
PostJson "/api/exam-lab-requests/$examId/schedule" @{ scheduledTime = "2026-05-28 11:00:00"; remark = "预约检查" } | Out-Null
$examDone = PostJson "/api/exam-lab-requests/$examId/complete" @{
  resultSummary = "未见明显异常"
  resultDetail = "胸部CT检查未见明显异常。"
  unitPrice = 120.00
  remark = "完成检查"
}
GetApi "/api/exam-lab-requests/$examId" | Out-Null
$cancelCompletedExamStatus = Expect400 {
  PostJson "/api/exam-lab-requests/$examId/cancel" @{ remark = "尝试取消已完成检查" } | Out-Null
}

$lab = PostJson "/api/exam-lab-requests" @{
  admissionId = $admissionId
  patientId = $patientId
  requestType = "LAB"
  itemCode = "LAB001"
  itemName = "血常规"
  itemCategory = "BLOOD"
  doctorName = "主管医生"
  requestTime = "2026-05-28 10:10:00"
  remark = "检验申请"
}
$labId = $lab.data.id
PostJson "/api/exam-lab-requests/$labId/submit" @{ remark = "提交检验" } | Out-Null
$labDone = PostJson "/api/exam-lab-requests/$labId/complete" @{
  resultSummary = "白细胞正常"
  resultDetail = "血常规结果基本正常。"
  unitPrice = 35.00
  remark = "完成检验"
}

$feeList = GetApi "/api/fees?admissionId=$admissionId&page=1&pageSize=20"

$record = PostJson "/api/medical-records" @{
  admissionId = $admissionId
  patientId = $patientId
  recordType = "ADMISSION_RECORD"
  title = "入院记录"
  content = "患者因发热入院，完善检查。"
  doctorName = "主管医生"
  recordTime = "2026-05-28 12:00:00"
  remark = "草稿"
}
$recordId = $record.data.id
$recordNo = $record.data.recordNo

PutJson "/api/medical-records/$recordId" @{
  recordNo = $recordNo
  admissionId = $admissionId
  patientId = $patientId
  recordType = "ADMISSION_RECORD"
  title = "入院记录修改"
  content = "患者因发热入院，已完善检查检验。"
  doctorName = "主管医生"
  recordTime = "2026-05-28 12:30:00"
  remark = "已修改草稿"
} | Out-Null

PostJson "/api/medical-records/$recordId/submit" @{ remark = "提交病历" } | Out-Null
$archived = PostJson "/api/medical-records/$recordId/archive" @{ remark = "归档病历" }

$modifyArchivedStatus = Expect400 {
  PutJson "/api/medical-records/$recordId" @{
    recordNo = $recordNo
    admissionId = $admissionId
    patientId = $patientId
    recordType = "ADMISSION_RECORD"
    title = "归档后修改"
    content = "不允许修改"
    doctorName = "主管医生"
    recordTime = "2026-05-28 13:00:00"
    remark = "应失败"
  } | Out-Null
}

$progress = PostJson "/api/medical-records" @{
  admissionId = $admissionId
  patientId = $patientId
  recordType = "PROGRESS_NOTE"
  title = "首次病程记录"
  content = "患者生命体征平稳，继续观察。"
  doctorName = "主管医生"
  recordTime = "2026-05-28 14:00:00"
  remark = "病程草稿"
}

$list = GetApi "/api/medical-records?admissionId=$admissionId&page=1&pageSize=10"

$draft = PostJson "/api/medical-records" @{
  admissionId = $admissionId
  patientId = $patientId
  recordType = "OTHER"
  title = "临时草稿"
  content = "待删除草稿。"
  doctorName = "主管医生"
  recordTime = "2026-05-28 15:00:00"
  remark = "删除测试"
}
DeleteApi "/api/medical-records/$($draft.data.id)" | Out-Null

[pscustomobject]@{
  admissionId = $admissionId
  examId = $examId
  examStatus = $examDone.data.status
  cancelCompletedExamStatus = $cancelCompletedExamStatus
  labId = $labId
  labStatus = $labDone.data.status
  feeCount = $feeList.data.total
  archivedRecordId = $recordId
  archivedStatus = $archived.data.status
  modifyArchivedStatus = $modifyArchivedStatus
  medicalRecordTotal = $list.data.total
  deletedDraftRecordId = $draft.data.id
} | Format-List
```

## 出院结算接口

状态值：

- `DRAFT` 草稿
- `SETTLED` 已结算
- `CANCELLED` 已取消

接口：

- `POST /api/discharges/create` 创建出院结算
- `GET /api/discharges` 分页查询，支持 `dischargeNo`、`admissionId`、`patientId`、`status`
- `GET /api/discharges/{id}` 查询详情
- `PUT /api/discharges/{id}` 修改草稿结算
- `DELETE /api/discharges/{id}` 逻辑删除未结算记录
- `POST /api/discharges/{id}/settle` 执行结算
- `POST /api/discharges/{id}/cancel` 取消草稿结算

创建出院结算时，后端会按当前住院账户自动计算：

- `totalFeeAmount` = 未取消费用总额
- `totalDepositAmount` = 成功预交总额
- `totalRefundAmount` = 成功退费总额
- `depositBalance` = 预交金余额
- `unpaidAmount` = 欠费金额
- `actualPayment` = 本次需要补交金额

执行结算时，后端会把未结算费用置为已结算；如果有欠费，会自动生成一条预交金补交记录；如果有余额，会自动生成一条退费记录；入院记录状态会更新为 `SETTLED`。

出院结算完整 PowerShell 测试：

```powershell
chcp 65001
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"

function PostJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 10
  Invoke-RestMethod -Uri "$base$path" -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function PutJson($path, $obj) {
  $json = $obj | ConvertTo-Json -Depth 10
  Invoke-RestMethod -Uri "$base$path" -Method Put -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}
function GetApi($path) {
  Invoke-RestMethod -Uri "$base$path" -Method Get
}
function Expect400($scriptBlock) {
  try {
    & $scriptBlock
    throw "Expected HTTP 400 but request succeeded"
  } catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 400) {
      return 400
    }
    throw
  }
}

$suffix = Get-Date -Format "HHmmssfff"

$patient = PostJson "/api/patients" @{
  patientNo = "PDIS$suffix"
  name = "出院测试"
  gender = "MALE"
  idCard = "110101198801$suffix"
  phone = "134$suffix"
  birthDate = "1988-01-01"
  address = "北京市海淀区"
}
$patientId = $patient.data.id

$admission = PostJson "/api/admissions" @{
  patientId = $patientId
  departmentName = "内科"
  wardName = "出院病区"
  admissionTime = "2026-05-28 09:00:00"
  admissionDiagnosis = "出院结算流程测试"
  chargeType = "医保"
  nursingLevel = "二级护理"
  doctorName = "结算医生"
  status = "REGISTERED"
}
$admissionId = $admission.data.id

$bed = PostJson "/api/beds" @{
  bedNo = "D$suffix"
  wardName = "出院病区"
  roomNo = "801"
  bedType = "普通床"
  status = "EMPTY"
}
PostJson "/api/beds/$($bed.data.id)/assign" @{ admissionId = $admissionId } | Out-Null

$order = PostJson "/api/orders" @{
  admissionId = $admissionId
  patientId = $patientId
  orderType = "LONG_TERM"
  orderCategory = "DRUG"
  itemName = "头孢克肟胶囊"
  dosage = "0.1"
  dosageUnit = "g"
  frequency = "每日两次"
  route = "口服"
  startTime = "2026-05-28 10:00:00"
  doctorName = "结算医生"
  remark = "出院流程药品医嘱"
}
$orderId = $order.data.id
PostJson "/api/orders/$orderId/submit" @{} | Out-Null
PostJson "/api/orders/$orderId/check" @{ nurseName = "核对护士"; remark = "已核对" } | Out-Null

$execution = PostJson "/api/nursing/executions" @{
  orderId = $orderId
  scheduledTime = "2026-05-28 10:30:00"
  nurseName = "执行护士"
  remark = "待执行"
}
PostJson "/api/nursing/executions/$($execution.data.id)/execute" @{ nurseName = "执行护士"; result = "执行成功"; remark = "已执行" } | Out-Null

$drug = PostJson "/api/drugs" @{
  drugCode = "DISDR$suffix"
  drugName = "头孢克肟胶囊"
  specification = "0.1g*12粒"
  unit = "盒"
  price = 30.00
  stockQuantity = 10
  status = "ENABLED"
}

$dispense = PostJson "/api/pharmacy/dispenses" @{
  orderId = $orderId
  drugId = $drug.data.id
  quantity = 2
  pharmacistName = "药师A"
  remark = "出院发药计费测试"
}
PostJson "/api/pharmacy/dispenses/$($dispense.data.id)/dispense" @{ pharmacistName = "药师A"; remark = "窗口发药" } | Out-Null

$manualFee = PostJson "/api/fees" @{
  admissionId = $admissionId
  patientId = $patientId
  sourceType = "MANUAL"
  itemCode = "TR001"
  itemName = "治疗费"
  itemCategory = "TREATMENT"
  quantity = 1
  unit = "次"
  unitPrice = 80.00
  feeTime = "2026-05-28 12:00:00"
  remark = "手工治疗费"
}

$deposit = PostJson "/api/deposits/pay" @{
  admissionId = $admissionId
  patientId = $patientId
  amount = 100.00
  paymentMethod = "CASH"
  operatorName = "收费员"
  remark = "住院押金"
}

$accountBefore = GetApi "/api/inpatient-accounts/$admissionId/summary"
if ([decimal]$accountBefore.data.totalFeeAmount -ne 140.00) { throw "totalFeeAmount mismatch" }
if ([decimal]$accountBefore.data.depositBalance -ne 100.00) { throw "depositBalance mismatch" }
if ([decimal]$accountBefore.data.unpaidAmount -ne 40.00) { throw "unpaidAmount mismatch" }

$discharge = PostJson "/api/discharges/create" @{
  admissionId = $admissionId
  patientId = $patientId
  dischargeTime = "2026-05-28 15:00:00"
  operatorName = "结算员"
  remark = "创建出院结算"
}
$dischargeId = $discharge.data.id
GetApi "/api/discharges/$dischargeId" | Out-Null

$settled = PostJson "/api/discharges/$dischargeId/settle" @{
  actualPayment = 40.00
  paymentMethod = "CASH"
  operatorName = "结算员"
  remark = "执行出院结算"
}

$modifySettledStatus = Expect400 {
  PutJson "/api/discharges/$dischargeId" @{
    admissionId = $admissionId
    patientId = $patientId
    dischargeTime = "2026-05-28 16:00:00"
    operatorName = "结算员"
    remark = "尝试修改已结算"
  } | Out-Null
}

$accountAfter = GetApi "/api/inpatient-accounts/$admissionId/summary"
if ([decimal]$accountAfter.data.unpaidAmount -ne 0.00) { throw "account should be balanced after settlement" }

$patient2 = PostJson "/api/patients" @{
  patientNo = "PCAN$suffix"
  name = "取消结算测试"
  gender = "FEMALE"
  idCard = "110101199902$suffix"
  phone = "133$suffix"
  birthDate = "1999-02-02"
  address = "北京市朝阳区"
}
$admission2 = PostJson "/api/admissions" @{
  patientId = $patient2.data.id
  departmentName = "外科"
  wardName = "取消病区"
  admissionTime = "2026-05-28 10:00:00"
  admissionDiagnosis = "取消结算流程测试"
  chargeType = "自费"
  nursingLevel = "三级护理"
  doctorName = "取消医生"
  status = "REGISTERED"
}
$draftCancel = PostJson "/api/discharges/create" @{
  admissionId = $admission2.data.id
  patientId = $patient2.data.id
  dischargeTime = "2026-05-28 16:00:00"
  operatorName = "结算员"
  remark = "待取消草稿"
}
$cancelled = PostJson "/api/discharges/$($draftCancel.data.id)/cancel" @{
  operatorName = "结算员"
  remark = "取消草稿"
}

[pscustomobject]@{
  admissionId = $admissionId
  dischargeId = $dischargeId
  totalFeeAmount = $discharge.data.totalFeeAmount
  totalDepositAmount = $discharge.data.totalDepositAmount
  unpaidAmount = $discharge.data.unpaidAmount
  actualPayment = $settled.data.actualPayment
  modifySettledStatus = $modifySettledStatus
  accountUnpaidAfterSettle = $accountAfter.data.unpaidAmount
  cancelledDraftStatus = $cancelled.data.status
} | Format-List
```
