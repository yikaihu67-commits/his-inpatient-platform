<template>
  <div class="station-page">
    <div class="toolbar">
      <div>
        <h2>护士站</h2>
        <div class="muted">面向护士演示：病区患者、床位图、医嘱核对、护士执行、床位释放和出院入口。</div>
      </div>
      <div class="toolbar-actions">
        <el-select v-model="wardName" style="width: 150px" @change="loadAll">
          <el-option label="一病区" value="一病区" />
          <el-option label="二病区" value="二病区" />
          <el-option label="三病区" value="三病区" />
          <el-option label="重症病区" value="重症病区" />
        </el-select>
        <el-button @click="loadAll">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="12">
      <el-col :span="7">
        <section class="station-panel">
          <div class="panel-title">当前病区在院患者</div>
          <el-scrollbar height="260px" v-loading="loading">
            <el-empty v-if="wardAdmissions.length === 0" description="暂无在院患者" />
            <button
              v-for="item in wardAdmissions"
              :key="item.id"
              type="button"
              class="patient-card"
              :class="{ active: selectedAdmission?.id === item.id }"
              @click="selectAdmission(item)"
            >
              <strong>{{ item.patientName || `患者 ${item.patientId}` }}</strong>
              <span>{{ item.admissionNo }} / {{ item.wardName }}</span>
              <el-tag size="small" type="success">在院</el-tag>
            </button>
          </el-scrollbar>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">床位图</div>
          <div v-loading="loading" class="bed-map">
            <button
              v-for="bed in wardBeds"
              :key="bed.id"
              type="button"
              class="bed-card"
              :class="bed.status"
              @click="selectBed(bed)"
            >
              <strong>{{ bed.bedNo }}</strong>
              <span>{{ bed.roomNo }}房</span>
              <em>{{ enumText(bed.status) }}</em>
            </button>
          </div>
          <el-empty v-if="wardBeds.length === 0 && !loading" description="暂无床位" />
          <div class="bed-actions">
            <el-button size="small" :disabled="!selectedBed || !selectedAdmission || !['EMPTY', 'AVAILABLE'].includes(selectedBed.status)" @click="transferToBed">转床到此床</el-button>
            <el-button size="small" :disabled="!selectedBed || !['EMPTY', 'AVAILABLE'].includes(selectedBed.status)" @click="reserveBed">包床/预留</el-button>
            <el-button size="small" type="warning" :disabled="!selectedBed || selectedBed.status !== 'OCCUPIED'" @click="releaseBed">释放床位</el-button>
          </div>
        </section>
      </el-col>

      <el-col :span="17">
        <section class="station-panel">
          <div class="panel-title">待核对医嘱</div>
          <el-table :data="submittedOrders" border stripe v-loading="orderLoading" empty-text="暂无待核对医嘱" height="210px">
            <el-table-column prop="orderNo" label="医嘱编号" min-width="150" />
            <el-table-column prop="patientId" label="患者ID" />
            <el-table-column prop="itemName" label="医嘱内容" min-width="180" />
            <el-table-column prop="orderCategory" label="分类">
              <template #default="{ row }">{{ enumText(row.orderCategory) }}</template>
            </el-table-column>
            <el-table-column prop="doctorName" label="医生" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag type="primary">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="checkOrder(row)">核对</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">医嘱执行</div>
          <el-table :data="checkedOrders" border stripe v-loading="orderLoading" empty-text="暂无可执行医嘱" height="220px">
            <el-table-column prop="orderNo" label="医嘱编号" min-width="150" />
            <el-table-column prop="patientId" label="患者ID" />
            <el-table-column prop="itemName" label="医嘱内容" min-width="180" />
            <el-table-column prop="route" label="用法" />
            <el-table-column prop="frequency" label="频次" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag type="success">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{ row }">
                <el-button link type="primary" @click="executeOrder(row)">创建并执行</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">
            执行记录
            <el-button link type="primary" :disabled="!selectedAdmission" @click="goDischarge">出院结算入口</el-button>
          </div>
          <el-table :data="executions" border stripe v-loading="executionLoading" empty-text="暂无执行记录" height="190px">
            <el-table-column prop="executionNo" label="执行编号" min-width="150" />
            <el-table-column prop="orderId" label="医嘱ID" />
            <el-table-column prop="nurseName" label="护士" />
            <el-table-column prop="executedTime" label="执行时间" min-width="160">
              <template #default="{ row }">{{ formatTime(row.executedTime || row.executeTime) }}</template>
            </el-table-column>
            <el-table-column prop="result" label="结果" min-width="120" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag :type="row.status === 'EXECUTED' ? 'success' : 'warning'">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import admissionApi from '../api/admission'
import bedApi from '../api/bed'
import nursingApi from '../api/nursingExecution'
import orderApi from '../api/order'

const router = useRouter()
const wardName = ref('一病区')
const loading = ref(false)
const orderLoading = ref(false)
const executionLoading = ref(false)
const admissions = ref([])
const beds = ref([])
const orders = ref([])
const executions = ref([])
const selectedAdmission = ref(null)
const selectedBed = ref(null)

const wardAdmissions = computed(() => admissions.value.filter((item) => item.status === 'IN_HOSPITAL' && item.wardName === wardName.value))
const wardBeds = computed(() => beds.value.filter((item) => item.wardName === wardName.value))
const visibleOrders = computed(() => {
  const admissionIds = new Set(wardAdmissions.value.map((item) => Number(item.id)))
  return orders.value.filter((item) => admissionIds.has(Number(item.admissionId)))
})
const submittedOrders = computed(() => visibleOrders.value.filter((item) => item.status === 'SUBMITTED'))
const checkedOrders = computed(() => visibleOrders.value.filter((item) => item.status === 'CHECKED'))

async function loadAll() {
  loading.value = true
  try {
    const [admissionPage, bedPage] = await Promise.all([
      admissionApi.list({ page: 1, pageSize: 100, status: 'IN_HOSPITAL' }),
      bedApi.list({ page: 1, pageSize: 100 })
    ])
    admissions.value = recordsOf(admissionPage)
    beds.value = recordsOf(bedPage)
    if (!selectedAdmission.value && wardAdmissions.value.length) selectedAdmission.value = wardAdmissions.value[0]
    selectedBed.value = selectedAdmission.value ? beds.value.find((bed) => Number(bed.currentAdmissionId) === Number(selectedAdmission.value.id)) : null
    await Promise.all([loadOrders(), loadExecutions()])
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const page = await orderApi.list({ page: 1, pageSize: 100 })
    orders.value = recordsOf(page)
  } finally {
    orderLoading.value = false
  }
}

async function loadExecutions() {
  executionLoading.value = true
  try {
    const page = await nursingApi.list({ page: 1, pageSize: 100, admissionId: selectedAdmission.value?.id })
    executions.value = recordsOf(page)
  } finally {
    executionLoading.value = false
  }
}

async function selectAdmission(admission) {
  selectedAdmission.value = admission
  selectedBed.value = beds.value.find((bed) => Number(bed.currentAdmissionId) === Number(admission.id)) || null
  await loadExecutions()
}

function selectBed(bed) {
  selectedBed.value = bed
  const admission = admissions.value.find((item) => Number(item.id) === Number(bed.currentAdmissionId))
  if (admission) selectedAdmission.value = admission
}

async function checkOrder(row) {
  await orderApi.check(row.id, { nurseName: '赵护士', remark: '护士站核对' })
  ElMessage.success('医嘱已核对')
  await loadOrders()
}

async function executeOrder(row) {
  const execution = await nursingApi.create({ orderId: row.id, nurseName: '赵护士', remark: '护士站创建执行记录' })
  await nursingApi.execute(execution.id, { nurseName: '赵护士', result: '执行成功', remark: '护士站执行' })
  ElMessage.success('医嘱已执行')
  await Promise.all([loadOrders(), loadExecutions()])
}

async function transferToBed() {
  if (!selectedAdmission.value || !selectedBed.value) return
  const oldBed = beds.value.find((bed) => Number(bed.currentAdmissionId) === Number(selectedAdmission.value.id))
  if (oldBed && oldBed.id !== selectedBed.value.id) {
    await bedApi.release(oldBed.id)
  }
  await bedApi.assign(selectedBed.value.id, { admissionId: selectedAdmission.value.id })
  ElMessage.success('转床成功')
  await loadAll()
}

async function reserveBed() {
  await bedApi.update(selectedBed.value.id, { ...selectedBed.value, status: 'RESERVED', currentAdmissionId: null })
  ElMessage.success('床位已预留')
  await loadAll()
}

async function releaseBed() {
  await bedApi.release(selectedBed.value.id)
  ElMessage.success('床位已释放')
  await loadAll()
}

function goDischarge() {
  router.push('/app/discharges')
}

function recordsOf(page) {
  return page?.records || page?.list || page?.content || page?.rows || []
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function enumText(value) {
  const map = {
    EMPTY: '空床', AVAILABLE: '可用', OCCUPIED: '占用', RESERVED: '预留', DISABLED: '停用',
    DRUG: '药品', EXAM: '检查', LAB: '检验', TREATMENT: '治疗', CARE: '护理',
    SUBMITTED: '已提交', CHECKED: '已核对', EXECUTED: '已执行', PENDING: '待执行', FAILED: '失败', CANCELLED: '已取消'
  }
  return map[value] || value || '-'
}

onMounted(loadAll)
</script>
