<template>
  <MobileShell title="患者移动服务">
    <section class="hero">
      <div>{{ patient.patientName || '-' }}</div>
      <strong>{{ admission.departmentName || '-' }} / {{ admission.bedNo || '-' }}</strong>
      <span>{{ statusText(admission.admissionStatus) }}</span>
    </section>
    <section class="money-grid">
      <div><span>总费用</span><strong>{{ money(bill.totalAmount) }}</strong></div>
      <div><span>未结算</span><strong>{{ money(bill.unsettledAmount) }}</strong></div>
    </section>
    <section class="entry-grid">
      <button v-for="item in entries" :key="item.path" type="button" @click="$router.push(item.path)">
        {{ item.label }}
      </button>
    </section>
    <section class="card">
      <h2>最近费用</h2>
      <div v-for="fee in recentFees" :key="fee.itemName + fee.feeTime" class="list-row">
        <span>{{ fee.itemName }}</span>
        <strong>{{ money(fee.totalAmount) }}</strong>
      </div>
      <el-empty v-if="!recentFees.length" description="暂无费用" />
    </section>
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { getMobileSession, mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { money, statusText } from './mobileFormat'

const router = useRouter()
const patient = ref(getMobileSession()?.patient || {})
const admission = ref({})
const bill = ref({})
const recentFees = ref([])
const entries = [
  { label: '我的账单', path: '/mobile/bill' },
  { label: '费用明细', path: '/mobile/bill-detail' },
  { label: '预约服务', path: '/mobile/appointments' },
  { label: '检查检验报告', path: '/mobile/reports' },
  { label: '手术安排', path: '/mobile/surgery' },
  { label: '出院服务', path: '/mobile/discharge' }
]

async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  const data = await api.home(params)
  admission.value = data.admissionInfo || {}
  bill.value = data.billSummary || {}
  recentFees.value = data.recentFees || []
}

onMounted(load)
</script>

<style scoped>
.hero,
.card,
.money-grid > div {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.hero div {
  font-size: 22px;
  font-weight: 700;
}

.hero strong,
.hero span {
  display: block;
  margin-top: 8px;
  color: #64748b;
}

.money-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.money-grid span {
  color: #64748b;
}

.money-grid strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
}

.entry-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 12px;
}

.entry-grid button {
  min-height: 70px;
  border: 1px solid #dbe4ef;
  border-radius: 12px;
  background: #fff;
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
}

.list-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-top: 1px solid #eef2f7;
}
</style>
