<template>
  <view class="page">
    <view class="card">
      <view class="row">
        <text class="section-title">出院状态</text>
        <text class="tag" :class="statusClass(info.status)">{{ statusText(info.status) }}</text>
      </view>
      <text class="line">待结算金额：{{ money(info.unpaidAmount) }}</text>
      <text class="line">是否可预约结算：{{ info.canSettle ? '可以' : '暂不可办理' }}</text>
      <text class="line">出院时间：{{ time(info.dischargeTime) }}</text>
      <button class="btn ghost appoint-btn" @click="goAppointment">预约出院结算</button>
    </view>

    <view class="card">
      <text class="section-title">出院诊断</text>
      <text class="content">{{ info.dischargeDiagnosis || '-' }}</text>
      <text class="section-title sub">出院小结</text>
      <text class="content">{{ info.dischargeSummary || '-' }}</text>
      <text class="section-title sub">出院医嘱</text>
      <text class="content">{{ info.dischargeOrder || '-' }}</text>
      <text class="section-title sub">出院带药</text>
      <text class="content">{{ info.takeHomeDrugInstruction || '-' }}</text>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { api } from '../../utils/api'
import { params } from '../../utils/session'
import { money, statusClass, statusText, time } from '../../utils/format'

const info = ref({})

function goAppointment() {
  uni.navigateTo({ url: '/pages/appointment-new/appointment-new' })
}

async function load() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  info.value = await api.discharge(requestParams)
}

onShow(load)
</script>

<style scoped>
.line {
  display: block;
  margin-top: 12rpx;
  color: #334155;
  font-size: 28rpx;
}

.appoint-btn {
  margin-top: 20rpx;
}

.sub {
  margin-top: 24rpx;
}

.content {
  display: block;
  color: #334155;
  font-size: 28rpx;
  line-height: 1.7;
}
</style>
