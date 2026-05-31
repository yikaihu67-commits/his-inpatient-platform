<template>
  <ManagementPage title="操作日志" :api="api" :search-fields="searchFields" :table-columns="columns" :form-fields="[]" :hide-create="true" :hide-edit="true" />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/operationLog'

const typeOptions = ['CREATE', 'UPDATE', 'DELETE', 'QUERY', 'SUBMIT', 'CHECK', 'EXECUTE', 'CANCEL', 'SETTLE', 'ENABLE', 'DISABLE', 'OTHER'].map((value) => ({ label: value, value }))
const statusOptions = ['SUCCESS', 'FAILED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'moduleName', label: '模块名称' },
  { prop: 'operationType', label: '操作类型', type: 'select', options: typeOptions },
  { prop: 'operatorName', label: '操作人' },
  { prop: 'businessId', label: '业务ID' },
  { prop: 'resultStatus', label: '结果', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'moduleName', label: '模块名称' },
  { prop: 'operationType', label: '操作类型' },
  { prop: 'operatorName', label: '操作人' },
  { prop: 'requestUri', label: '请求路径', minWidth: 180 },
  { prop: 'resultStatus', label: '结果状态', tag: true },
  { prop: 'operationTime', label: '操作时间', format: 'time', minWidth: 160 }
]
</script>
