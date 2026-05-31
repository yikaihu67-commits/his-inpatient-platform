<template>
  <MobileShell title="出院服务">
    <section class="card">
      <div class="row"><strong>出院状态</strong><el-tag :type="tagType(info.status)">{{ statusText(info.status) }}</el-tag></div>
      <p>待结算费用：{{ money(info.unpaidAmount) }}</p>
      <el-button type="primary" plain class="full" @click="goAppointment">预约出院结算</el-button>
    </section>
    <section class="card">
      <h2>出院小结</h2>
      <p>{{ info.dischargeSummary || '-' }}</p>
      <h2>出院医嘱</h2>
      <p>{{ info.dischargeOrder || '-' }}</p>
    </section>
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { money, statusText, tagType } from './mobileFormat'

const router = useRouter()
const info = ref({})
async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  info.value = await api.discharge(params)
}
function goAppointment() {
  router.push('/mobile/appointments/new')
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row { display: flex; justify-content: space-between; gap: 8px; }
.full { width: 100%; margin-top: 12px; }
h2 { margin: 10px 0 6px; font-size: 16px; }
p { color: #334155; line-height: 1.6; }
</style>
