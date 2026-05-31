<template>
  <KioskShell title="出院状态">
    <el-skeleton :loading="loading" animated>
      <section class="discharge-grid">
        <div class="state-card">
          <span>出院申请状态</span>
          <strong><el-tag size="large" :type="statusType(info.status)">{{ statusText(info.status) }}</el-tag></strong>
        </div>
        <div class="state-card">
          <span>待结算金额</span>
          <strong>{{ money(info.unpaidAmount) }}</strong>
        </div>
        <div class="state-card">
          <span>是否可办理出院结算</span>
          <strong>{{ info.canSettle ? '可办理' : '暂不可办理' }}</strong>
        </div>
      </section>
      <section class="text-panel">
        <h2>出院诊断</h2>
        <p>{{ text(info.dischargeDiagnosis) }}</p>
        <h2>出院小结</h2>
        <p>{{ text(info.dischargeSummary) }}</p>
        <h2>出院医嘱</h2>
        <p>{{ text(info.dischargeOrder) }}</p>
        <h2>出院带药说明</h2>
        <p>{{ text(info.takeHomeDrugInstruction) }}</p>
      </section>
    </el-skeleton>
  </KioskShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { sessionParams } from '../../kioskSession'
import KioskShell from './KioskShell.vue'
import { money, statusText, statusType, text } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const info = ref({})

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    info.value = await api.discharge(params)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.discharge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.state-card,
.text-panel {
  background: #fff;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  padding: 24px;
}

.state-card span {
  color: #64748b;
  font-size: 19px;
}

.state-card strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
}

.text-panel h2 {
  margin: 0 0 8px;
}

.text-panel p {
  margin: 0 0 22px;
  color: #334155;
  font-size: 21px;
  line-height: 1.7;
}
</style>
