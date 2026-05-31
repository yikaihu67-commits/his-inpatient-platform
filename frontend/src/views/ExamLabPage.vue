<template>
  <ManagementPage
    title="检查检验"
    subtitle="医生申请检查检验，支持安排、完成、报告录入、异常标记和生成费用。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/examLab'

const typeOptions = [{ label: '检查', value: 'EXAM' }, { label: '检验', value: 'LAB' }]
const categoryOptions = [
  { label: '影像', value: 'IMAGING' },
  { label: '超声', value: 'ULTRASOUND' },
  { label: '心电', value: 'ECG' },
  { label: '血液', value: 'BLOOD' },
  { label: '尿液', value: 'URINE' },
  { label: '生化', value: 'BIOCHEMISTRY' },
  { label: '其他', value: 'OTHER' }
]
const statusOptions = ['REQUESTED', 'SCHEDULED', 'COMPLETED', 'REPORTED', 'BILLED', 'CANCELLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'requestType', label: '类型', type: 'select', options: typeOptions },
  { prop: 'itemCategory', label: '类别', type: 'select', options: categoryOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions },
  { prop: 'itemName', label: '项目名称' }
]

const columns = [
  { prop: 'requestNo', label: '申请单号', minWidth: 150 },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'requestType', label: '类型' },
  { prop: 'itemName', label: '项目名称', minWidth: 160 },
  { prop: 'doctorName', label: '申请医生' },
  { prop: 'abnormalFlag', label: '异常' },
  { prop: 'totalAmount', label: '金额', format: 'money' },
  { prop: 'billed', label: '已计费' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'requestNo', label: '申请单号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院ID', type: 'number', required: true },
  { prop: 'patientId', label: '患者ID', type: 'number', required: true },
  { prop: 'requestType', label: '申请类型', type: 'select', options: typeOptions, default: 'EXAM' },
  { prop: 'itemCode', label: '项目编码' },
  { prop: 'itemName', label: '项目名称', required: true },
  { prop: 'requestContent', label: '申请内容', span: 24, type: 'textarea' },
  { prop: 'itemCategory', label: '项目类别', type: 'select', options: categoryOptions, default: 'OTHER' },
  { prop: 'doctorName', label: '申请医生', required: true },
  { prop: 'executionDepartment', label: '执行科室' },
  { prop: 'unitPrice', label: '单价', type: 'number', min: 0, default: 0 },
  { prop: 'quantity', label: '数量', type: 'number', min: 0, default: 1 },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '安排', key: 'schedule', show: (row) => ['REQUESTED', 'SUBMITTED'].includes(row.status), fields: [{ prop: 'scheduledTime', label: '安排时间', type: 'datetime', inputType: 'datetime' }, { prop: 'executionDepartment', label: '执行科室' }] },
  { label: '完成', key: 'complete', show: (row) => ['REQUESTED', 'SUBMITTED', 'SCHEDULED'].includes(row.status), fields: [{ prop: 'executedTime', label: '执行时间', type: 'datetime', inputType: 'datetime' }, { prop: 'executorName', label: '执行人' }] },
  { label: '录入报告', key: 'report', show: (row) => row.status === 'COMPLETED', fields: [{ prop: 'reportDoctorName', label: '报告医生' }, { prop: 'resultSummary', label: '结果摘要' }, { prop: 'resultDetail', label: '结果详情', type: 'textarea' }, { prop: 'abnormalFlag', label: '异常标记', type: 'select', options: [{ label: '是', value: true }, { label: '否', value: false }] }] },
  { label: '生成费用', key: 'bill', show: (row) => ['COMPLETED', 'REPORTED'].includes(row.status) && !row.billed, fields: [{ prop: 'unitPrice', label: '计费单价', type: 'number', default: 0 }, { prop: 'quantity', label: '数量', type: 'number', default: 1 }] },
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => ['REQUESTED', 'SUBMITTED', 'SCHEDULED'].includes(row.status) }
]

const canEdit = (row) => ['DRAFT', 'REQUESTED'].includes(row.status)
const canDelete = (row) => ['DRAFT', 'REQUESTED'].includes(row.status)
</script>
