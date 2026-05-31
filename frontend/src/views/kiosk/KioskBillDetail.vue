<template>
  <KioskShell title="费用明细">
    <section class="filter-bar">
      <el-select v-model="query.itemCategory" clearable size="large" placeholder="费用类型" style="width: 190px" @change="load">
        <el-option label="药品" value="DRUG" />
        <el-option label="检查" value="EXAM" />
        <el-option label="检验" value="LAB" />
        <el-option label="护理" value="NURSING" />
        <el-option label="手术" value="SURGERY" />
        <el-option label="处置" value="TREATMENT" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-select v-model="query.status" clearable size="large" placeholder="结算状态" style="width: 190px" @change="load">
        <el-option label="未结算" value="UNSETTLED" />
        <el-option label="已结算" value="SETTLED" />
      </el-select>
    </section>
    <el-table :data="rows" v-loading="loading" border stripe empty-text="暂无费用明细">
      <el-table-column prop="itemName" label="费用项目" min-width="180" />
      <el-table-column prop="itemCategory" label="类型" width="110" />
      <el-table-column label="单价" width="120"><template #default="{ row }">{{ money(row.unitPrice) }}</template></el-table-column>
      <el-table-column label="数量" width="100"><template #default="{ row }">{{ money(row.quantity) }}</template></el-table-column>
      <el-table-column label="金额" width="130"><template #default="{ row }">{{ money(row.totalAmount) }}</template></el-table-column>
      <el-table-column prop="sourceType" label="来源" width="150" />
      <el-table-column label="时间" min-width="170"><template #default="{ row }">{{ time(row.feeTime) }}</template></el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template>
      </el-table-column>
    </el-table>
  </KioskShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { sessionParams } from '../../kioskSession'
import KioskShell from './KioskShell.vue'
import { money, statusText, statusType, time } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const rows = ref([])
const query = reactive({ itemCategory: '', status: '' })

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    rows.value = await api.billDetails({ ...params, ...query })
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}

:deep(.el-table) {
  font-size: 18px;
}
</style>
