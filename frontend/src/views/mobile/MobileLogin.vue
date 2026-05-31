<template>
  <div class="mobile-login">
    <section class="card">
      <h1>患者移动服务</h1>
      <p>绑定住院信息后查询账单、报告、手术和预约服务。</p>
      <el-form label-position="top">
        <el-form-item label="住院记录 ID">
          <el-input v-model="form.admissionId" size="large" placeholder="例如：1" inputmode="numeric" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" size="large" placeholder="住院登记手机号" inputmode="tel" />
        </el-form-item>
        <el-form-item label="身份证后四位">
          <el-input v-model="form.idCardLast4" size="large" maxlength="4" inputmode="numeric" placeholder="例如：0001" />
        </el-form-item>
        <el-button type="primary" size="large" class="full" :loading="loading" @click="login">绑定并进入</el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api/patientMobile'
import { setMobileSession } from '../../mobileSession'

const router = useRouter()
const loading = ref(false)
const form = reactive({ admissionId: '', phone: '', idCardLast4: '' })

async function login() {
  loading.value = true
  try {
    const data = await api.login({
      admissionId: form.admissionId ? Number(form.admissionId) : undefined,
      phone: form.phone || undefined,
      idCardLast4: form.idCardLast4 || undefined
    })
    setMobileSession(data)
    router.push('/mobile/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.mobile-login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 16px;
  background: #f3f6fb;
}

.card {
  width: 100%;
  max-width: 420px;
  padding: 22px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

h1 {
  margin: 0 0 8px;
}

p {
  margin: 0 0 18px;
  color: #64748b;
}

.full {
  width: 100%;
}
</style>
