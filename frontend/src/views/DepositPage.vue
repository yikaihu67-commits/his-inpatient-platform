<template>
  <ManagementPage
    title="预交金管理"
    subtitle="流程：选择在院住院记录 -> 缴纳预交金；退费会校验可退余额。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :header-actions="headerActions"
    :on-form-change="handleDepositFormChange"
    :on-action-form-change="handleDepositActionChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/deposit'
import { paymentMethodOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, inHospitalAdmissionOptions, patientOptions } = useDemoOptions()

const typeOptions = [
  { label: '预交', value: 'PAY' },
  { label: '退费', value: 'REFUND' }
]
const statusOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '已取消', value: 'CANCELLED' }
]

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'transactionType', label: '交易类型', type: 'select', options: typeOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'depositNo', label: '预交金编号', minWidth: 150 },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'amount', label: '金额', format: 'money' },
  { prop: 'paymentMethod', label: '支付方式' },
  { prop: 'transactionType', label: '交易类型' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'depositNo', label: '预交金编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
  { prop: 'amount', label: '金额', type: 'number', default: 0, required: true, min: 0 },
  { prop: 'paymentMethod', label: '支付方式', type: 'select', options: paymentMethodOptions, default: 'CASH' },
  { prop: 'operatorName', label: '操作员' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const headerActions = [
  {
    label: '退预交金',
    key: 'refund',
    type: 'warning',
    fields: [
      { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
      { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
      { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
      { prop: 'amount', label: '退费金额', type: 'number', default: 0 },
      { prop: 'paymentMethod', label: '支付方式', type: 'select', options: paymentMethodOptions, default: 'CASH' },
      { prop: 'operatorName', label: '操作员' }
    ]
  }
]

const actions = [
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => row.status !== 'CANCELLED' }
]

function fillAdmission(form) {
  const admission = findAdmission(form.admissionId)
  if (admission) {
    form.patientId = admission.patientId
    form.admissionContext = `${admission.admissionNo || '-'} / ${admission.patientName || '-'} / ${admission.status}`
  }
}

function handleDepositFormChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) fillAdmission(form)
}

function handleDepositActionChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) fillAdmission(form)
}
</script>
