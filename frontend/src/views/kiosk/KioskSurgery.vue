<template>
  <KioskShell title="手术安排">
    <el-table :data="rows" v-loading="loading" border stripe empty-text="暂无手术安排">
      <el-table-column prop="surgeryName" label="手术名称" min-width="190" />
      <el-table-column label="状态" width="130"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="计划时间" min-width="170"><template #default="{ row }">{{ time(row.plannedTime) }}</template></el-table-column>
      <el-table-column label="实际开始" min-width="170"><template #default="{ row }">{{ time(row.actualStartTime) }}</template></el-table-column>
      <el-table-column label="实际结束" min-width="170"><template #default="{ row }">{{ time(row.actualEndTime) }}</template></el-table-column>
      <el-table-column prop="operatingRoom" label="手术室" width="130" />
      <el-table-column prop="primaryDoctorName" label="主刀医生" width="140" />
      <el-table-column label="费用" width="130"><template #default="{ row }">{{ money(row.surgeryFee) }}</template></el-table-column>
    </el-table>
  </KioskShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { sessionParams } from '../../kioskSession'
import KioskShell from './KioskShell.vue'
import { money, statusText, statusType, time } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const rows = ref([])

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    rows.value = await api.surgeries(params)
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
