<template>
  <div class="station-page">
    <div class="toolbar">
      <div>
        <h2>医生站</h2>
        <div class="muted">面向医生演示：选择在院患者，查看入院/床位信息，开立、提交、停止和作废医嘱。</div>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" :disabled="!selectedAdmission" @click="openOrderDialog">新增医嘱</el-button>
        <el-button type="primary" :disabled="!selectedAdmission" @click="openRecordDialog()">新增病历</el-button>
        <el-button type="success" :disabled="!selectedAdmission" @click="goSurgeryPage">手术申请</el-button>
      </div>
    </div>

    <el-row :gutter="12">
      <el-col :span="7">
        <section class="station-panel">
          <div class="panel-title">当前在院患者</div>
          <el-input v-model="keyword" placeholder="搜索患者姓名/住院号" clearable class="mb12" />
          <el-scrollbar height="calc(100vh - 250px)" v-loading="loading">
            <el-empty v-if="filteredAdmissions.length === 0" description="暂无在院患者" />
            <button
              v-for="item in filteredAdmissions"
              :key="item.id"
              type="button"
              class="patient-card"
              :class="{ active: selectedAdmission?.id === item.id }"
              @click="selectAdmission(item)"
            >
              <strong>{{ item.patientName || `患者 ${item.patientId}` }}</strong>
              <span>{{ item.admissionNo }} / {{ item.departmentName }} / {{ item.wardName }}</span>
              <el-tag size="small" type="success">在院</el-tag>
            </button>
          </el-scrollbar>
        </section>
      </el-col>

      <el-col :span="17">
        <section class="station-panel">
          <div class="panel-title">患者信息</div>
          <el-descriptions v-if="selectedAdmission" :column="3" border>
            <el-descriptions-item label="患者">{{ patientInfo.name || selectedAdmission.patientName }}</el-descriptions-item>
            <el-descriptions-item label="患者编号">{{ patientInfo.patientNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ patientInfo.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="住院号">{{ selectedAdmission.admissionNo }}</el-descriptions-item>
            <el-descriptions-item label="入院科室">{{ selectedAdmission.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="病区">{{ selectedAdmission.wardName }}</el-descriptions-item>
            <el-descriptions-item label="当前床位">{{ bedText }}</el-descriptions-item>
            <el-descriptions-item label="诊断">{{ selectedAdmission.admissionDiagnosis || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经管医生">{{ selectedAdmission.doctorName || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择在院患者" />
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">医嘱列表</div>
          <el-table :data="orders" border stripe v-loading="orderLoading" empty-text="暂无医嘱" height="calc(100vh - 470px)">
            <el-table-column prop="orderNo" label="医嘱编号" min-width="150" />
            <el-table-column prop="orderType" label="类型" min-width="100">
              <template #default="{ row }">{{ enumText(row.orderType) }}</template>
            </el-table-column>
            <el-table-column prop="orderCategory" label="分类" min-width="90">
              <template #default="{ row }">{{ enumText(row.orderCategory) }}</template>
            </el-table-column>
            <el-table-column prop="itemName" label="医嘱内容" min-width="180" />
            <el-table-column prop="dosage" label="剂量" />
            <el-table-column prop="route" label="用法" />
            <el-table-column prop="frequency" label="频次" min-width="110" />
            <el-table-column prop="startTime" label="开始时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
            </el-table-column>
            <el-table-column prop="endTime" label="停止时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
            </el-table-column>
            <el-table-column prop="doctorName" label="开立医生" min-width="100" />
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="190">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" :disabled="row.status !== 'DRAFT'" @click="submitOrder(row)">提交</el-button>
                  <el-button link type="warning" :disabled="!['CHECKED', 'EXECUTED'].includes(row.status)" @click="stopOrder(row)">停止</el-button>
                  <el-button link type="danger" :disabled="row.status !== 'SUBMITTED'" @click="cancelOrder(row)">作废</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">病历文书</div>
          <el-table :data="medicalRecords" border stripe v-loading="recordLoading" empty-text="暂无病历记录" height="260px">
            <el-table-column prop="recordNo" label="病历编号" min-width="150" />
            <el-table-column prop="recordType" label="类型" min-width="140">
              <template #default="{ row }">{{ enumText(row.recordType) }}</template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column prop="doctorName" label="记录医生" min-width="100" />
            <el-table-column prop="recordTime" label="记录时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.recordTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }"><el-tag :type="recordStatusType(row.status)">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="230">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" :disabled="row.status !== 'DRAFT'" @click="openRecordDialog(row)">编辑</el-button>
                  <el-button link type="primary" :disabled="row.status !== 'DRAFT'" @click="submitRecord(row)">提交</el-button>
                  <el-button link type="success" :disabled="row.status !== 'SUBMITTED'" @click="reviewRecord(row)">审核</el-button>
                  <el-button link type="warning" :disabled="!['SUBMITTED', 'REVIEWED'].includes(row.status)" @click="archiveRecord(row)">归档</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">手术申请与记录</div>
          <el-table :data="surgeries" border stripe v-loading="surgeryLoading" empty-text="暂无手术记录" height="220px">
            <el-table-column prop="surgeryNo" label="手术编号" min-width="150" />
            <el-table-column prop="surgeryName" label="手术名称" min-width="180" />
            <el-table-column prop="operatingRoom" label="手术室" />
            <el-table-column prop="plannedTime" label="计划时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.plannedTime) }}</template>
            </el-table-column>
            <el-table-column prop="primaryDoctorName" label="主刀医生" min-width="100" />
            <el-table-column prop="surgeryFee" label="费用" min-width="100">
              <template #default="{ row }">{{ money(row.surgeryFee) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }"><el-tag :type="surgeryStatusType(row.status)">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="100">
              <template #default>
                <el-button link type="primary" @click="goSurgeryPage">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-dialog v-model="orderDialogVisible" title="新增医嘱" width="720px" destroy-on-close>
      <el-form :model="orderForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="医嘱类型" required>
              <el-select v-model="orderForm.orderType">
                <el-option v-for="item in orderTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医嘱分类" required>
              <el-select v-model="orderForm.orderCategory">
                <el-option v-for="item in orderCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="医嘱内容" required><el-input v-model="orderForm.itemName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="剂量"><el-input v-model="orderForm.dosage" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="剂量单位"><el-input v-model="orderForm.dosageUnit" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="频次"><el-input v-model="orderForm.frequency" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用法"><el-input v-model="orderForm.route" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开立医生" required><el-input v-model="orderForm.doctorName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开始时间"><el-date-picker v-model="orderForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="停止时间"><el-date-picker v-model="orderForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="orderForm.remark" type="textarea" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createOrder">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" :title="recordForm.id ? '编辑病历' : '新增病历'" width="760px" destroy-on-close>
      <el-form :model="recordForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="病历类型" required>
              <el-select v-model="recordForm.recordType" @change="fillRecordTemplate">
                <el-option v-for="item in medicalRecordTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="记录医生" required><el-input v-model="recordForm.doctorName" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标题" required><el-input v-model="recordForm.title" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="内容" required><el-input v-model="recordForm.content" type="textarea" :rows="10" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="recordForm.remark" type="textarea" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="recordSaving" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import admissionApi from '../api/admission'
import bedApi from '../api/bed'
import medicalRecordApi from '../api/medicalRecord'
import orderApi from '../api/order'
import patientApi from '../api/patient'
import surgeryApi from '../api/surgery'
import { medicalRecordTypeOptions, orderCategoryOptions, orderTypeOptions } from '../options'

const router = useRouter()
const loading = ref(false)
const orderLoading = ref(false)
const recordLoading = ref(false)
const surgeryLoading = ref(false)
const saving = ref(false)
const recordSaving = ref(false)
const keyword = ref('')
const admissions = ref([])
const beds = ref([])
const orders = ref([])
const medicalRecords = ref([])
const surgeries = ref([])
const selectedAdmission = ref(null)
const patientInfo = ref({})
const orderDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const orderForm = reactive({})
const recordForm = reactive({})

const filteredAdmissions = computed(() => {
  const key = keyword.value.trim()
  if (!key) return admissions.value
  return admissions.value.filter((item) => `${item.patientName || ''}${item.admissionNo || ''}`.includes(key))
})

const bedText = computed(() => {
  const bed = beds.value.find((item) => Number(item.currentAdmissionId) === Number(selectedAdmission.value?.id))
  return bed ? `${bed.wardName} ${bed.roomNo}房 ${bed.bedNo}床` : '暂未分配床位'
})

async function loadAdmissions() {
  loading.value = true
  try {
    const [admissionPage, bedPage] = await Promise.all([
      admissionApi.list({ page: 1, pageSize: 100, status: 'IN_HOSPITAL' }),
      bedApi.list({ page: 1, pageSize: 100 })
    ])
    admissions.value = recordsOf(admissionPage)
    beds.value = recordsOf(bedPage)
    if (!selectedAdmission.value && admissions.value.length) {
      await selectAdmission(admissions.value[0])
    }
  } finally {
    loading.value = false
  }
}

async function selectAdmission(admission) {
  selectedAdmission.value = admission
  patientInfo.value = admission.patientId ? await patientApi.detail(admission.patientId).catch(() => ({})) : {}
  await Promise.all([loadOrders(), loadMedicalRecords(), loadSurgeries()])
}

async function loadOrders() {
  if (!selectedAdmission.value) return
  orderLoading.value = true
  try {
    const page = await orderApi.list({ page: 1, pageSize: 100, admissionId: selectedAdmission.value.id })
    orders.value = recordsOf(page)
  } finally {
    orderLoading.value = false
  }
}

async function loadMedicalRecords() {
  if (!selectedAdmission.value) return
  recordLoading.value = true
  try {
    const page = await medicalRecordApi.list({ page: 1, pageSize: 100, admissionId: selectedAdmission.value.id })
    medicalRecords.value = recordsOf(page)
  } finally {
    recordLoading.value = false
  }
}

async function loadSurgeries() {
  if (!selectedAdmission.value) return
  surgeryLoading.value = true
  try {
    const page = await surgeryApi.listByAdmission(selectedAdmission.value.id, { page: 1, pageSize: 100 })
    surgeries.value = recordsOf(page)
  } finally {
    surgeryLoading.value = false
  }
}

function openOrderDialog() {
  Object.assign(orderForm, {
    orderType: 'LONG_TERM',
    orderCategory: 'DRUG',
    itemName: '',
    dosage: '',
    dosageUnit: '',
    frequency: '每日三次',
    route: '口服',
    startTime: '',
    endTime: '',
    doctorName: selectedAdmission.value?.doctorName || '王医生',
    remark: ''
  })
  orderDialogVisible.value = true
}

async function createOrder() {
  if (!selectedAdmission.value) return
  if (!orderForm.itemName || !orderForm.doctorName) {
    ElMessage.warning('医嘱内容和开立医生不能为空')
    return
  }
  saving.value = true
  try {
    await orderApi.create({
      admissionId: selectedAdmission.value.id,
      patientId: selectedAdmission.value.patientId,
      ...orderForm,
      status: 'DRAFT'
    })
    ElMessage.success('医嘱已创建')
    orderDialogVisible.value = false
    await loadOrders()
  } finally {
    saving.value = false
  }
}

async function submitOrder(row) {
  await orderApi.submit(row.id)
  ElMessage.success('医嘱已提交')
  await loadOrders()
}

async function stopOrder(row) {
  await orderApi.stop(row.id)
  ElMessage.success('医嘱已停止')
  await loadOrders()
}

async function cancelOrder(row) {
  await orderApi.cancel(row.id)
  ElMessage.success('医嘱已作废')
  await loadOrders()
}

function openRecordDialog(row = null) {
  Object.keys(recordForm).forEach((key) => delete recordForm[key])
  if (row) {
    Object.assign(recordForm, { ...row })
  } else {
    Object.assign(recordForm, {
      recordType: 'DAILY_COURSE_RECORD',
      title: '日常病程记录',
      content: '',
      doctorName: selectedAdmission.value?.doctorName || '王医生',
      remark: ''
    })
    fillRecordTemplate()
  }
  recordDialogVisible.value = true
}

function fillRecordTemplate() {
  if (!recordForm.title) recordForm.title = recordTitle(recordForm.recordType)
  if (!recordForm.content) recordForm.content = recordTemplate(recordForm.recordType)
}

async function saveRecord() {
  if (!selectedAdmission.value) return
  if (!recordForm.title || !recordForm.content || !recordForm.doctorName) {
    ElMessage.warning('病历标题、内容和记录医生不能为空')
    return
  }
  recordSaving.value = true
  try {
    const payload = {
      admissionId: selectedAdmission.value.id,
      patientId: selectedAdmission.value.patientId,
      recordNo: recordForm.recordNo,
      recordType: recordForm.recordType,
      title: recordForm.title,
      content: recordForm.content,
      doctorName: recordForm.doctorName,
      remark: recordForm.remark,
      status: 'DRAFT'
    }
    if (recordForm.id) {
      await medicalRecordApi.update(recordForm.id, payload)
      ElMessage.success('病历已修改')
    } else {
      await medicalRecordApi.create(payload)
      ElMessage.success('病历已创建')
    }
    recordDialogVisible.value = false
    await loadMedicalRecords()
  } finally {
    recordSaving.value = false
  }
}

async function submitRecord(row) {
  await medicalRecordApi.submit(row.id)
  ElMessage.success('病历已提交')
  await loadMedicalRecords()
}

async function reviewRecord(row) {
  await medicalRecordApi.review(row.id)
  ElMessage.success('病历已审核')
  await loadMedicalRecords()
}

async function archiveRecord(row) {
  await medicalRecordApi.archive(row.id)
  ElMessage.success('病历已归档')
  await loadMedicalRecords()
}

function goSurgeryPage() {
  router.push({
    path: '/app/surgeries',
    query: selectedAdmission.value ? { admissionId: selectedAdmission.value.id, patientId: selectedAdmission.value.patientId } : {}
  })
}

function recordsOf(page) {
  return page?.records || page?.list || page?.content || page?.rows || []
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function enumText(value) {
  const map = {
    LONG_TERM: '长期', TEMPORARY: '临时', DRUG: '药品', EXAM: '检查', LAB: '检验', TREATMENT: '治疗',
    CARE: '护理', DIET: '饮食', OTHER: '其他', DRAFT: '草稿', SUBMITTED: '已提交', CHECKED: '已核对',
    EXECUTED: '已执行', STOPPED: '已停止', CANCELLED: '已作废',
    ADMISSION_RECORD: '入院记录', FIRST_COURSE_RECORD: '首次病程记录', DAILY_COURSE_RECORD: '日常病程记录',
    SUPERIOR_ROUND_RECORD: '上级医师查房记录', DISCHARGE_RECORD: '出院记录', NURSING_RECORD: '护理记录',
    REVIEWED: '已审核', ARCHIVED: '已归档',
    APPLIED: '已申请', SCHEDULED: '已安排', IN_PROGRESS: '手术中', COMPLETED: '已完成', BILLED: '已计费'
  }
  return map[value] || value || '-'
}

function statusType(value) {
  if (['CHECKED', 'EXECUTED'].includes(value)) return 'success'
  if (['STOPPED', 'CANCELLED'].includes(value)) return 'danger'
  if (value === 'SUBMITTED') return 'primary'
  return 'warning'
}

function surgeryStatusType(value) {
  if (['COMPLETED', 'BILLED'].includes(value)) return 'success'
  if (value === 'CANCELLED') return 'danger'
  if (value === 'IN_PROGRESS') return 'primary'
  return 'warning'
}

function recordStatusType(value) {
  if (['REVIEWED', 'ARCHIVED'].includes(value)) return 'success'
  if (value === 'CANCELLED') return 'danger'
  if (value === 'SUBMITTED') return 'primary'
  return 'warning'
}

function recordTitle(type) {
  const map = {
    ADMISSION_RECORD: '入院记录',
    FIRST_COURSE_RECORD: '首次病程记录',
    DAILY_COURSE_RECORD: '日常病程记录',
    SUPERIOR_ROUND_RECORD: '上级医师查房记录',
    DISCHARGE_RECORD: '出院记录',
    NURSING_RECORD: '护理记录'
  }
  return map[type] || '病历记录'
}

function recordTemplate(type) {
  if (type === 'ADMISSION_RECORD') return '主诉：\n现病史：\n既往史：\n查体：\n初步诊断：\n处理意见：'
  if (type === 'DISCHARGE_RECORD') return '入院情况：\n住院经过：\n出院情况：\n出院诊断：\n出院医嘱：'
  if (type === 'NURSING_RECORD') return '护理观察：\n护理措施：\n健康宣教：'
  return '病情变化：\n查体：\n辅助检查：\n诊疗计划：'
}

function money(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(2) : '-'
}

onMounted(loadAdmissions)
</script>
