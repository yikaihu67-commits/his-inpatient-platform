<template>
  <ManagementPage title="科室管理" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" :row-actions="actions" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/department'

const typeOptions = ['OUTPATIENT', 'INPATIENT', 'MEDICAL_TECH', 'PHARMACY', 'ADMIN', 'OTHER'].map((value) => ({ label: value, value }))
const statusOptions = ['ENABLED', 'DISABLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'deptCode', label: '科室编码' },
  { prop: 'deptName', label: '科室名称' },
  { prop: 'deptType', label: '科室类型', type: 'select', options: typeOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'deptCode', label: '科室编码' },
  { prop: 'deptName', label: '科室名称', minWidth: 150 },
  { prop: 'deptType', label: '科室类型' },
  { prop: 'parentId', label: '上级科室ID' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'deptCode', label: '科室编码', required: true },
  { prop: 'deptName', label: '科室名称', required: true },
  { prop: 'deptType', label: '科室类型', type: 'select', options: typeOptions, default: 'INPATIENT' },
  { prop: 'parentId', label: '上级科室ID', type: 'number' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'ENABLED' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '启用', key: 'enable', show: (row) => row.status === 'DISABLED' },
  { label: '停用', key: 'disable', type: 'warning', show: (row) => row.status === 'ENABLED' }
]
</script>
