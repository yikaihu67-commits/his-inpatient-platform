<template>
  <MobileShell title="我的账单">
    <section class="grid">
      <div><span>总费用</span><strong>{{ money(summary.totalAmount) }}</strong></div>
      <div><span>已结算</span><strong>{{ money(summary.settledAmount) }}</strong></div>
      <div><span>未结算</span><strong>{{ money(summary.unsettledAmount) }}</strong></div>
      <div><span>药品费用</span><strong>{{ money(summary.drugAmount) }}</strong></div>
      <div><span>护理费用</span><strong>{{ money(summary.nursingAmount) }}</strong></div>
      <div><span>手术费用</span><strong>{{ money(summary.surgeryAmount) }}</strong></div>
      <div><span>检查费用</span><strong>{{ money(summary.examAmount) }}</strong></div>
      <div><span>检验费用</span><strong>{{ money(summary.labAmount) }}</strong></div>
    </section>
  </MobileShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { money } from './mobileFormat'

const router = useRouter()
const summary = ref({})
async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  summary.value = await api.billSummary(params)
}
onMounted(load)
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.grid div {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}
span { color: #64748b; }
strong { display: block; margin-top: 8px; font-size: 21px; }
</style>
