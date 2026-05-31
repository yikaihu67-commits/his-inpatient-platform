<template>
  <KioskShell title="住院信息查询">
    <el-skeleton :loading="loading" animated>
      <section class="info-grid">
        <div v-for="item in fields" :key="item.label" class="info-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
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
import { statusText, text, time } from './kioskFormat'

const router = useRouter()
const loading = ref(false)
const info = ref({})
const fields = computed(() => [
  { label: '患者姓名', value: text(info.value.patientName) },
  { label: '性别', value: statusText(info.value.gender) },
  { label: '年龄', value: text(info.value.age) },
  { label: '科室', value: text(info.value.departmentName) },
  { label: '病区', value: text(info.value.wardName) },
  { label: '床位', value: `${text(info.value.roomNo)} / ${text(info.value.bedNo)}` },
  { label: '入院时间', value: time(info.value.admissionTime) },
  { label: '主治医生', value: text(info.value.doctorName) },
  { label: '住院状态', value: statusText(info.value.admissionStatus) },
  { label: '出院状态', value: statusText(info.value.dischargeStatus) }
])

async function load() {
  const params = sessionParams()
  if (!params) return router.push('/kiosk/login')
  loading.value = true
  try {
    info.value = await api.admissionInfo(params)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.info-card {
  background: #fff;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  padding: 24px;
}

.info-card span {
  color: #64748b;
  font-size: 19px;
}

.info-card strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}
</style>
