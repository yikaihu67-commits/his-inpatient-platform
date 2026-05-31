<template>
  <ManagementPage title="药品管理" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="formFields" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/drug'

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' }
]

const searchFields = [
  { prop: 'drugCode', label: '药品编码', placeholder: '请输入药品编码' },
  { prop: 'drugName', label: '药品名称', placeholder: '请输入药品名称' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'drugCode', fields: ['drugCode', 'drugNo', 'drugNumber'], label: '药品编码' },
  { prop: 'drugName', fields: ['drugName', 'name'], label: '药品名称', minWidth: 160 },
  { prop: 'specification', fields: ['specification', 'spec'], label: '规格' },
  { prop: 'unit', label: '单位' },
  { prop: 'stockQuantity', fields: ['stockQuantity', 'stock', 'inventory'], label: '库存' },
  { prop: 'stockLowerLimit', fields: ['stockLowerLimit', 'lowStockLimit'], label: '库存下限' },
  { prop: 'price', fields: ['price', 'unitPrice'], label: '单价', format: 'money' },
  { prop: 'status', fields: ['status', 'drugStatus'], label: '状态', tag: true }
]

const formFields = [
  { prop: 'drugCode', label: '药品编码', placeholder: '请输入药品编码', required: true },
  { prop: 'drugName', label: '药品名称', placeholder: '请输入药品名称', required: true },
  { prop: 'specification', label: '规格', placeholder: '请输入规格' },
  { prop: 'unit', label: '单位', placeholder: '请输入单位' },
  { prop: 'price', label: '单价', type: 'number', default: 0, required: true, min: 0 },
  { prop: 'stockQuantity', label: '库存', type: 'number', default: 0, required: true, min: 0 },
  { prop: 'stockLowerLimit', label: '库存下限', type: 'number', default: 0, min: 0 },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'ENABLED' }
]
</script>
