<template>
  <view class="page">
    <view v-for="item in rows" :key="item.surgeryName + item.plannedTime" class="card">
      <view class="row">
        <text class="item-title">{{ item.surgeryName || '-' }}</text>
        <text class="tag" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
      </view>
      <text class="line">计划时间：{{ time(item.plannedTime) }}</text>
      <text class="line">实际开始：{{ time(item.actualStartTime) }}</text>
      <text class="line">手术室：{{ item.operatingRoom || '-' }}</text>
      <text class="line">主刀医生：{{ item.primaryDoctorName || '-' }}</text>
      <text class="line">手术费用：{{ money(item.surgeryFee) }}</text>
    </view>
    <view v-if="!rows.length" class="empty">暂无手术安排</view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { money, statusClass, statusText, time } from '../../utils/format'

const rows = ref([])

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  rows.value = await api.surgeries(requestParams)
}

onShow(load)
</script>

<style scoped>
.item-title {
  font-size: 30rpx;
  font-weight: 700;
}

.line {
  display: block;
  margin-top: 12rpx;
  color: #334155;
  font-size: 28rpx;
}
</style>
