<template>
  <MobileShell title="新增预约">
    <el-form label-position="top" class="card">
      <el-form-item label="预约类型">
        <el-select v-model="form.appointmentType" style="width: 100%">
          <el-option label="检查预约" value="EXAM_APPOINTMENT" />
          <el-option label="检验预约" value="LAB_APPOINTMENT" />
          <el-option label="出院结算预约" value="DISCHARGE_SETTLEMENT" />
          <el-option label="复诊预约" value="FOLLOW_UP" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="预约项目"><el-input v-model="form.appointmentItem" /></el-form-item>
      <el-form-item label="预约日期"><el-date-picker v-model="form.appointmentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      <el-form-item label="时间段">
        <el-select v-model="form.timeSlot" style="width: 100%">
          <el-option label="上午" value="上午" />
          <el-option label="下午" value="下午" />
          <el-option label="晚上" value="晚上" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      <el-button type="primary" class="full" :loading="saving" @click="save">提交预约</el-button>
    </el-form>
  </MobileShell>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api/patientMobile'
import { getMobileSession, mobileParams } from '../../mobileSession'
import MobileShell from './MobileShell.vue'

const router = useRouter()
const saving = ref(false)
const session = getMobileSession()?.patient || {}
const form = reactive({
  appointmentType: 'EXAM_APPOINTMENT',
  appointmentItem: '',
  appointmentDate: '',
  timeSlot: '上午',
  contactPhone: session.maskedPhone?.replace('****', '') || '',
  remark: ''
})

async function save() {
  const params = mobileParams()
  if (!params) return router.push('/mobile/login')
  if (!form.appointmentItem || !form.appointmentDate || !form.timeSlot) {
    ElMessage.warning('请填写预约项目、日期和时间段')
    return
  }
  saving.value = true
  try {
    await api.createAppointment({ ...form, ...params })
    ElMessage.success('预约已提交')
    router.push('/mobile/appointments')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 14px; }
.full { width: 100%; }
</style>
