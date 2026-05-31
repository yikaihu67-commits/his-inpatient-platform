<template>
  <div class="not-found">
    <h2>页面不存在或无权限访问</h2>
    <p>请从左侧菜单进入功能页面，或切换到有权限的演示角色。</p>
    <el-button type="primary" @click="$router.push('/app/dashboard')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.not-found p {
  color: #6b7280;
}
</style>
