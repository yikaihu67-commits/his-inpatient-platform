<template>
  <ManagementPage title="基础字典" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/dict'

const statusOptions = ['ENABLED', 'DISABLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'dictType', label: '字典类型' },
  { prop: 'dictCode', label: '字典编码' },
  { prop: 'dictName', label: '字典名称' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'dictType', label: '字典类型' },
  { prop: 'dictCode', label: '字典编码' },
  { prop: 'dictName', label: '字典名称' },
  { prop: 'sortOrder', label: '排序' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'dictType', label: '字典类型', required: true },
  { prop: 'dictCode', label: '字典编码', required: true },
  { prop: 'dictName', label: '字典名称', required: true },
  { prop: 'sortOrder', label: '排序', type: 'number', default: 0 },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'ENABLED' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]
</script>
