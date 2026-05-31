<template>
  <div class="mobile-shell">
    <header>
      <button type="button" @click="$router.back()">‹</button>
      <h1>{{ title }}</h1>
      <button type="button" @click="logout">退出</button>
    </header>
    <main>
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { clearMobileSession } from '../../mobileSession'

defineProps({ title: { type: String, required: true } })
const router = useRouter()

function logout() {
  clearMobileSession()
  router.push('/mobile/login')
}
</script>

<style scoped>
.mobile-shell {
  min-height: 100vh;
  background: #f3f6fb;
}

header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 14px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

h1 {
  margin: 0;
  font-size: 18px;
}

button {
  border: 0;
  background: transparent;
  color: #2563eb;
  font-size: 16px;
}

main {
  padding: 14px;
}
</style>
