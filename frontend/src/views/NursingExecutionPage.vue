<template>
  <ManagementPage title="护士执行" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" :row-actions="actions" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/nursingExecution'
import { useDemoOptions } from '../useDemoOptions'

const { activeAdmissionOptions, nurseNameOptions, patientOptions } = useDemoOptions()

const statusOptions = ['PENDING', 'EXECUTED', 'FAILED', 'CANCELLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: activeAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'orderId', label: '医嘱ID' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'executionNo', label: '执行编号', minWidth: 150 },
  { prop: 'orderId', label: '医嘱ID' },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'itemName', label: '项目名称', minWidth: 160 },
  { prop: 'nurseName', label: '护士' },
  { prop: 'executeTime', label: '执行时间', format: 'time', minWidth: 160 },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'executionNo', label: '执行编号', placeholder: '不填则后端自动生成' },
  { prop: 'orderId', label: '医嘱ID', type: 'number', required: true },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: activeAdmissionOptions, required: true },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'itemName', label: '项目名称', required: true },
  { prop: 'nurseName', label: '护士', type: 'select', options: nurseNameOptions, allowCreate: true },
  { prop: 'plannedTime', label: '计划时间', type: 'datetime', inputType: 'datetime' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'PENDING' }
]

const actions = [
  { label: '执行', key: 'execute', show: (row) => row.status === 'PENDING', fields: [{ prop: 'nurseName', label: '执行护士', type: 'select', options: nurseNameOptions, allowCreate: true }] },
  { label: '标记失败', key: 'fail', type: 'warning', show: (row) => row.status === 'PENDING', fields: [{ prop: 'failureReason', label: '失败原因', type: 'textarea' }] },
  { label: '取消执行', key: 'cancel', type: 'danger', show: (row) => row.status === 'PENDING' }
]
</script>
