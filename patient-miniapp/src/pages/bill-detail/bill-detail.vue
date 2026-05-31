<template>
  <view class="page">
    <view class="card filter-card">
      <picker :range="feeTypes" range-key="label" @change="changeType">
        <view class="picker">费用类型：{{ selectedType.label }}</view>
      </picker>
      <picker :range="statuses" range-key="label" @change="changeStatus">
        <view class="picker">结算状态：{{ selectedStatus.label }}</view>
      </picker>
    </view>

    <view v-for="fee in rows" :key="fee.itemName + fee.feeTime" class="card">
      <view class="row">
        <text class="item-title">{{ fee.itemName || '-' }}</text>
        <text class="amount">{{ money(fee.totalAmount) }}</text>
      </view>
      <text class="muted">{{ typeText(fee.itemCategory) }} / {{ statusText(fee.status) }} / {{ time(fee.feeTime) }}</text>
      <text class="muted detail">单价 {{ money(fee.unitPrice) }} x {{ fee.quantity || 0 }}，来源 {{ fee.sourceType || '-' }}</text>
    </view>

    <view v-if="!rows.length" class="empty">暂无费用明细</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { money, statusText, time, typeText } from '../../utils/format'

const rows = ref([])
const query = reactive({ itemCategory: '', status: '' })
const feeTypes = [
  { label: '全部', value: '' },
  { label: '药品', value: 'DRUG' },
  { label: '医嘱', value: 'ORDER' },
  { label: '护理', value: 'NURSING' },
  { label: '手术', value: 'SURGERY' },
  { label: '检查', value: 'EXAM' },
  { label: '检验', value: 'LAB' },
  { label: '其他', value: 'OTHER' }
]
const statuses = [
  { label: '全部', value: '' },
  { label: '未结算', value: 'UNSETTLED' },
  { label: '已结算', value: 'SETTLED' }
]

const selectedType = computed(() => feeTypes.find((item) => item.value === query.itemCategory) || feeTypes[0])
const selectedStatus = computed(() => statuses.find((item) => item.value === query.status) || statuses[0])

function changeType(event) {
  query.itemCategory = feeTypes[Number(event.detail.value)]?.value || ''
  load()
}

function changeStatus(event) {
  query.status = statuses[Number(event.detail.value)]?.value || ''
  load()
}

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  rows.value = await api.billDetails({ ...requestParams, ...query })
}

onShow(load)
</script>

<style scoped>
.filter-card {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
}

.picker {
  min-height: 74rpx;
  line-height: 74rpx;
  padding: 0 18rpx;
  border-radius: 14rpx;
  background: #f8fafc;
  color: #334155;
}

.item-title {
  font-size: 30rpx;
  font-weight: 700;
}

.amount {
  font-weight: 800;
}

.detail {
  display: block;
  margin-top: 8rpx;
}
</style>
