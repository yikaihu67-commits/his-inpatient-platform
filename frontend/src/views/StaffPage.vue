<template>
  <ManagementPage title="人员管理" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" :row-actions="actions" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/staff'

const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]
const roleOptions = ['DOCTOR', 'NURSE', 'PHARMACIST', 'TECHNICIAN', 'CASHIER', 'ADMIN', 'OTHER'].map((value) => ({ label: value, value }))
const statusOptions = ['ENABLED', 'DISABLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'staffNo', label: '工号' },
  { prop: 'staffName', label: '姓名' },
  { prop: 'departmentId', label: '科室ID' },
  { prop: 'roleType', label: '角色', type: 'select', options: roleOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'staffNo', label: '工号' },
  { prop: 'staffName', label: '姓名' },
  { prop: 'gender', label: '性别' },
  { prop: 'departmentId', label: '科室ID' },
  { prop: 'roleType', label: '角色' },
  { prop: 'title', label: '职称' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'staffNo', label: '工号', required: true },
  { prop: 'staffName', label: '姓名', required: true },
  { prop: 'gender', label: '性别', type: 'select', options: genderOptions },
  { prop: 'phone', label: '手机号' },
  { prop: 'departmentId', label: '科室ID', type: 'number', required: true },
  { prop: 'roleType', label: '角色', type: 'select', options: roleOptions, default: 'DOCTOR' },
  { prop: 'title', label: '职称' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'ENABLED' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '启用', key: 'enable', show: (row) => row.status === 'DISABLED' },
  { label: '停用', key: 'disable', type: 'warning', show: (row) => row.status === 'ENABLED' }
]
</script>
