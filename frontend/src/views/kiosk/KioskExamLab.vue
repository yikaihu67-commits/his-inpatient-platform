<template>
  <KioskShell title="检查检验报告">
    <el-table :data="rows" v-loading="loading" border stripe empty-text="暂无检查检验记录">
      <el-table-column prop="itemName" label="项目名称" min-width="180" />
      <el-table-column prop="requestType" label="类型" width="100" />
      <el-table-column label="申请时间" min-width="170"><template #default="{ row }">{{ time(row.requestTime) }}</template></el-table-column>
      <el-table-column label="执行时间" min-width="170"><template #default="{ row }">{{ time(row.executedTime) }}</template></el-table-column>
      <el-table-column label="报告时间" min-width="170"><template #default="{ row }">{{ time(row.reportTime) }}</template></el-table-column>
      <el-table-column label="状态" width="130"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="异常" width="100"><template #default="{ row }"><el-tag :type="row.abnormalFlag ? 'danger' : 'success'">{{ row.abnormalFlag ? '异常' : '正常' }}</el-tag></template></el-table-column>
      <el-table-column prop="resultSummary" label="报告结果" min-width="220" show-overflow-tooltip />
    </el-table>
  </KioskShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { sessionParams } from '../../kioskSession'
import KioskShell from './KioskShell.vue'
import { statusText, statusType, time } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const rows = ref([])

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    rows.value = await api.examLabs(params)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
:deep(.el-table) {
  font-size: 18px;
}
</style>
