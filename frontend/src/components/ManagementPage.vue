<template>
  <div class="page">
    <div class="toolbar">
      <div>
        <h2 style="margin: 0">{{ title }}</h2>
        <div v-if="subtitle" class="muted">{{ subtitle }}</div>
      </div>
      <div class="toolbar-actions">
        <el-button
          v-for="button in headerActions"
          :key="button.label"
          :type="button.type || 'primary'"
          :disabled="!canOperateKey(button.key)"
          @click="runHeaderAction(button)"
        >
          {{ button.label }}
        </el-button>
        <el-button v-if="!hideCreate" type="primary" :disabled="!canOperateKey('create')" @click="openCreate">新增</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="query" label-width="88px">
        <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
          <component
            :is="inputComponent(field)"
            v-model="query[field.prop]"
            clearable
            :placeholder="placeholder(field)"
            :value-format="field.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
            :filterable="field.type === 'select'"
            :allow-create="field.allowCreate || false"
            :disabled="field.disabled || false"
            default-first-option
            style="width: 180px"
            @change="handleQueryChange(field)"
          >
            <el-option v-for="opt in fieldOptions(field)" :key="opt.value" :label="optionLabel(opt)" :value="opt.value" />
          </component>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-alert
      v-if="errorMessage"
      class="page-alert"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />

    <div class="table-panel">
      <el-table
        :key="tableKey"
        :data="records"
        border
        stripe
        v-loading="loading"
        height="calc(100vh - 330px)"
        empty-text="暂无数据"
      >
        <el-table-column type="index" width="54" label="#" />
        <el-table-column
          v-for="col in tableColumns"
          :key="columnKey(col)"
          :prop="col.prop"
          :label="col.label"
          :min-width="col.minWidth || 110"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <el-tag v-if="isStatusColumn(col)" :type="statusType(getCellValue(row, col))" size="small">
              {{ enumLabel(getCellValue(row, col)) }}
            </el-tag>
            <span v-else>{{ formatCell(getCellValue(row, col), col) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="330">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button v-if="canShow('edit', row)" link type="primary" :disabled="!canOperateKey('edit')" @click="openEdit(row)">编辑</el-button>
              <template v-for="action in rowActions" :key="action.label">
                <el-button
                  v-if="canShow(action, row)"
                  link
                  :type="action.type || 'primary'"
                  :disabled="!canOperateKey(action.key)"
                  @click="runRowAction(action, row)"
                >
                  {{ action.label }}
                </el-button>
              </template>
              <el-button v-if="canShow('delete', row)" link type="danger" :disabled="!canOperateKey('delete')" @click="removeRow(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          @change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="formVisible" :title="editingId ? `编辑${title}` : `新增${title}`" width="680px" destroy-on-close>
      <el-form :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col v-for="field in formFields" :key="field.prop" :span="field.span || 12">
            <el-form-item :label="field.label">
              <component
                :is="inputComponent(field)"
                v-model="form[field.prop]"
                clearable
                :type="field.inputType"
                :value-format="field.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
                :rows="field.rows || 3"
                :placeholder="placeholder(field)"
                :filterable="field.type === 'select'"
                :allow-create="field.allowCreate || false"
                :disabled="field.disabled || false"
                default-first-option
                style="width: 100%"
                @change="handleFormChange(field)"
              >
                <el-option v-for="opt in fieldOptions(field)" :key="opt.value" :label="optionLabel(opt)" :value="opt.value" />
              </component>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="actionVisible" :title="actionTitle" width="520px" destroy-on-close>
      <el-form :model="actionForm" label-width="110px">
        <el-form-item v-for="field in actionFields" :key="field.prop" :label="field.label">
          <component
            :is="inputComponent(field)"
            v-model="actionForm[field.prop]"
            clearable
            :type="field.inputType"
            :value-format="field.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
            :rows="field.rows || 3"
            :placeholder="placeholder(field)"
            :filterable="field.type === 'select'"
            :allow-create="field.allowCreate || false"
            :disabled="field.disabled || false"
            default-first-option
            style="width: 100%"
            @change="handleActionFormChange(field)"
          >
            <el-option v-for="opt in fieldOptions(field)" :key="opt.value" :label="optionLabel(opt)" :value="opt.value" />
          </component>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAction">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="详情" size="620px">
      <template v-if="detailSections.length">
        <section v-for="section in detailSections" :key="section.title" class="detail-section">
          <h3>{{ section.title }}</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item v-for="col in section.fields" :key="columnKey(col)" :label="col.label">
              {{ formatCell(getCellValue(detail, col), col) }}
            </el-descriptions-item>
          </el-descriptions>
        </section>
      </template>
      <el-descriptions v-else :column="1" border>
        <el-descriptions-item v-for="col in resolvedDetailColumns" :key="columnKey(col)" :label="col.label">
          {{ formatCell(getCellValue(detail, col), col) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { canUseAction, isKnownAction } from '../permissions'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  api: { type: Object, required: true },
  searchFields: { type: Array, default: () => [] },
  tableColumns: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] },
  rowActions: { type: Array, default: () => [] },
  headerActions: { type: Array, default: () => [] },
  hideCreate: { type: Boolean, default: false },
  hideEdit: { type: Boolean, default: false },
  hideDelete: { type: Boolean, default: false },
  canEdit: { type: Function, default: null },
  canDelete: { type: Function, default: null },
  detailFields: { type: Array, default: () => [] },
  detailSections: { type: Array, default: () => [] },
  detailLoader: { type: Function, default: null },
  onFormChange: { type: Function, default: null },
  onActionFormChange: { type: Function, default: null },
  onQueryChange: { type: Function, default: null }
})

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const records = ref([])
const total = ref(0)
const tableKey = ref(0)
const errorMessage = ref('')
const query = reactive({ page: 1, pageSize: 10 })
const form = reactive({})
const detail = ref({})
const editingId = ref(null)
const formVisible = ref(false)
const detailVisible = ref(false)
const actionVisible = ref(false)
const actionTitle = ref('')
const actionFields = ref([])
const actionForm = reactive({})
const currentRole = ref(localStorage.getItem('demoRole') || 'ADMIN')
let pendingAction = null
let pendingRow = null

const detailColumns = computed(() => props.tableColumns.concat(props.formFields).filter((item, index, arr) =>
  item.prop && arr.findIndex((i) => i.prop === item.prop) === index
))

const resolvedDetailColumns = computed(() => props.detailFields.length ? props.detailFields : detailColumns.value)

const enumLabels = {
  MALE: '男',
  FEMALE: '女',
  ACTIVE: '正常',
  ENABLED: '启用',
  DISABLED: '停用',
  DRAFT: '草稿',
  REGISTERED: '已登记',
  IN_HOSPITAL: '在院',
  DISCHARGE_PENDING: '待出院',
  DISCHARGED: '已出院',
  DELETED: '已删除',
  CANCELLED: '已取消',
  SETTLED: '已结算',
  REQUESTED: '已申请',
  CONFIRMED: '已确认',
  REPORTED: '已出报告',
  PENDING_SETTLEMENT: '待结算',
  DISCHARGE_REQUESTED: '已申请出院',
  EMPTY: '空床',
  AVAILABLE: '可用',
  OCCUPIED: '占用',
  RESERVED: '预留',
  LONG_TERM: '长期医嘱',
  TEMPORARY: '临时医嘱',
  DRUG: '药品',
  EXAM: '检查',
  LAB: '检验',
  TREATMENT: '治疗',
  CARE: '护理',
  DIET: '饮食',
  OTHER: '其他',
  SURGERY: '手术费',
  DRUG_RETURN: '退药冲正',
  SUBMITTED: '已提交',
  CHECKED: '已核对',
  EXECUTED: '已执行',
  STOPPED: '已停止',
  PENDING: '待执行',
  FAILED: '失败',
  CREATED: '已创建',
  DISPENSED: '已发药',
  RETURNED: '已退药',
  UNSETTLED: '未结算',
  SUCCESS: '成功',
  PAY: '预交',
  REFUND: '退费',
  CASH: '现金',
  WECHAT: '微信',
  ALIPAY: '支付宝',
  BANK_CARD: '银行卡',
  MEDICAL_INSURANCE: '医保',
  EXAM_APPOINTMENT: '检查预约',
  LAB_APPOINTMENT: '检验预约',
  DISCHARGE_SETTLEMENT: '出院结算预约',
  FOLLOW_UP: '复诊预约',
  COMPLETED: '已完成',
  SCHEDULED: '已预约',
  APPLIED: '已申请',
  IN_PROGRESS: '手术中',
  BILLED: '已计费',
  ARCHIVED: '已归档',
  REVIEWED: '已审核',
  ADMISSION_RECORD: '入院记录',
  FIRST_COURSE_RECORD: '首次病程记录',
  DAILY_COURSE_RECORD: '日常病程记录',
  SUPERIOR_ROUND_RECORD: '上级医师查房记录',
  DISCHARGE_RECORD: '出院记录',
  NURSING_RECORD: '护理记录',
  PROGRESS_NOTE: '病程记录',
  OPERATION_RECORD: '手术记录',
  DISCHARGE_SUMMARY: '出院小结',
  CONSULTATION_RECORD: '会诊记录',
  OUTPATIENT: '门诊科室',
  INPATIENT: '住院科室',
  MEDICAL_TECH: '医技科室',
  PHARMACY: '药房',
  ADMIN: '行政/管理员',
  DOCTOR: '医生',
  NURSE: '护士',
  PHARMACIST: '药师',
  TECHNICIAN: '技师',
  CASHIER: '收费员',
  SURGERY_MANAGER: '手术室管理员',
  IMAGING: '影像',
  ULTRASOUND: '超声',
  ECG: '心电',
  BLOOD: '血液',
  URINE: '尿液',
  BIOCHEMISTRY: '生化',
  CREATE: '新增',
  UPDATE: '修改',
  DELETE: '删除',
  QUERY: '查询',
  SUBMIT: '提交',
  CHECK: '核对',
  EXECUTE: '执行',
  CANCEL: '取消',
  SETTLE: '结算',
  ENABLE: '启用',
  DISABLE: '停用'
}

const defaultAliases = {
  patientNo: ['patientNo', 'patientCode', 'patientNumber'],
  name: ['name', 'patientName'],
  gender: ['gender', 'sex'],
  phone: ['phone', 'mobile', 'phoneNumber'],
  idCard: ['idCard', 'idCardNo', 'identityNo'],
  status: ['status', 'patientStatus', 'bedStatus', 'drugStatus', 'feeStatus', 'dischargeStatus'],
  admissionNo: ['admissionNo', 'visitNo'],
  bedNo: ['bedNo', 'bedCode', 'bedNumber'],
  wardName: ['wardName', 'ward'],
  roomNo: ['roomNo', 'roomNumber'],
  bedType: ['bedType', 'type'],
  orderNo: ['orderNo', 'medicalOrderNo'],
  orderType: ['orderType', 'type'],
  orderCategory: ['orderCategory', 'category'],
  drugCode: ['drugCode', 'drugNo', 'drugNumber'],
  drugName: ['drugName', 'name'],
  specification: ['specification', 'spec'],
  stockQuantity: ['stockQuantity', 'stock', 'inventory'],
  price: ['price', 'unitPrice'],
  feeNo: ['feeNo', 'billNo'],
  totalAmount: ['totalAmount', 'amount'],
  depositNo: ['depositNo', 'paymentNo'],
  dischargeNo: ['dischargeNo', 'settlementNo'],
  totalFeeAmount: ['totalFeeAmount', 'totalAmount'],
  depositBalance: ['depositBalance', 'balance'],
  unpaidAmount: ['unpaidAmount', 'arrearsAmount'],
  actualPayment: ['actualPayment', 'paymentAmount'],
  requestNo: ['requestNo', 'examLabNo'],
  recordNo: ['recordNo', 'medicalRecordNo'],
  deptCode: ['deptCode', 'departmentCode'],
  deptName: ['deptName', 'departmentName'],
  staffNo: ['staffNo', 'employeeNo'],
  staffName: ['staffName', 'name'],
  dictType: ['dictType', 'type'],
  dictCode: ['dictCode', 'code'],
  dictName: ['dictName', 'name'],
  moduleName: ['moduleName', 'module'],
  operationType: ['operationType', 'type'],
  operatorName: ['operatorName', 'operator'],
  requestUri: ['requestUri', 'uri', 'path'],
  resultStatus: ['resultStatus', 'status'],
  operationTime: ['operationTime', 'createdAt'],
  surgeryNo: ['surgeryNo', 'operationNo'],
  surgeryName: ['surgeryName', 'operationName'],
  surgeryFee: ['surgeryFee', 'totalAmount']
}

function inputComponent(field) {
  if (field.type === 'select') return 'el-select'
  if (field.type === 'textarea') return 'el-input'
  if (field.type === 'number') return 'el-input-number'
  if (field.type === 'datetime' || field.type === 'date') return 'el-date-picker'
  return 'el-input'
}

function placeholder(field) {
  if (field.placeholder) return field.placeholder
  return `${field.type === 'select' ? '请选择' : '请输入'}${field.label}`
}

function columnKey(col) {
  return [props.title, col.prop, col.label].filter(Boolean).join('-')
}

function toSnakeCase(value) {
  return String(value || '').replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`)
}

function toCamelCase(value) {
  return String(value || '').replace(/_([a-z])/g, (_, char) => char.toUpperCase())
}

function fieldCandidates(col = {}) {
  const names = [col.prop, ...(col.fields || []), ...(col.aliases || []), ...(defaultAliases[col.prop] || [])].filter(Boolean)
  return [...new Set(names.flatMap((name) => [name, toSnakeCase(name), toCamelCase(name)]))]
}

function getCellValue(row, col = {}) {
  if (!row) return undefined
  for (const field of fieldCandidates(col)) {
    const value = row[field]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return col.prop ? row[col.prop] : undefined
}

function normalizePageData(raw) {
  const payload = raw?.data && typeof raw.data === 'object' ? raw.data : raw
  const page = payload?.data && typeof payload.data === 'object' ? payload.data : payload
  const rows = Array.isArray(page)
    ? page
    : page?.records || page?.list || page?.content || page?.rows || page?.items || []
  return {
    rows: Array.isArray(rows) ? rows.map((row) => ({ ...row })) : [],
    total: page?.total ?? page?.totalElements ?? page?.count ?? page?.totalCount ?? (Array.isArray(rows) ? rows.length : 0),
    page: page?.page ?? page?.current,
    pageSize: page?.pageSize ?? page?.size
  }
}

async function loadData() {
  loading.value = true
  try {
    const raw = await props.api.list({ ...query })
    const pageData = normalizePageData(raw)
    records.value = pageData.rows
    total.value = Number(pageData.total) || 0
    errorMessage.value = ''
    if (pageData.page && pageData.page !== query.page) query.page = pageData.page
    if (pageData.pageSize && pageData.pageSize !== query.pageSize) query.pageSize = pageData.pageSize
    tableKey.value += 1
  } catch {
    records.value = []
    total.value = 0
    errorMessage.value = '数据加载失败，请检查后端服务或稍后重试'
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.keys(query).forEach((key) => {
    if (!['page', 'pageSize'].includes(key)) delete query[key]
  })
  query.page = 1
  loadData()
}

function clearReactive(target) {
  Object.keys(target).forEach((key) => delete target[key])
}

function buildPayload(source, fields) {
  const payload = {}
  fields.forEach((field) => {
    if (!field.virtual && source[field.prop] !== undefined && source[field.prop] !== '') {
      payload[field.prop] = source[field.prop]
    }
  })
  return payload
}

function handleFormChange(field) {
  if (props.onFormChange) props.onFormChange(field, form)
}

function handleActionFormChange(field) {
  if (props.onActionFormChange) props.onActionFormChange(field, actionForm, pendingAction)
}

function handleQueryChange(field) {
  if (props.onQueryChange) props.onQueryChange(field, query)
}

function openCreate() {
  if (!canOperateKey('create')) {
    permissionMessage()
    return
  }
  editingId.value = null
  clearReactive(form)
  props.formFields.forEach((field) => {
    if (field.default !== undefined) form[field.prop] = field.default
  })
  if (props.onFormChange) props.onFormChange({ prop: null }, form)
  formVisible.value = true
}

function openEdit(row) {
  if (!canOperateKey('edit')) {
    permissionMessage()
    return
  }
  editingId.value = row.id
  clearReactive(form)
  props.formFields.forEach((field) => {
    form[field.prop] = getCellValue(row, field) ?? field.default ?? ''
  })
  if (props.onFormChange) props.onFormChange({ prop: null }, form)
  formVisible.value = true
}

function openDetail(row) {
  detail.value = { ...row }
  detailVisible.value = true
  if (props.api.detail) {
    props.api.detail(row.id)
      .then(async (data) => {
        const baseDetail = data ? { ...data } : { ...row }
        if (props.detailLoader) {
          const extraDetail = await props.detailLoader(baseDetail).catch(() => ({}))
          detail.value = { ...baseDetail, ...extraDetail }
        } else {
          detail.value = baseDetail
        }
      })
      .catch(() => {
        ElMessage.warning('详情加载失败，已显示列表数据')
      })
  }
}

async function saveForm() {
  if (!canOperateKey(editingId.value ? 'edit' : 'create')) {
    permissionMessage()
    return
  }
  if (!validateForm()) return
  saving.value = true
  try {
    if (editingId.value) {
      await props.api.update(editingId.value, buildPayload(form, props.formFields))
      ElMessage.success('修改成功')
    } else {
      await props.api.create(buildPayload(form, props.formFields))
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

function validateForm() {
  for (const field of props.formFields) {
    const value = form[field.prop]
    if (field.required && (value === undefined || value === null || value === '')) {
      ElMessage.warning(`请填写${field.label}`)
      return false
    }
    if (field.type === 'number' && field.min !== undefined && value !== undefined && value !== null && value < field.min) {
      ElMessage.warning(`${field.label}不能小于 ${field.min}`)
      return false
    }
    if ((field.type === 'date' || field.valueFormat === 'YYYY-MM-DD') && value && !/^\d{4}-\d{2}-\d{2}$/.test(String(value))) {
      ElMessage.warning(`${field.label}格式应为 YYYY-MM-DD`)
      return false
    }
    if (field.pattern && value) {
      const regex = field.pattern instanceof RegExp ? field.pattern : new RegExp(field.pattern)
      if (!regex.test(String(value))) {
        ElMessage.warning(field.message || `${field.label}格式不正确`)
        return false
      }
    }
    if (typeof field.validator === 'function') {
      const message = field.validator(value, form)
      if (message) {
        ElMessage.warning(message)
        return false
      }
    }
  }
  return true
}

function handleRoleChange(event) {
  currentRole.value = event.detail || localStorage.getItem('demoRole') || 'ADMIN'
}

function canOperateKey(actionKey) {
  if (!actionKey || !isKnownAction(actionKey)) return true
  return canUseAction(currentRole.value, route.path, actionKey)
}

function permissionMessage() {
  ElMessage.warning('当前角色无权限执行该操作')
}

function canShow(action, row) {
  if (action === 'delete') return !props.hideDelete && (props.canDelete ? props.canDelete(row) : true)
  if (action === 'edit') return !props.hideEdit && (props.canEdit ? props.canEdit(row) : true)
  if (typeof action.show === 'function') return action.show(row)
  return true
}

function isStatusColumn(col) {
  return col.tag || col.prop === 'status' || col.prop === 'resultStatus'
}

function enumLabel(value) {
  if (value === null || value === undefined || value === '') return '-'
  return enumLabels[value] || value
}

function optionLabel(opt) {
  return opt.label && opt.label !== opt.value ? opt.label : enumLabel(opt.value)
}

function fieldOptions(field) {
  const options = field.options
  if (!options) return []
  if (Array.isArray(options)) return options
  if (Array.isArray(options.value)) return options.value
  if (typeof options === 'function') return options()
  return []
}

function statusType(value) {
  if (['ENABLED', 'ACTIVE', 'SUCCESS', 'SETTLED', 'COMPLETED', 'ARCHIVED', 'REVIEWED', 'EXECUTED', 'IN_HOSPITAL', 'AVAILABLE', 'BILLED', 'REPORTED', 'CONFIRMED'].includes(value)) return 'success'
  if (['DISABLED', 'CANCELLED', 'STOPPED', 'FAILED', 'DELETED'].includes(value)) return 'danger'
  if (['DRAFT', 'PENDING', 'REGISTERED', 'CREATED', 'UNSETTLED', 'EMPTY', 'APPLIED', 'REQUESTED', 'PENDING_SETTLEMENT', 'DISCHARGE_REQUESTED'].includes(value)) return 'warning'
  if (['SUBMITTED', 'SCHEDULED', 'CHECKED', 'DISPENSED', 'OCCUPIED', 'IN_PROGRESS'].includes(value)) return 'primary'
  return 'info'
}

function formatCell(value, col = {}) {
  if (value === null || value === undefined || value === '') return '-'
  if (col.format === 'money' || /(amount|price|payment|balance|fee)/i.test(col.prop || '')) {
    const number = Number(value)
    return Number.isFinite(number) ? number.toFixed(2) : value
  }
  if (col.format === 'time' || /(time|date|createdAt|updatedAt)/i.test(col.prop || '')) {
    return String(value).replace('T', ' ').slice(0, 19)
  }
  if (enumLabels[value]) return enumLabels[value]
  return value
}

async function runRowAction(action, row) {
  if (!canOperateKey(action.key)) {
    permissionMessage()
    return
  }
  if (action.fields?.length) {
    clearReactive(actionForm)
    action.fields.forEach((field) => {
      actionForm[field.prop] = field.default ?? ''
    })
    pendingAction = action
    pendingRow = row
    actionTitle.value = action.label
    actionFields.value = action.fields
    actionVisible.value = true
    if (props.onActionFormChange) props.onActionFormChange({ prop: null }, actionForm, pendingAction)
    return
  }
  await doAction(action, row, {})
}

async function submitAction() {
  if (!pendingAction || !pendingRow) return
  if (!canOperateKey(pendingAction.key)) {
    permissionMessage()
    return
  }
  await doAction(pendingAction, pendingRow, buildPayload(actionForm, pendingAction.fields || []))
  actionVisible.value = false
}

async function doAction(action, row, payload) {
  if (!canOperateKey(action.key)) {
    permissionMessage()
    return
  }
  saving.value = true
  try {
    if (action.header) {
      await props.api[action.key](payload)
    } else {
      await props.api[action.key](row.id, payload, row)
    }
    ElMessage.success(action.success || '操作成功')
    await loadData()
  } finally {
    saving.value = false
  }
}

async function runHeaderAction(action) {
  if (!canOperateKey(action.key)) {
    permissionMessage()
    return
  }
  if (action.fields?.length) {
    clearReactive(actionForm)
    action.fields.forEach((field) => {
      actionForm[field.prop] = field.default ?? ''
    })
    pendingAction = { key: action.key, label: action.label, success: action.success, header: true }
    pendingRow = { id: null }
    actionTitle.value = action.label
    actionFields.value = action.fields
    actionVisible.value = true
    if (props.onActionFormChange) props.onActionFormChange({ prop: null }, actionForm, pendingAction)
    return
  }
  await props.api[action.key]()
  await loadData()
}

async function removeRow(row) {
  if (!canOperateKey('delete')) {
    permissionMessage()
    return
  }
  await ElMessageBox.confirm('确认删除当前记录？', '提示', { type: 'warning' })
  await props.api.remove(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

function initDefaultQuery() {
  props.searchFields.forEach((field) => {
    if (field.default !== undefined) query[field.prop] = field.default
  })
}

onMounted(() => {
  window.addEventListener('demo-role-change', handleRoleChange)
  initDefaultQuery()
  loadData()
})

onUnmounted(() => {
  window.removeEventListener('demo-role-change', handleRoleChange)
})

watch(
  () => props.title,
  () => {
    records.value = []
    total.value = 0
    errorMessage.value = ''
    tableKey.value += 1
    resetQuery()
  }
)
</script>

<style scoped>
.toolbar-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.row-actions :deep(.el-button) {
  margin-left: 0;
}

.page-alert {
  margin-bottom: 12px;
}

.detail-section {
  margin-bottom: 18px;
}

.detail-section h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
