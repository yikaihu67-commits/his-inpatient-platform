<template>
  <div class="station-page">
    <div class="toolbar">
      <div>
        <h2>收费工作台</h2>
        <div class="muted">面向收费员演示：费用明细、预交金、余额、出院结算和床位释放联动。</div>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadAll">刷新</el-button>
        <el-button type="primary" :disabled="!selectedAdmission" @click="openFeeDialog">新增手工费用</el-button>
        <el-button type="success" :disabled="!selectedAdmission" @click="openDepositDialog">缴纳预交金</el-button>
        <el-button type="warning" :disabled="!canSettle" @click="settleDischarge">出院结算</el-button>
      </div>
    </div>

    <el-row :gutter="12">
      <el-col :span="6">
        <section class="station-panel">
          <div class="panel-title">在院患者</div>
          <el-scrollbar height="calc(100vh - 230px)" v-loading="loading">
            <el-empty v-if="admissions.length === 0" description="暂无可结算在院患者" />
            <button
              v-for="item in admissions"
              :key="item.id"
              type="button"
              class="patient-card"
              :class="{ active: selectedAdmission && selectedAdmission.id === item.id }"
              @click="selectAdmission(item)"
            >
              <strong>{{ item.patientName || `患者 ${item.patientId}` }}</strong>
              <span>{{ item.admissionNo }} / {{ item.departmentName }} / {{ item.wardName }}</span>
              <el-tag size="small" type="success">{{ item.status }}</el-tag>
            </button>
          </el-scrollbar>
        </section>
      </el-col>

      <el-col :span="18">
        <section class="station-panel">
          <div class="panel-title">患者与住院信息</div>
          <el-descriptions v-if="selectedAdmission" :column="4" border>
            <el-descriptions-item label="患者">{{ selectedAdmission.patientName || selectedAdmission.patientId }}</el-descriptions-item>
            <el-descriptions-item label="住院号">{{ selectedAdmission.admissionNo }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ selectedAdmission.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="病区">{{ selectedAdmission.wardName }}</el-descriptions-item>
            <el-descriptions-item label="床位">{{ selectedBedText }}</el-descriptions-item>
            <el-descriptions-item label="诊断">{{ selectedAdmission.admissionDiagnosis || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经管医生">{{ selectedAdmission.doctorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态"><el-tag type="success">{{ selectedAdmission.status }}</el-tag></el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择患者" />
        </section>

        <div class="summary-grid mt12">
          <div v-for="item in summaryCards" :key="item.label" class="stat-card">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ money(item.value) }}</div>
          </div>
        </div>

        <el-row :gutter="12" class="mt12">
          <el-col :span="14">
            <section class="station-panel">
              <div class="panel-title">费用明细</div>
              <el-table :data="fees" border stripe v-loading="feeLoading" empty-text="暂无费用" height="330px">
                <el-table-column prop="feeNo" label="费用编号" min-width="150" />
                <el-table-column prop="sourceType" label="费用来源" min-width="110">
                  <template #default="{ row }">{{ sourceText(row.sourceType) }}</template>
                </el-table-column>
                <el-table-column prop="itemCategory" label="费用类型" min-width="100">
                  <template #default="{ row }">{{ categoryText(row.itemCategory) }}</template>
                </el-table-column>
                <el-table-column prop="itemName" label="项目名称" min-width="170" />
                <el-table-column prop="quantity" label="数量" />
                <el-table-column prop="unitPrice" label="单价">
                  <template #default="{ row }">{{ money(row.unitPrice) }}</template>
                </el-table-column>
                <el-table-column prop="totalAmount" label="金额">
                  <template #default="{ row }">
                    <span :class="{ negative: Number(row.totalAmount || 0) < 0 }">{{ money(row.totalAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                  <template #default="{ row }"><el-tag :type="row.status === 'SETTLED' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'warning'">{{ statusText(row.status) }}</el-tag></template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" min-width="160">
                  <template #default="{ row }">{{ time(row.createdAt) }}</template>
                </el-table-column>
              </el-table>
            </section>
          </el-col>

          <el-col :span="10">
            <section class="station-panel">
              <div class="panel-title">预交金明细</div>
              <el-table :data="deposits" border stripe v-loading="depositLoading" empty-text="暂无预交金" height="330px">
                <el-table-column prop="depositNo" label="编号" min-width="140" />
                <el-table-column prop="amount" label="金额">
                  <template #default="{ row }">{{ money(row.amount) }}</template>
                </el-table-column>
                <el-table-column prop="paymentMethod" label="支付方式">
                  <template #default="{ row }">{{ paymentText(row.paymentMethod) }}</template>
                </el-table-column>
                <el-table-column prop="transactionType" label="类型">
                  <template #default="{ row }">{{ row.transactionType === 'REFUND' ? '退费' : '预交' }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态">
                  <template #default="{ row }"><el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">{{ statusText(row.status) }}</el-tag></template>
                </el-table-column>
              </el-table>
            </section>
          </el-col>
        </el-row>

        <section class="station-panel mt12">
          <div class="panel-title">结算结果</div>
          <el-table :data="discharges" border stripe v-loading="dischargeLoading" empty-text="暂无结算记录" height="180px">
            <el-table-column prop="dischargeNo" label="结算编号" min-width="160" />
            <el-table-column prop="totalFeeAmount" label="总费用">
              <template #default="{ row }">{{ money(row.totalFeeAmount) }}</template>
            </el-table-column>
            <el-table-column prop="depositBalance" label="预交余额">
              <template #default="{ row }">{{ money(row.depositBalance) }}</template>
            </el-table-column>
            <el-table-column prop="unpaidAmount" label="欠费">
              <template #default="{ row }">{{ money(row.unpaidAmount) }}</template>
            </el-table-column>
            <el-table-column prop="actualPayment" label="实付">
              <template #default="{ row }">{{ money(row.actualPayment) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag :type="row.status === 'SETTLED' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'warning'">{{ statusText(row.status) }}</el-tag></template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-dialog v-model="feeDialogVisible" title="新增手工费用" width="560px" destroy-on-close>
      <el-form :model="feeForm" label-width="90px">
        <el-form-item label="项目名称" required><el-input v-model="feeForm.itemName" /></el-form-item>
        <el-form-item label="费用类型"><el-select v-model="feeForm.itemCategory"><el-option label="治疗费" value="TREATMENT" /><el-option label="护理费" value="NURSING" /><el-option label="其他" value="OTHER" /></el-select></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="feeForm.quantity" :min="1" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="feeForm.unitPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="金额"><el-input :model-value="money(Number(feeForm.quantity || 0) * Number(feeForm.unitPrice || 0))" disabled /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createManualFee">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="depositDialogVisible" title="缴纳预交金" width="520px" destroy-on-close>
      <el-form :model="depositForm" label-width="90px">
        <el-form-item label="金额" required><el-input-number v-model="depositForm.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="支付方式"><el-select v-model="depositForm.paymentMethod"><el-option v-for="item in paymentOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
        <el-form-item label="操作员"><el-input v-model="depositForm.operatorName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="depositDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="payDeposit">确认缴纳</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import accountApi from '../api/account'
import admissionApi from '../api/admission'
import bedApi from '../api/bed'
import depositApi from '../api/deposit'
import dischargeApi from '../api/discharge'
import feeApi from '../api/fee'

const loading = ref(false)
const feeLoading = ref(false)
const depositLoading = ref(false)
const dischargeLoading = ref(false)
const saving = ref(false)
const admissions = ref([])
const beds = ref([])
const fees = ref([])
const deposits = ref([])
const discharges = ref([])
const account = ref(null)
const selectedAdmission = ref(null)
const feeDialogVisible = ref(false)
const depositDialogVisible = ref(false)
const feeForm = reactive({ itemName: '手工治疗费', itemCategory: 'TREATMENT', quantity: 1, unitPrice: 50 })
const depositForm = reactive({ amount: 300, paymentMethod: 'CASH', operatorName: '收费员' })

const paymentOptions = [
  { label: '现金', value: 'CASH' },
  { label: '银行卡', value: 'BANK_CARD' },
  { label: '微信', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '医保账户', value: 'MEDICAL_INSURANCE' }
]

const selectedBedText = computed(() => {
  const bed = beds.value.find((item) => Number(item.currentAdmissionId) === Number(selectedAdmission.value && selectedAdmission.value.id))
  return bed ? `${bed.wardName} ${bed.roomNo}房 ${bed.bedNo}床` : '未分配'
})

const totals = computed(() => {
  const validFees = fees.value.filter((item) => item.status !== 'CANCELLED')
  const totalFee = sum(validFees, 'totalAmount')
  const drugFee = sum(validFees.filter((item) => item.itemCategory === 'DRUG' && Number(item.totalAmount || 0) > 0), 'totalAmount')
  const orderFee = sum(validFees.filter((item) => item.sourceType === 'ORDER'), 'totalAmount')
  const returnFee = sum(validFees.filter((item) => item.sourceType === 'DRUG_RETURN' || Number(item.totalAmount || 0) < 0), 'totalAmount')
  const settledFee = sum(validFees.filter((item) => item.status === 'SETTLED'), 'totalAmount')
  const unsettledFee = sum(validFees.filter((item) => item.status === 'UNSETTLED'), 'totalAmount')
  const paid = sum(deposits.value.filter((item) => item.status === 'SUCCESS' && item.transactionType === 'PAY'), 'amount')
  const refunded = sum(deposits.value.filter((item) => item.status === 'SUCCESS' && item.transactionType === 'REFUND'), 'amount')
  const depositBalance = paid - refunded
  return {
    totalFee,
    drugFee,
    orderFee,
    returnFee,
    totalDeposit: paid,
    usedAmount: Math.min(totalFee, Math.max(depositBalance, 0)),
    payable: Math.max(totalFee - depositBalance, 0),
    settledFee,
    unsettledFee,
    balance: Math.max(depositBalance - totalFee, 0)
  }
})

const summaryCards = computed(() => [
  { label: '总费用', value: totals.value.totalFee },
  { label: '药品费用', value: totals.value.drugFee },
  { label: '医嘱费用', value: totals.value.orderFee },
  { label: '退药冲正', value: totals.value.returnFee },
  { label: '预交金总额', value: totals.value.totalDeposit },
  { label: '已用金额', value: totals.value.usedAmount },
  { label: '应付金额', value: totals.value.payable },
  { label: '已结算金额', value: totals.value.settledFee },
  { label: '未结金额', value: totals.value.unsettledFee },
  { label: '余额', value: totals.value.balance }
])

const canSettle = computed(() => selectedAdmission.value && selectedAdmission.value.status === 'IN_HOSPITAL' && !discharges.value.some((item) => item.status === 'SETTLED'))

async function loadAll() {
  loading.value = true
  try {
    const [admissionPage, bedPage] = await Promise.all([
      admissionApi.list({ page: 1, pageSize: 100, status: 'IN_HOSPITAL' }),
      bedApi.list({ page: 1, pageSize: 100 })
    ])
    admissions.value = recordsOf(admissionPage)
    beds.value = recordsOf(bedPage)
    if (!selectedAdmission.value && admissions.value.length) selectedAdmission.value = admissions.value[0]
    if (selectedAdmission.value) await loadSelectedData()
  } finally {
    loading.value = false
  }
}

async function selectAdmission(admission) {
  selectedAdmission.value = admission
  await loadSelectedData()
}

async function loadSelectedData() {
  if (!selectedAdmission.value) return
  feeLoading.value = true
  depositLoading.value = true
  dischargeLoading.value = true
  try {
    const id = selectedAdmission.value.id
    const [feePage, depositPage, dischargePage, accountSummary] = await Promise.all([
      feeApi.list({ page: 1, pageSize: 100, admissionId: id }),
      depositApi.list({ page: 1, pageSize: 100, admissionId: id }),
      dischargeApi.list({ page: 1, pageSize: 50, admissionId: id }),
      accountApi.summary(id).catch(() => null)
    ])
    fees.value = recordsOf(feePage)
    deposits.value = recordsOf(depositPage)
    discharges.value = recordsOf(dischargePage)
    account.value = accountSummary
  } finally {
    feeLoading.value = false
    depositLoading.value = false
    dischargeLoading.value = false
  }
}

function openFeeDialog() {
  Object.assign(feeForm, { itemName: '手工治疗费', itemCategory: 'TREATMENT', quantity: 1, unitPrice: 50 })
  feeDialogVisible.value = true
}

async function createManualFee() {
  if (!selectedAdmission.value || !feeForm.itemName) {
    ElMessage.warning('请选择患者并填写项目名称')
    return
  }
  saving.value = true
  try {
    await feeApi.create({
      admissionId: selectedAdmission.value.id,
      patientId: selectedAdmission.value.patientId,
      sourceType: 'MANUAL',
      itemName: feeForm.itemName,
      itemCategory: feeForm.itemCategory,
      quantity: feeForm.quantity,
      unit: '次',
      unitPrice: feeForm.unitPrice,
      status: 'UNSETTLED',
      remark: '收费工作台手工费用'
    })
    ElMessage.success('手工费用已新增')
    feeDialogVisible.value = false
    await loadSelectedData()
  } finally {
    saving.value = false
  }
}

function openDepositDialog() {
  Object.assign(depositForm, { amount: Math.max(totals.value.payable, 300), paymentMethod: 'CASH', operatorName: '收费员' })
  depositDialogVisible.value = true
}

async function payDeposit() {
  if (!selectedAdmission.value || !depositForm.amount) {
    ElMessage.warning('请选择患者并填写金额')
    return
  }
  saving.value = true
  try {
    await depositApi.pay({
      admissionId: selectedAdmission.value.id,
      patientId: selectedAdmission.value.patientId,
      amount: depositForm.amount,
      paymentMethod: depositForm.paymentMethod,
      operatorName: depositForm.operatorName,
      remark: '收费工作台预交金'
    })
    ElMessage.success('预交金已缴纳')
    depositDialogVisible.value = false
    await loadSelectedData()
  } finally {
    saving.value = false
  }
}

async function settleDischarge() {
  if (!canSettle.value) {
    ElMessage.warning('只有在院且未结算患者可以出院结算')
    return
  }
  saving.value = true
  try {
    const draft = discharges.value.find((item) => item.status === 'DRAFT') || await dischargeApi.create({
      admissionId: selectedAdmission.value.id,
      patientId: selectedAdmission.value.patientId,
      operatorName: '收费员',
      remark: '收费工作台出院结算'
    })
    await dischargeApi.settle(draft.id, {
      actualPayment: draft.unpaidAmount || totals.value.payable,
      paymentMethod: 'CASH',
      operatorName: '收费员',
      remark: '收费工作台执行结算'
    })
    ElMessage.success('出院结算完成，入院状态和床位状态已刷新')
    selectedAdmission.value = null
    fees.value = []
    deposits.value = []
    discharges.value = []
    await loadAll()
  } finally {
    saving.value = false
  }
}

function recordsOf(page) {
  return page && (page.records || page.list || page.content || page.rows) || []
}

function sum(rows, prop) {
  return rows.reduce((acc, item) => acc + Number(item[prop] || 0), 0)
}

function money(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function time(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function sourceText(value) {
  const map = { ORDER: '医嘱费用', DRUG_DISPENSE: '药品发药费用', DRUG_RETURN: '退药冲正费用', SURGERY: '手术费用', BED: '床位费', MANUAL: '手工费用', OTHER: '其他' }
  return map[value] || value || '-'
}

function categoryText(value) {
  const map = { DRUG: '药品费', EXAM: '检查费', LAB: '检验费', TREATMENT: '治疗费', SURGERY: '手术费', BED: '床位费', NURSING: '护理费', OTHER: '其他' }
  return map[value] || value || '-'
}

function paymentText(value) {
  const map = { CASH: '现金', BANK_CARD: '银行卡', WECHAT: '微信', ALIPAY: '支付宝', MEDICAL_INSURANCE: '医保账户', OTHER: '其他' }
  return map[value] || value || '-'
}

function statusText(value) {
  const map = { UNSETTLED: '未结算', SETTLED: '已结算', CANCELLED: '已取消', SUCCESS: '成功', PAY: '预交', REFUND: '退费', DRAFT: '草稿' }
  return map[value] || value || '-'
}

onMounted(loadAll)
</script>
