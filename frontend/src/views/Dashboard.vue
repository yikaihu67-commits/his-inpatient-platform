<template>
  <div class="page dashboard-page">
    <section class="welcome-panel">
      <div>
        <div class="eyebrow">住院管理总览</div>
        <h1>住院 HIS 管理平台</h1>
        <p>
          集中查看在院患者、病历待办、手术排班、费用结算和今日动态，适合医生站、护士站与住院管理演示。
        </p>
      </div>
      <div class="welcome-actions">
        <el-button :loading="loading" @click="loadDashboard">刷新首页</el-button>
        <el-button type="primary" :loading="resetting" @click="resetDemoData">重置演示数据</el-button>
      </div>
    </section>

    <el-alert
      v-if="errorMessage"
      class="mb12"
      type="warning"
      :closable="false"
      :title="errorMessage"
      show-icon
    />

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="summary-grid">
          <el-skeleton-item v-for="index in 8" :key="index" variant="rect" class="skeleton-card" />
        </div>
      </template>
      <template #default>
        <div class="metric-grid">
          <button
            v-for="card in metricCards"
            :key="card.key"
            type="button"
            class="metric-card"
            @click="go(card.path)"
          >
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <em>{{ card.note }}</em>
          </button>
        </div>
      </template>
    </el-skeleton>

    <section class="dashboard-layout">
      <div class="left-column">
        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>手术状态统计</h2>
              <p>手术申请、排班、进行中、完成和待计费状态</p>
            </div>
            <el-button link type="primary" @click="go('/app/surgeries')">进入手术管理</el-button>
          </div>
          <div class="surgery-stats">
            <button v-for="item in surgeryCards" :key="item.status" type="button" @click="go(item.path)">
              <el-tag :type="item.tagType">{{ item.label }}</el-tag>
              <strong>{{ item.value }}</strong>
              <span>{{ item.desc }}</span>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>最近手术</h2>
              <p>展示最近的手术申请、安排、完成和计费记录</p>
            </div>
          </div>
          <el-table :data="recentSurgeries" empty-text="暂无手术数据" size="small">
            <el-table-column prop="patientName" label="患者" min-width="86" />
            <el-table-column prop="surgeryName" label="手术名称" min-width="170" show-overflow-tooltip />
            <el-table-column label="状态" width="98">
              <template #default="{ row }">
                <el-tag :type="surgeryStatusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计划时间" min-width="150">
              <template #default="{ row }">{{ formatTime(row.plannedTime) }}</template>
            </el-table-column>
            <el-table-column prop="primaryDoctorName" label="主刀医生" min-width="90" />
            <el-table-column label="费用" width="96">
              <template #default="{ row }">{{ money(row.surgeryFee) }}</template>
            </el-table-column>
            <el-table-column label="计费" width="82">
              <template #default="{ row }">
                <el-tag :type="row.billed ? 'success' : 'warning'">{{ row.billed ? '已计费' : '待计费' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>主流程导航</h2>
              <p>按顺序完成住院患者演示闭环</p>
            </div>
          </div>
          <div class="quick-flow">
            <button v-for="item in flowEntries" :key="item.path" type="button" @click="go(item.path)">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </button>
          </div>
        </section>
      </div>

      <div class="right-column">
        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>病历待办</h2>
              <p>待审核、草稿和归档状态一眼可见</p>
            </div>
          </div>
          <div class="record-summary">
            <button type="button" @click="go('/app/medical-records?status=SUBMITTED')">
              <span>待审核</span>
              <strong>{{ n(metrics.pendingReviewMedicalRecordCount) }}</strong>
            </button>
            <button type="button" @click="go('/app/medical-records?status=DRAFT')">
              <span>草稿</span>
              <strong>{{ n(metrics.draftMedicalRecordCount) }}</strong>
            </button>
            <button type="button" @click="go('/app/medical-records?status=ARCHIVED')">
              <span>已归档</span>
              <strong>{{ n(metrics.archivedMedicalRecordCount) }}</strong>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>费用概览</h2>
              <p>住院费用、未结算费用和手术费用汇总</p>
            </div>
          </div>
          <div class="fee-overview">
            <div>
              <span>当前住院费用</span>
              <strong>{{ money(metrics.currentInpatientFeeAmount) }}</strong>
            </div>
            <div>
              <span>未结算费用</span>
              <strong>{{ money(metrics.unsettledFeeAmount) }}</strong>
            </div>
            <div>
              <span>已结算费用</span>
              <strong>{{ money(metrics.settledFeeAmount) }}</strong>
            </div>
            <div>
              <span>今日手术费用</span>
              <strong>{{ money(metrics.todaySurgeryFeeAmount) }}</strong>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>待办事项</h2>
              <p>病历、手术、费用和在院患者待处理事项</p>
            </div>
          </div>
          <el-empty v-if="!todos.length" description="暂无待办事项" :image-size="80" />
          <div v-else class="todo-list">
            <button v-for="item in todos" :key="todoKey(item)" type="button" @click="go(item.targetPath)">
              <div>
                <el-tag size="small" :type="todoTag(item.type)">{{ item.type || '待办' }}</el-tag>
                <strong>{{ item.title || '未命名事项' }}</strong>
              </div>
              <span>{{ item.patientName || '未关联患者' }} · {{ item.admissionNo || item.admissionId || '-' }}</span>
              <em>{{ statusText(item.status) }} · {{ formatTime(item.time) }}</em>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>今日动态</h2>
              <p>今日入院、手术和费用记录</p>
            </div>
          </div>
          <el-empty v-if="!todayActivities.length" description="暂无今日动态" :image-size="80" />
          <div v-else class="activity-list">
            <button v-for="item in todayActivities" :key="activityKey(item)" type="button" @click="go(item.targetPath)">
              <span>{{ item.type }}</span>
              <strong>{{ item.title || '-' }}</strong>
              <em>{{ item.patientName || '未关联患者' }} · {{ formatTime(item.time) }}</em>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <div>
              <h2>快捷入口</h2>
              <p>面向答辩演示的核心工作台</p>
            </div>
          </div>
          <div class="quick-links">
            <button v-for="item in quickLinks" :key="item.path" type="button" @click="go(item.path)">
              {{ item.label }}
            </button>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dashboardApi from '../api/dashboard'
import demoApi from '../api/demo'

const router = useRouter()
const loading = ref(false)
const resetting = ref(false)
const errorMessage = ref('')
const summary = ref({
  metrics: {},
  todos: [],
  recentSurgeries: [],
  todayActivities: []
})

const metrics = computed(() => summary.value.metrics || {})
const todos = computed(() => summary.value.todos || [])
const recentSurgeries = computed(() => summary.value.recentSurgeries || [])
const todayActivities = computed(() => summary.value.todayActivities || [])

const statusMap = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  REVIEWED: '已审核',
  ARCHIVED: '已归档',
  APPLIED: '已申请',
  SCHEDULED: '已安排',
  IN_PROGRESS: '手术中',
  COMPLETED: '已完成',
  BILLED: '已计费',
  CANCELLED: '已取消',
  IN_HOSPITAL: '在院',
  DISCHARGED: '已出院',
  UNSETTLED: '未结算',
  SETTLED: '已结算'
}

const metricCards = computed(() => [
  {
    key: 'inHospital',
    label: '当前在院患者',
    value: n(metrics.value.currentInHospitalPatientCount),
    note: '点击进入入院列表',
    path: '/app/admissions?status=IN_HOSPITAL'
  },
  {
    key: 'todayAdmission',
    label: '今日入院',
    value: n(metrics.value.todayAdmissionCount),
    note: '今日登记入院',
    path: '/app/admissions'
  },
  {
    key: 'todayDischarge',
    label: '今日出院',
    value: n(metrics.value.todayDischargeCount),
    note: '今日完成出院',
    path: '/app/discharges'
  },
  {
    key: 'pendingRecord',
    label: '待处理病历',
    value: n(metrics.value.pendingMedicalRecordCount),
    note: '草稿 + 待审核',
    path: '/app/medical-records'
  },
  {
    key: 'todaySurgery',
    label: '今日手术',
    value: n(metrics.value.todaySurgeryCount),
    note: '计划或执行在今日',
    path: '/app/surgeries'
  },
  {
    key: 'surgeryFee',
    label: '今日手术费用',
    value: money(metrics.value.todaySurgeryFeeAmount),
    note: '已生成手术费用',
    path: '/app/charging-station'
  },
  {
    key: 'unsettledFee',
    label: '未结算费用',
    value: money(metrics.value.unsettledFeeAmount),
    note: '收费模块待处理',
    path: '/app/charging-station'
  },
  {
    key: 'settledFee',
    label: '已结算费用',
    value: money(metrics.value.settledFeeAmount),
    note: '已完成结算费用',
    path: '/app/discharges'
  },
  {
    key: 'pendingOrder',
    label: '待执行医嘱',
    value: n(metrics.value.pendingExecuteOrderCount),
    note: '已提交/已核对待执行',
    path: '/app/orders?status=SUBMITTED'
  },
  {
    key: 'todayNursing',
    label: '今日护理记录',
    value: n(metrics.value.todayNursingRecordCount),
    note: '护理站今日记录',
    path: '/app/nursing-records'
  },
  {
    key: 'todayPatientAppointment',
    label: '今日患者预约',
    value: n(metrics.value.todayPatientAppointmentCount),
    note: '移动端和患者服务预约',
    path: '/app/patient-appointments'
  },
  {
    key: 'pendingPatientAppointment',
    label: '待确认预约',
    value: n(metrics.value.pendingPatientAppointmentCount),
    note: '需要后台确认处理',
    path: '/app/patient-appointments?status=REQUESTED'
  }
])

const surgeryCards = computed(() => [
  {
    label: '待排班',
    status: 'APPLIED',
    value: n(metrics.value.surgeryAppliedCount),
    desc: '已申请未安排',
    tagType: 'warning',
    path: '/app/surgeries?status=APPLIED'
  },
  {
    label: '已排班',
    status: 'SCHEDULED',
    value: n(metrics.value.surgeryScheduledCount),
    desc: '等待开始手术',
    tagType: 'primary',
    path: '/app/surgeries?status=SCHEDULED'
  },
  {
    label: '手术中',
    status: 'IN_PROGRESS',
    value: n(metrics.value.surgeryInProgressCount),
    desc: '正在执行',
    tagType: 'danger',
    path: '/app/surgeries?status=IN_PROGRESS'
  },
  {
    label: '已完成',
    status: 'COMPLETED',
    value: n(metrics.value.surgeryCompletedCount),
    desc: '等待计费或归档',
    tagType: 'success',
    path: '/app/surgeries?status=COMPLETED'
  },
  {
    label: '待计费',
    status: 'PENDING_BILL',
    value: n(metrics.value.surgeryPendingBillingCount),
    desc: '完成后未计费',
    tagType: 'warning',
    path: '/app/surgeries?status=COMPLETED'
  }
])

const flowEntries = [
  { title: '患者管理', desc: '建档和患者详情', path: '/app/patients' },
  { title: '入院登记', desc: '办理入院和住院信息', path: '/app/admissions' },
  { title: '床位分配', desc: '占床、转床和释放', path: '/app/beds' },
  { title: '医嘱管理', desc: '开立、提交和核对', path: '/app/orders' },
  { title: '费用管理', desc: '费用明细和预交金', path: '/app/charging-station' },
  { title: '出院结算', desc: '结算并释放床位', path: '/app/discharges' }
]

const quickLinks = [
  { label: '患者管理', path: '/app/patients' },
  { label: '医生站', path: '/app/doctor-station' },
  { label: '病历管理', path: '/app/medical-records' },
  { label: '手术管理', path: '/app/surgeries' },
  { label: '收费管理', path: '/app/charging-station' },
  { label: '出院管理', path: '/app/discharges' }
]

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await dashboardApi.summary()
    summary.value = {
      metrics: data?.metrics || {},
      todos: data?.todos || [],
      recentSurgeries: data?.recentSurgeries || [],
      todayActivities: data?.todayActivities || []
    }
  } catch (error) {
    errorMessage.value = '首页数据加载失败，请确认后端服务已启动。'
    summary.value = { metrics: {}, todos: [], recentSurgeries: [], todayActivities: [] }
  } finally {
    loading.value = false
  }
}

async function resetDemoData() {
  try {
    await ElMessageBox.confirm('将清空并重新插入标准演示数据，确认继续？', '重置演示数据', { type: 'warning' })
  } catch {
    return
  }
  resetting.value = true
  try {
    await demoApi.reset()
    ElMessage.success('演示数据已重置')
    await loadDashboard()
  } finally {
    resetting.value = false
  }
}

function go(path) {
  if (!path) return
  router.push(path)
}

function n(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number) ? number : 0
}

function money(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function statusText(status) {
  return statusMap[status] || status || '未知'
}

function surgeryStatusType(status) {
  if (status === 'BILLED' || status === 'COMPLETED') return 'success'
  if (status === 'IN_PROGRESS') return 'danger'
  if (status === 'SCHEDULED') return 'primary'
  if (status === 'APPLIED') return 'warning'
  if (status === 'CANCELLED') return 'info'
  return ''
}

function todoTag(type) {
  if (String(type).includes('病历')) return 'primary'
  if (String(type).includes('手术')) return 'warning'
  if (String(type).includes('费用')) return 'danger'
  if (String(type).includes('在院')) return 'success'
  return 'info'
}

function todoKey(item) {
  return `${item.type}-${item.title}-${item.admissionId}-${item.time}`
}

function activityKey(item) {
  return `${item.type}-${item.title}-${item.time}`
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-panel,
.panel,
.metric-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.welcome-panel {
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px;
}

.eyebrow {
  margin-bottom: 8px;
  color: #2563eb;
  font-weight: 700;
}

.welcome-panel h1 {
  margin: 0 0 10px;
  font-size: 30px;
}

.welcome-panel p {
  max-width: 820px;
  margin: 0;
  color: #4b5563;
  line-height: 1.8;
}

.welcome-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.metric-card {
  min-height: 118px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
}

.metric-card:hover,
.surgery-stats button:hover,
.quick-flow button:hover,
.todo-list button:hover,
.activity-list button:hover,
.quick-links button:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.metric-card span,
.metric-card em {
  display: block;
}

.metric-card span {
  color: #6b7280;
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin: 8px 0;
  font-size: 28px;
  line-height: 1.2;
}

.metric-card em {
  color: #9ca3af;
  font-size: 12px;
  font-style: normal;
}

.dashboard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(340px, 0.8fr);
  gap: 16px;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  padding: 16px;
}

.panel-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-heading h2 {
  margin: 0 0 4px;
  font-size: 18px;
}

.panel-heading p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.surgery-stats,
.quick-flow,
.record-summary,
.quick-links {
  display: grid;
  gap: 10px;
}

.surgery-stats {
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
}

.surgery-stats button,
.quick-flow button,
.record-summary button,
.todo-list button,
.activity-list button,
.quick-links button {
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
}

.surgery-stats button {
  min-height: 104px;
  padding: 12px;
  text-align: left;
}

.surgery-stats strong {
  display: block;
  margin: 10px 0 4px;
  font-size: 24px;
}

.surgery-stats span,
.quick-flow span,
.todo-list span,
.todo-list em,
.activity-list em {
  color: #6b7280;
  font-size: 12px;
}

.quick-flow {
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
}

.quick-flow button {
  min-height: 92px;
  padding: 13px;
  text-align: left;
}

.quick-flow strong,
.quick-flow span {
  display: block;
}

.quick-flow span {
  margin-top: 8px;
}

.record-summary {
  grid-template-columns: repeat(3, 1fr);
}

.record-summary button {
  padding: 12px;
  text-align: center;
}

.record-summary span,
.record-summary strong {
  display: block;
}

.record-summary span {
  color: #6b7280;
  font-size: 12px;
}

.record-summary strong {
  margin-top: 6px;
  font-size: 24px;
}

.fee-overview {
  display: grid;
  gap: 10px;
}

.fee-overview div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.fee-overview span {
  color: #6b7280;
}

.fee-overview strong {
  font-size: 18px;
}

.todo-list,
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-list button,
.activity-list button {
  width: 100%;
  padding: 10px;
  text-align: left;
}

.todo-list div {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.todo-list span,
.todo-list em,
.activity-list span,
.activity-list strong,
.activity-list em {
  display: block;
}

.todo-list em,
.activity-list em {
  margin-top: 4px;
  font-style: normal;
}

.activity-list span {
  color: #2563eb;
  font-size: 12px;
}

.quick-links {
  grid-template-columns: repeat(2, 1fr);
}

.quick-links button {
  min-height: 42px;
}

.skeleton-card {
  height: 118px;
  border-radius: 8px;
}

@media (max-width: 980px) {
  .welcome-panel,
  .panel-heading {
    flex-direction: column;
    align-items: stretch;
  }

  .dashboard-layout {
    grid-template-columns: 1fr;
  }
}
</style>
