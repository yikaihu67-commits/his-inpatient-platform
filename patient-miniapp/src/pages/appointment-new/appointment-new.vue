<template>
  <view class="page">
    <view class="card">
      <view class="field">
        <text class="label">预约类型</text>
        <picker :range="types" range-key="label" @change="changeType">
          <view class="input picker">{{ selectedType.label }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">预约项目</text>
        <input class="input" v-model="form.appointmentItem" placeholder="例如：胸部 CT、出院结算" />
      </view>
      <view class="field">
        <text class="label">预约日期</text>
        <picker mode="date" @change="changeDate">
          <view class="input picker">{{ form.appointmentDate || '请选择日期' }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">时间段</text>
        <picker :range="timeSlots" @change="changeSlot">
          <view class="input picker">{{ form.timeSlot }}</view>
        </picker>
      </view>
      <view class="field">
        <text class="label">联系电话</text>
        <input class="input" v-model="form.contactPhone" type="number" maxlength="11" placeholder="请输入联系电话" />
      </view>
      <view class="field">
        <text class="label">备注</text>
        <textarea class="textarea" v-model="form.remark" placeholder="可填写特殊说明" />
      </view>
      <button class="btn" :loading="saving" @click="save">提交预约</button>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { api } from '../../utils/api'
import { getSession, params } from '../../utils/session'

const saving = ref(false)
const sessionPatient = getSession()?.patient || {}
const types = [
  { label: '检查预约', value: 'EXAM_APPOINTMENT' },
  { label: '检验预约', value: 'LAB_APPOINTMENT' },
  { label: '出院结算预约', value: 'DISCHARGE_SETTLEMENT' },
  { label: '复诊预约', value: 'FOLLOW_UP' },
  { label: '其他', value: 'OTHER' }
]
const timeSlots = ['上午', '下午', '晚上']
const form = reactive({
  appointmentType: 'EXAM_APPOINTMENT',
  appointmentItem: '',
  appointmentDate: '',
  timeSlot: '上午',
  contactPhone: sessionPatient.maskedPhone?.includes('*') ? '' : (sessionPatient.maskedPhone || ''),
  remark: ''
})
const selectedType = computed(() => types.find((item) => item.value === form.appointmentType) || types[0])

function changeType(event) {
  form.appointmentType = types[Number(event.detail.value)]?.value || 'EXAM_APPOINTMENT'
}

function changeDate(event) {
  form.appointmentDate = event.detail.value
}

function changeSlot(event) {
  form.timeSlot = timeSlots[Number(event.detail.value)] || '上午'
}

async function save() {
  const requestParams = params()
  if (!requestParams) return uni.reLaunch({ url: '/pages/login/login' })
  if (!form.appointmentItem || !form.appointmentDate || !form.timeSlot) {
    uni.showToast({ title: '请填写预约项目、日期和时间段', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await api.createAppointment({ ...requestParams, ...form })
    uni.showToast({ title: '预约已提交', icon: 'success' })
    uni.navigateBack()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.picker {
  line-height: 84rpx;
}

.textarea {
  width: 100%;
  min-height: 150rpx;
  box-sizing: border-box;
  padding: 18rpx 20rpx;
  border: 2rpx solid #dbe4ef;
  border-radius: 14rpx;
  background: #fff;
  font-size: 30rpx;
}
</style>
