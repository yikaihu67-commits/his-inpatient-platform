<template>
  <view class="page">
    <view class="hero card">
      <view>
        <text class="hello">{{ patient.patientName || '-' }}</text>
        <text class="muted">{{ admission.departmentName || patient.departmentName || '-' }} / {{ admission.bedNo || '-' }}</text>
      </view>
      <text class="tag" :class="statusClass(admission.admissionStatus || patient.admissionStatus)">
        {{ statusText(admission.admissionStatus || patient.admissionStatus) }}
      </text>
    </view>

    <view class="money-grid">
      <view class="card">
        <text class="muted">总费用</text>
        <text class="amount">{{ money(bill.totalAmount) }}</text>
      </view>
      <view class="card">
        <text class="muted">未结算</text>
        <text class="amount warning-text">{{ money(bill.unsettledAmount) }}</text>
      </view>
    </view>

    <view class="entry-grid">
      <button v-for="item in entries" :key="item.url" class="entry" @click="go(item.url)">
        {{ item.label }}
      </button>
    </view>

    <view class="card">
      <view class="row">
        <text class="section-title">最近费用</text>
        <text class="muted" @click="go('/pages/bill-detail/bill-detail')">全部</text>
      </view>
      <view v-for="fee in recentFees" :key="fee.itemName + fee.feeTime" class="list-row">
        <view>
          <text class="item-title">{{ fee.itemName || '-' }}</text>
          <text class="muted">{{ typeText(fee.itemCategory) }} / {{ time(fee.feeTime) }}</text>
        </view>
        <text class="money">{{ money(fee.totalAmount) }}</text>
      </view>
      <view v-if="!recentFees.length && !loading" class="empty">暂无费用记录</view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { api } from '../../utils/api'
import { getSession, params } from '../../utils/session'
import { money, statusClass, statusText, time, typeText } from '../../utils/format'

const loading = ref(false)
const patient = ref(getSession()?.patient || {})
const admission = ref({})
const bill = ref({})
const recentFees = ref([])
const entries = [
  { label: '我的账单', url: '/pages/bill/bill' },
  { label: '费用明细', url: '/pages/bill-detail/bill-detail' },
  { label: '预约服务', url: '/pages/appointments/appointments' },
  { label: '检查检验报告', url: '/pages/reports/reports' },
  { label: '手术安排', url: '/pages/surgery/surgery' },
  { label: '出院服务', url: '/pages/discharge/discharge' },
  { label: '个人中心', url: '/pages/profile/profile' }
]

function go(url) {
  uni.navigateTo({ url })
}

async function load() {
  const requestParams = params()
  if (!requestParams) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  loading.value = true
  try {
    const data = await api.profile(requestParams)
    admission.value = data.admissionInfo || {}
    bill.value = data.billSummary || {}
    recentFees.value = data.recentFees || []
  } finally {
    loading.value = false
  }
}

onShow(load)
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.hello {
  display: block;
  margin-bottom: 10rpx;
  font-size: 42rpx;
  font-weight: 800;
}

.money-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.amount {
  display: block;
  margin-top: 10rpx;
  font-size: 40rpx;
  font-weight: 800;
}

.warning-text {
  color: #b45309;
}

.entry-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-bottom: 20rpx;
}

.entry {
  min-height: 112rpx;
  line-height: 112rpx;
  border-radius: 18rpx;
  background: #fff;
  color: #0f172a;
  font-size: 30rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 22rpx rgba(15, 23, 42, 0.05);
}

.list-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  padding: 18rpx 0;
  border-top: 2rpx solid #eef2f7;
}

.item-title {
  display: block;
  margin-bottom: 8rpx;
  font-size: 30rpx;
  font-weight: 700;
}

.money {
  font-weight: 800;
}
</style>
