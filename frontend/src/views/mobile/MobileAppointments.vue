<template>
  <MobileShell title="预约服务">
    <el-button type="primary" class="full" @click="$router.push('/mobile/appointments/new')">新增预约</el-button>
    <div v-for="item in rows" :key="item.id" class="card">
      <div class="row"><strong>{{ typeText(item.appointmentType) }}</strong><el-tag :type="tagType(item.status)">{{ statusText(item.status) }}</el-tag></div>
      <p>{{ item.appointmentItem }}</p>
      <div class="muted">{{ item.appointmentDate }} {{ item.timeSlot }} / {{ item.contactPhone || '-' }}</div>
      <el-button v-if="['REQUESTED','CONFIRMED'].includes(item.status)" type="danger" plain size="small" @click="cancel(item)">取消预约</el-button>
    </div>
    <el-empty v-if="!rows.length" description="暂无预约" />
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { statusText, tagType } from './mobileFormat'

const router = useRouter()
const rows = ref([])
const typeMap = { EXAM_APPOINTMENT: '检查预约', LAB_APPOINTMENT: '检验预约', DISCHARGE_SETTLEMENT: '出院结算预约', FOLLOW_UP: '复诊预约', OTHER: '其他' }
const typeText = (value) => typeMap[value] || value

async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  rows.value = await api.appointments(params)
}

async function cancel(item) {
  await api.cancelAppointment(item.id, { remark: '患者移动端取消' })
  await load()
}

onMounted(load)
</script>

<style scoped>
.full { width: 100%; margin-bottom: 12px; }
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
p { margin: 10px 0; }
.muted { color: #64748b; margin-bottom: 10px; }
</style>
