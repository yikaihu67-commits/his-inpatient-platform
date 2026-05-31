<template>
  <KioskShell title="账单查询">
    <el-skeleton :loading="loading" animated>
      <section class="summary-grid">
        <div class="summary-card primary">
          <span>总费用</span>
          <strong>{{ money(summary.totalAmount) }}</strong>
        </div>
        <div class="summary-card">
          <span>已结算费用</span>
          <strong>{{ money(summary.settledAmount) }}</strong>
        </div>
        <div class="summary-card warning">
          <span>未结算费用</span>
          <strong>{{ money(summary.unsettledAmount) }}</strong>
        </div>
      </section>
      <section class="category-grid">
        <div v-for="item in categories" :key="item.label" class="category-card">
          <span>{{ item.label }}</span>
          <strong>{{ money(item.value) }}</strong>
        </div>
      </section>
    </el-skeleton>
  </KioskShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { sessionParams } from '../../kioskSession'
import KioskShell from './KioskShell.vue'
import { money } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const summary = ref({})
const categories = computed(() => [
  { label: '药品费用', value: summary.value.drugAmount },
  { label: '医嘱/处置费用', value: summary.value.orderAmount },
  { label: '护理费用', value: summary.value.nursingAmount },
  { label: '手术费用', value: summary.value.surgeryAmount },
  { label: '检查费用', value: summary.value.examAmount },
  { label: '检验费用', value: summary.value.labAmount },
  { label: '其他费用', value: summary.value.otherAmount }
])

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    summary.value = await api.billSummary(params)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.summary-grid,
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card,
.category-card {
  background: #fff;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  padding: 24px;
}

.primary {
  border-color: #2563eb;
}

.warning {
  border-color: #f59e0b;
}

span {
  display: block;
  color: #64748b;
  font-size: 19px;
}

strong {
  display: block;
  margin-top: 10px;
  font-size: 34px;
}
</style>
