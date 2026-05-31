<template>
  <MobileShell title="手术安排">
    <div v-for="item in rows" :key="item.surgeryName + item.plannedTime" class="card">
      <div class="row"><strong>{{ item.surgeryName }}</strong><el-tag :type="tagType(item.status)">{{ statusText(item.status) }}</el-tag></div>
      <p>计划时间：{{ time(item.plannedTime) }}</p>
      <p>手术室：{{ item.operatingRoom || '-' }}</p>
      <p>主刀医生：{{ item.primaryDoctorName || '-' }}</p>
    </div>
    <el-empty v-if="!rows.length" description="暂无手术安排" />
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { statusText, tagType, time } from './mobileFormat'

const router = useRouter()
const rows = ref([])
async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  rows.value = await api.surgeries(params)
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row { display: flex; justify-content: space-between; gap: 8px; }
p { margin: 10px 0 0; color: #334155; }
</style>
