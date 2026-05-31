<template>
  <view class="page login-page">
    <view class="brand-card">
      <text class="title">患者移动服务</text>
      <text class="subtitle">绑定住院信息后，可查询账单、报告、手术安排和预约服务。</text>
    </view>

    <view class="demo-card">
      <text class="demo-title">本地演示患者</text>
      <text class="demo-line">住院号：ZY-DEMO-002</text>
      <text class="demo-line">患者编号：P-DEMO-002</text>
      <text class="demo-line">手机号：13900000002，身份证后四位：0002</text>
    </view>

    <view class="card">
      <view class="field">
        <text class="label">住院号</text>
        <input class="input" v-model="form.admissionNo" placeholder="ZY-DEMO-002" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input class="input" v-model="form.phone" type="number" maxlength="11" placeholder="13900000002" />
      </view>
      <view class="field">
        <text class="label">身份证后四位</text>
        <input class="input" v-model="form.idCardLast4" type="number" maxlength="4" placeholder="0002" />
      </view>
      <button class="btn" :loading="loading" @click="login">绑定并进入</button>
      <button class="btn ghost demo-btn" @click="fillDemo">填入演示账号</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api } from '../../utils/api'
import { setSession } from '../../utils/session'

const loading = ref(false)
const form = reactive({
  admissionNo: 'ZY-DEMO-002',
  phone: '13900000002',
  idCardLast4: '0002'
})

function fillDemo() {
  form.admissionNo = 'ZY-DEMO-002'
  form.phone = '13900000002'
  form.idCardLast4 = '0002'
}

async function login() {
  if (!form.admissionNo || !form.phone || !form.idCardLast4) {
    uni.showToast({ title: '请填写住院号、手机号和身份证后四位', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await api.login({
      admissionNo: form.admissionNo,
      phone: form.phone,
      idCardLast4: form.idCardLast4
    })
    setSession(data)
    uni.reLaunch({ url: '/pages/home/home' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  padding-top: 72rpx;
}

.brand-card {
  margin-bottom: 28rpx;
}

.title {
  display: block;
  color: #0f172a;
  font-size: 48rpx;
  font-weight: 800;
}

.subtitle {
  display: block;
  margin-top: 14rpx;
  color: #64748b;
  font-size: 28rpx;
  line-height: 1.6;
}

.demo-card {
  margin-bottom: 20rpx;
  padding: 20rpx;
  border: 2rpx dashed #93c5fd;
  border-radius: 18rpx;
  background: #eff6ff;
}

.demo-title,
.demo-line {
  display: block;
}

.demo-title {
  margin-bottom: 8rpx;
  color: #1d4ed8;
  font-size: 28rpx;
  font-weight: 700;
}

.demo-line {
  color: #334155;
  font-size: 26rpx;
  line-height: 1.7;
}

.demo-btn {
  margin-top: 18rpx;
}
</style>
