<template>
  <MobileShell title="费用明细">
    <div class="filters">
      <el-select v-model="query.itemCategory" clearable placeholder="类型" @change="load">
        <el-option label="药品" value="DRUG" />
        <el-option label="检查" value="EXAM" />
        <el-option label="检验" value="LAB" />
        <el-option label="护理" value="NURSING" />
        <el-option label="手术" value="SURGERY" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" @change="load">
        <el-option label="未结算" value="UNSETTLED" />
        <el-option label="已结算" value="SETTLED" />
      </el-select>
    </div>
    <div v-for="fee in rows" :key="fee.itemName + fee.feeTime" class="card">
      <div class="row"><strong>{{ fee.itemName }}</strong><b>{{ money(fee.totalAmount) }}</b></div>
      <div class="muted">{{ fee.itemCategory }} / {{ statusText(fee.status) }} / {{ time(fee.feeTime) }}</div>
    </div>
    <el-empty v-if="!rows.length" description="暂无费用明细" />
  </MobileShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'
import { money, statusText, time } from './mobileFormat'

const router = useRouter()
const rows = ref([])
const query = reactive({ itemCategory: '', status: '' })
async function load() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  rows.value = await api.billDetails({ ...params, ...query })
}
onMounted(load)
</script>

<style scoped>
.filters { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 10px; }
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; margin-bottom: 10px; }
.row { display: flex; justify-content: space-between; gap: 10px; }
.muted { margin-top: 8px; color: #64748b; font-size: 13px; }
</style>
