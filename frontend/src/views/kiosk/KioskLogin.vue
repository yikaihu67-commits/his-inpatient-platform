<template>
  <div class="kiosk-login">
    <section class="login-card">
      <div class="title-block">
        <div class="eyebrow">住院 HIS 患者自助服务</div>
        <h1>请输入住院信息</h1>
        <p>用于查询住院信息、费用、检查检验、手术安排和出院状态。</p>
      </div>
      <el-form class="kiosk-form" label-position="top">
        <el-form-item label="住院记录 ID">
          <el-input v-model="form.admissionId" size="large" placeholder="例如：1" inputmode="numeric" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" size="large" placeholder="请输入住院登记手机号" inputmode="tel" />
        </el-form-item>
        <el-form-item label="身份证号后四位">
          <el-input v-model="form.idCardLast4" size="large" placeholder="例如：0001" maxlength="4" inputmode="numeric" />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input v-model="form.patientName" size="large" placeholder="可选，例如：李四" />
        </el-form-item>
        <el-alert v-if="errorMessage" type="warning" :title="errorMessage" show-icon :closable="false" />
        <el-button class="login-button" type="primary" size="large" :loading="loading" @click="verify">
          开始查询
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientSelfService'
import { setKioskSession } from '../../kioskSession'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  admissionId: '',
  phone: '',
  idCardLast4: '',
  patientName: ''
})

async function verify() {
  errorMessage.value = ''
  if (!form.admissionId && !form.phone && !form.idCardLast4 && !form.patientName) {
    errorMessage.value = '请至少输入一项身份信息'
    return
  }
  loading.value = true
  try {
    const session = await api.verify({
      admissionId: form.admissionId ? Number(form.admissionId) : undefined,
      phone: form.phone || undefined,
      idCardLast4: form.idCardLast4 || undefined,
      patientName: form.patientName || undefined
    })
    setKioskSession(session)
    router.push('/kiosk/home')
  } catch (error) {
    errorMessage.value = error?.message || '未找到匹配患者'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.kiosk-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
  background: #eef3f8;
}

.login-card {
  width: min(760px, 100%);
  background: #fff;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  padding: 38px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.08);
}

.title-block {
  margin-bottom: 24px;
}

.eyebrow {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

h1 {
  margin: 8px 0;
  font-size: 42px;
}

p {
  margin: 0;
  color: #64748b;
  font-size: 20px;
}

.kiosk-form :deep(.el-form-item__label) {
  font-size: 20px;
  font-weight: 700;
}

.kiosk-form :deep(.el-input__wrapper) {
  min-height: 58px;
  font-size: 22px;
}

.login-button {
  width: 100%;
  height: 64px;
  margin-top: 18px;
  font-size: 24px;
}
</style>
