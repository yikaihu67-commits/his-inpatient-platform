<template>
  <div class="login-page">
    <div class="login-panel">
      <h1>住院 HIS 管理平台</h1>
      <p>毕业设计演示入口</p>
      <el-form label-width="86px">
        <el-form-item label="账号">
          <el-input v-model="username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="任意密码" show-password />
        </el-form-item>
        <el-form-item label="演示角色">
          <el-select v-model="role" style="width: 100%">
            <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" @click="enterSystem">
          进入系统
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { roles } from '../permissions'

const router = useRouter()
const username = ref('admin')
const password = ref('123456')
const role = ref(localStorage.getItem('demoRole') || 'ADMIN')

function enterSystem() {
  localStorage.setItem('demoRole', role.value)
  router.push('/app/dashboard')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #f6f9fc 0%, #e8f0f7 100%);
}

.login-panel {
  width: 420px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 34px 34px 30px;
  box-shadow: 0 14px 40px rgba(31, 41, 55, 0.08);
}

h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

p {
  margin: 0 0 28px;
  color: #6b7280;
}
</style>
