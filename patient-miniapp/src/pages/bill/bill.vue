<template>
  <view class="page">
    <view class="grid">
      <view v-for="item in cards" :key="item.label" class="card metric">
        <text class="muted">{{ item.label }}</text>
        <text class="amount">{{ money(item.value) }}</text>
      </view>
    </view>
    <button class="btn" @click="goDetail">查看费用明细</button>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { money } from '../../utils/format'

const summary = ref({})
const cards = computed(() => [
  { label: '总费用', value: summary.value.totalAmount },
  { label: '已结算', value: summary.value.settledAmount },
  { label: '未结算', value: summary.value.unsettledAmount },
  { label: '药品费用', value: summary.value.drugAmount },
  { label: '医嘱费用', value: summary.value.orderAmount },
  { label: '护理费用', value: summary.value.nursingAmount },
  { label: '手术费用', value: summary.value.surgeryAmount },
  { label: '检查费用', value: summary.value.examAmount },
  { label: '检验费用', value: summary.value.labAmount },
  { label: '其他费用', value: summary.value.otherAmount }
])

function goDetail() {
  uni.navigateTo({ url: '/pages/bill-detail/bill-detail' })
}

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  summary.value = await api.billSummary(requestParams)
}

onShow(load)
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
  margin-bottom: 20rpx;
}

.metric {
  margin-bottom: 0;
}

.amount {
  display: block;
  margin-top: 10rpx;
  font-size: 36rpx;
  font-weight: 800;
}
</style>
