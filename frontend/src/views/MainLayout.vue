<template>
  <el-container class="layout">
    <el-aside width="230px" class="sidebar">
      <div class="brand">住院 HIS 管理平台</div>
      <el-menu
        :default-active="route.path"
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#fff"
        router
      >
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div class="topbar-title">{{ currentTitle }}</div>
        <div class="topbar-actions">
          <span class="role-label">当前角色</span>
          <el-select v-model="role" size="small" style="width: 160px" @change="changeRole">
            <el-option v-for="item in roles" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button @click="router.push('/login')">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view :key="route.fullPath" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { menus, roles } from '../permissions'

const route = useRoute()
const router = useRouter()
const role = ref(localStorage.getItem('demoRole') || 'ADMIN')

const visibleMenus = computed(() => menus.filter((item) => role.value === 'ADMIN' || item.roles.includes(role.value)))
const currentTitle = computed(() => menus.find((item) => item.path === route.path)?.label || '首页工作台')

function changeRole(value) {
  localStorage.setItem('demoRole', value)
  window.dispatchEvent(new CustomEvent('demo-role-change', { detail: value }))
  if (!visibleMenus.value.some((item) => item.path === route.path)) {
    router.push('/app/dashboard')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.sidebar {
  background: #1f2937;
}

.brand {
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 700;
}

.topbar-title {
  font-size: 16px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-label {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}

:deep(.el-menu) {
  border-right: 0;
}
</style>
