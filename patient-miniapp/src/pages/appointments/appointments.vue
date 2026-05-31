<template>
  <view class="page">
    <button class="btn" @click="create">新增预约</button>

    <view v-for="item in rows" :key="item.id" class="card">
      <view class="row">
        <text class="item-title">{{ typeText(item.appointmentType) }}</text>
        <text class="tag" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
      </view>
      <text class="content">{{ item.appointmentItem || '-' }}</text>
      <text class="muted">{{ item.appointmentDate || '-' }} {{ item.timeSlot || '' }} / {{ item.contactPhone || '-' }}</text>
      <button v-if="canCancel(item)" class="btn danger small-btn" :loading="cancellingId === item.id" @click="cancel(item)">
        取消预约
      </button>
    </view>

    <view v-if="!rows.length" class="empty">暂无预约记录</view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { statusClass, statusText, typeText } from '../../utils/format'

const rows = ref([])
const cancellingId = ref(null)

function create() {
  uni.navigateTo({ url: '/pages/appointment-new/appointment-new' })
}

function canCancel(item) {
  return ['REQUESTED', 'CONFIRMED'].includes(item.status)
}

async function cancel(item) {
  cancellingId.value = item.id
  try {
    await api.cancelAppointment(item.id)
    uni.showToast({ title: '预约已取消', icon: 'success' })
    await load()
  } finally {
    cancellingId.value = null
  }
}

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  rows.value = await api.appointments(requestParams)
}

onShow(load)
</script>

<style scoped>
.btn {
  margin-bottom: 20rpx;
}

.item-title {
  font-size: 30rpx;
  font-weight: 700;
}

.content {
  display: block;
  margin: 16rpx 0 8rpx;
  color: #334155;
}

.small-btn {
  margin-top: 16rpx;
}
</style>
