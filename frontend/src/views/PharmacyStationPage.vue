<template>
  <div class="station-page">
    <div class="toolbar">
      <div>
        <h2>药房工作台</h2>
        <div class="muted">面向药房演示：查看待发药药品医嘱，发药扣库存并生成费用，退药恢复库存并生成冲正费用。</div>
      </div>
      <el-button @click="loadAll">刷新</el-button>
    </div>

    <el-row :gutter="12">
      <el-col :span="8">
        <section class="station-panel">
          <div class="panel-title">药品库存</div>
          <el-table :data="drugs" border stripe v-loading="loading" empty-text="暂无药品" height="320px">
            <el-table-column prop="drugCode" label="编码" min-width="120" />
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" min-width="110" />
            <el-table-column prop="unit" label="单位" />
            <el-table-column prop="price" label="单价">
              <template #default="{ row }">{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="库存">
              <template #default="{ row }">
                <el-tag :type="Number(row.stockQuantity || 0) <= Number(row.stockLowerLimit || 0) ? 'danger' : 'success'">
                  {{ row.stockQuantity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stockLowerLimit" label="下限" />
          </el-table>
          <el-alert class="mt12" type="warning" show-icon :closable="false" title="低库存药品会用红色标签提示；低库存演示药可用于触发库存不足业务错误。" />
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">患者信息</div>
          <el-descriptions v-if="selectedOrder" :column="1" border>
            <el-descriptions-item label="患者">{{ selectedOrder.patientName || selectedOrder.patientId }}</el-descriptions-item>
            <el-descriptions-item label="住院号">{{ selectedOrder.admissionNo || selectedOrder.admissionId }}</el-descriptions-item>
            <el-descriptions-item label="医嘱">{{ selectedOrder.itemName }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ selectedOrder.doctorName || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="请选择待发药医嘱" />
        </section>
      </el-col>

      <el-col :span="16">
        <section class="station-panel">
          <div class="panel-title">待发药药品医嘱</div>
          <el-table :data="pendingDrugOrders" border stripe v-loading="orderLoading" empty-text="暂无待发药医嘱" height="310px" @row-click="selectOrder">
            <el-table-column prop="orderNo" label="医嘱编号" min-width="150" />
            <el-table-column prop="patientName" label="患者" min-width="100" />
            <el-table-column prop="itemName" label="药品名称" min-width="160" />
            <el-table-column prop="dosage" label="剂量" />
            <el-table-column prop="route" label="用法" />
            <el-table-column prop="frequency" label="频次" min-width="110" />
            <el-table-column prop="doctorName" label="医生" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag type="success">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDispense(row)">发药</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <section class="station-panel mt12">
          <div class="panel-title">已发药记录</div>
          <el-table :data="dispensedRecords" border stripe v-loading="dispenseLoading" empty-text="暂无发药记录" height="310px">
            <el-table-column prop="dispenseNo" label="发药单号" min-width="150" />
            <el-table-column prop="patientName" label="患者" min-width="100" />
            <el-table-column prop="orderNo" label="医嘱编号" min-width="150" />
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="quantity" label="数量" />
            <el-table-column label="单价">
              <template #default="{ row }">{{ money(drugPrice(row.drugId)) }}</template>
            </el-table-column>
            <el-table-column label="金额">
              <template #default="{ row }">{{ money(Number(row.quantity || 0) * Number(drugPrice(row.drugId) || 0)) }}</template>
            </el-table-column>
            <el-table-column prop="pharmacistName" label="药师" />
            <el-table-column prop="dispenseTime" label="发药时间" min-width="160">
              <template #default="{ row }">{{ time(row.dispenseTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }"><el-tag :type="row.status === 'DISPENSED' ? 'success' : 'warning'">{{ enumText(row.status) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="110">
              <template #default="{ row }">
                <el-button link type="warning" :disabled="row.status !== 'DISPENSED'" @click="returnDrug(row)">退药</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </el-col>
    </el-row>

    <el-dialog v-model="dispenseDialogVisible" title="药房发药" width="560px" destroy-on-close>
      <el-form :model="dispenseForm" label-width="90px">
        <el-form-item label="医嘱内容">
          <el-input :model-value="selectedOrder?.itemName" disabled />
        </el-form-item>
        <el-form-item label="药品" required>
          <el-select v-model="dispenseForm.drugId" filterable @change="syncDrugAmount">
            <el-option v-for="drug in drugs" :key="drug.id" :label="`${drug.drugName} / 库存 ${drug.stockQuantity}`" :value="drug.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" required>
          <el-input-number v-model="dispenseForm.quantity" :min="1" @change="syncDrugAmount" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input :model-value="money(selectedDrugPrice)" disabled />
        </el-form-item>
        <el-form-item label="金额">
          <el-input :model-value="money(dispenseAmount)" disabled />
        </el-form-item>
        <el-form-item label="药师">
          <el-input v-model="dispenseForm.pharmacistName" />
        </el-form-item>
        <el-alert v-if="selectedDrug && dispenseForm.quantity > selectedDrug.stockQuantity" type="error" show-icon :closable="false" title="库存不足，提交后后端会返回业务错误。" />
      </el-form>
      <template #footer>
        <el-button @click="dispenseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDispense">确认发药</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import dispenseApi from '../api/dispense'
import drugApi from '../api/drug'
import orderApi from '../api/order'

const loading = ref(false)
const orderLoading = ref(false)
const dispenseLoading = ref(false)
const saving = ref(false)
const drugs = ref([])
const orders = ref([])
const dispenses = ref([])
const selectedOrder = ref(null)
const dispenseDialogVisible = ref(false)
const dispenseForm = reactive({ drugId: null, quantity: 1, pharmacistName: '药师' })

const activeDispenseOrderIds = computed(() => new Set(dispenses.value.filter((item) => ['CREATED', 'DISPENSED'].includes(item.status)).map((item) => Number(item.orderId))))
const pendingDrugOrders = computed(() => orders.value.filter((item) => item.orderCategory === 'DRUG' && item.status === 'CHECKED' && !activeDispenseOrderIds.value.has(Number(item.id))))
const dispensedRecords = computed(() => dispenses.value.filter((item) => ['DISPENSED', 'RETURNED'].includes(item.status)))
const selectedDrug = computed(() => drugById(dispenseForm.drugId))
const selectedDrugPrice = computed(() => selectedDrug.value ? selectedDrug.value.price : 0)
const dispenseAmount = computed(() => Number(dispenseForm.quantity || 0) * Number(selectedDrug.value?.price || 0))

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadDrugs(), loadOrders(), loadDispenses()])
  } finally {
    loading.value = false
  }
}

async function loadDrugs() {
  const page = await drugApi.list({ page: 1, pageSize: 100, status: 'ENABLED' })
  drugs.value = recordsOf(page)
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const page = await orderApi.list({ page: 1, pageSize: 100, orderCategory: 'DRUG', status: 'CHECKED' })
    orders.value = recordsOf(page)
  } finally {
    orderLoading.value = false
  }
}

async function loadDispenses() {
  dispenseLoading.value = true
  try {
    const page = await dispenseApi.list({ page: 1, pageSize: 100 })
    dispenses.value = recordsOf(page)
  } finally {
    dispenseLoading.value = false
  }
}

function selectOrder(row) {
  selectedOrder.value = row
}

function openDispense(row) {
  selectedOrder.value = row
  const matchedDrug = drugs.value.find((drug) => drug.drugName === row.itemName) || drugs.value[0]
  dispenseForm.drugId = matchedDrug?.id || null
  dispenseForm.quantity = matchedDrug && Number(matchedDrug.stockQuantity || 0) <= Number(matchedDrug.stockLowerLimit || 0) ? Number(matchedDrug.stockQuantity || 0) + 1 : 1
  dispenseForm.pharmacistName = '药师'
  dispenseDialogVisible.value = true
}

function syncDrugAmount() {}

async function submitDispense() {
  if (!selectedOrder.value || !dispenseForm.drugId || !dispenseForm.quantity) {
    ElMessage.warning('请选择医嘱、药品和数量')
    return
  }
  saving.value = true
  try {
    const created = await dispenseApi.create({
      orderId: selectedOrder.value.id,
      drugId: dispenseForm.drugId,
      quantity: dispenseForm.quantity,
      pharmacistName: dispenseForm.pharmacistName,
      remark: '药房工作台发药'
    })
    await dispenseApi.dispense(created.id, { pharmacistName: dispenseForm.pharmacistName, remark: '药房工作台发药' })
    ElMessage.success('发药成功，库存已扣减并生成药品费用')
    dispenseDialogVisible.value = false
    await loadAll()
  } finally {
    saving.value = false
  }
}

async function returnDrug(row) {
  await dispenseApi.returnDrug(row.id, { pharmacistName: row.pharmacistName || '药师', remark: '药房工作台退药' })
  ElMessage.success('退药成功，库存已恢复并生成冲正费用')
  await loadAll()
}

function drugById(id) {
  return drugs.value.find((item) => Number(item.id) === Number(id))
}

function drugPrice(id) {
  const drug = drugById(id)
  return drug ? drug.price : 0
}

function recordsOf(page) {
  return page?.records || page?.list || page?.content || page?.rows || []
}

function money(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function time(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

function enumText(value) {
  const map = { CHECKED: '已核对', DISPENSED: '已发药', RETURNED: '已退药', CREATED: '已创建', DRUG: '药品' }
  return map[value] || value || '-'
}

onMounted(loadAll)
</script>
