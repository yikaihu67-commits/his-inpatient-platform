<template>
  <view class="page">
    <view class="card">
      <text class="section-title">个人中心</text>
      <view class="info-row"><text>患者编号</text><text>{{ patient.patientNo || '-' }}</text></view>
      <view class="info-row"><text>姓名</text><text>{{ patient.patientName || '-' }}</text></view>
      <view class="info-row"><text>手机号</text><text>{{ patient.maskedPhone || '-' }}</text></view>
      <view class="info-row"><text>住院号</text><text>{{ patient.admissionNo || '-' }}</text></view>
      <view class="info-row"><text>科室</text><text>{{ patient.departmentName || '-' }}</text></view>
      <view class="info-row"><text>病区</text><text>{{ patient.wardName || '-' }}</text></view>
      <view class="info-row"><text>入院状态</text><text>{{ statusText(patient.admissionStatus) }}</text></view>
    </view>
    <button class="btn danger" @click="logout">退出绑定</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { clearSession, getSession } from '../../utils/session'
import { statusText } from '../../utils/format'

const patient = ref({})

function logout() {
  clearSession()
  uni.reLaunch({ url: '/pages/login/login' })
}

onShow(() => {
  patient.value = getSession()?.patient || {}
})
</script>

<style scoped>
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  padding: 18rpx 0;
  border-top: 2rpx solid #eef2f7;
  color: #334155;
  font-size: 28rpx;
}
</style>
