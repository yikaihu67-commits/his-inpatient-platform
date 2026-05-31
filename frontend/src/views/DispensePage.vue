<template>
  <ManagementPage title="药房发药" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" :row-actions="actions" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/dispense'

const statusOptions = ['CREATED', 'DISPENSED', 'RETURNED', 'CANCELLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'orderId', label: '医嘱ID' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'dispenseNo', label: '发药单号', minWidth: 150 },
  { prop: 'drugName', label: '药品名称', minWidth: 160 },
  { prop: 'quantity', label: '数量' },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'pharmacistName', label: '药师' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'dispenseNo', label: '发药单号', placeholder: '不填则后端自动生成' },
  { prop: 'orderId', label: '医嘱ID', type: 'number', required: true },
  { prop: 'drugId', label: '药品ID', type: 'number', required: true },
  { prop: 'admissionId', label: '住院ID', type: 'number', required: true },
  { prop: 'patientId', label: '患者ID', type: 'number', required: true },
  { prop: 'drugName', label: '药品名称' },
  { prop: 'quantity', label: '数量', type: 'number', required: true, min: 1 },
  { prop: 'pharmacistName', label: '药师' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'CREATED' }
]

const actions = [
  { label: '发药', key: 'dispense', show: (row) => row.status === 'CREATED', fields: [{ prop: 'pharmacistName', label: '发药药师' }] },
  { label: '退药', key: 'returnDrug', type: 'warning', show: (row) => row.status === 'DISPENSED' },
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => row.status === 'CREATED' }
]
</script>
