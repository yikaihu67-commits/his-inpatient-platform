<template>
  <view class="page">
    <view v-for="item in rows" :key="item.itemName + item.requestTime" class="card">
      <view class="row">
        <text class="item-title">{{ item.itemName || '-' }}</text>
        <text class="tag" :class="item.abnormalFlag ? 'danger' : statusClass(item.status)">
          {{ item.abnormalFlag ? '异常' : statusText(item.status) }}
        </text>
      </view>
      <text class="muted">{{ typeText(item.requestType) }} / 申请 {{ time(item.requestTime) }}</text>
      <text class="muted line">报告 {{ time(item.reportTime) }} / {{ item.billed ? '已计费' : '未计费' }}</text>
      <view class="result">
        <text>{{ item.resultDetail || item.resultSummary || '暂无报告结果' }}</text>
      </view>
    </view>
    <view v-if="!rows.length" class="empty">暂无检查检验报告</view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { statusClass, statusText, time, typeText } from '../../utils/format'

const rows = ref([])

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  rows.value = await api.reports(requestParams)
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
  margin-top: 8rpx;
}

.result {
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 14rpx;
  background: #f8fafc;
  color: #334155;
  font-size: 28rpx;
  line-height: 1.6;
}
</style>
