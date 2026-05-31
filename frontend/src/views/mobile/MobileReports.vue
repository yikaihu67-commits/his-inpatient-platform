<template>
  <MobileShell title="检查检验报告">
    <div v-for="item in rows" :key="item.itemName + item.requestTime" class="card">
      <div class="row"><strong>{{ item.itemName }}</strong><el-tag :type="item.abnormalFlag ? 'danger' : tagType(item.status)">{{ item.abnormalFlag ? '异常' : statusText(item.status) }}</el-tag></div>
      <p>{{ item.resultSummary || '暂无报告结果' }}</p>
      <div class="muted">{{ item.requestType }} / 报告时间：{{ time(item.reportTime) }}</div>
    </div>
    <el-empty v-if="!rows.length" description="暂无报告" />
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
  rows.value = await api.reports(params)
}
onMounted(load)
</script>

<style scoped>
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row { display: flex; justify-content: space-between; gap: 8px; }
p { margin: 10px 0; }
.muted { color: #64748b; font-size: 13px; }
</style>
