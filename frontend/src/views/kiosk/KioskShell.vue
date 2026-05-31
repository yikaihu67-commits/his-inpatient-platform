<template>
  <div class="kiosk-shell">
    <header class="kiosk-header">
      <div>
        <div class="kiosk-eyebrow">患者自助服务</div>
        <h1>{{ title }}</h1>
      </div>
      <div class="kiosk-header-actions">
        <el-button size="large" @click="goHome">返回首页</el-button>
        <el-button size="large" type="danger" plain @click="logout">退出查询</el-button>
      </div>
    </header>
    <main>
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { clearKioskSession } from '../../kioskSession'

defineProps({
  title: { type: String, required: true }
})

const router = useRouter()

function goHome() {
  router.push('/kiosk/home')
}

function logout() {
  clearKioskSession()
  router.push('/kiosk/login')
}
</script>

<style scoped>
.kiosk-shell {
  min-height: 100vh;
  padding: 28px;
  background: #eef3f8;
}

.kiosk-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
}

.kiosk-eyebrow {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
}

h1 {
  margin: 4px 0 0;
  font-size: 38px;
}

.kiosk-header-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 760px) {
  .kiosk-shell {
    padding: 18px;
  }

  .kiosk-header {
    align-items: stretch;
    flex-direction: column;
  }

  h1 {
    font-size: 30px;
  }
}
</style>
