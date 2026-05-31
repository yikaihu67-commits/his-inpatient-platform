<template>
  <ManagementPage
    title="床位管理"
    subtitle="流程：新增可用床位 -> 选择在院/已登记住院记录 -> 分配床位；已出院记录不会出现在可分配列表。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :on-action-form-change="handleAssignFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/bed'
import { bedStatusOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { activeAdmissionOptions, findAdmission, findBedByAdmission, wardNameOptions } = useDemoOptions()

const bedTypeOptions = [
  { label: '普通床', value: '普通床' },
  { label: '抢救床', value: '抢救床' },
  { label: '监护床', value: '监护床' },
  { label: '隔离床', value: '隔离床' }
]

const searchFields = [
  { prop: 'wardName', label: '病区', type: 'select', options: wardNameOptions, allowCreate: true },
  { prop: 'roomNo', label: '房间号', placeholder: '请输入房间号' },
  { prop: 'bedNo', label: '床号', placeholder: '请输入床号' },
  { prop: 'status', label: '状态', type: 'select', options: bedStatusOptions }
]

const columns = [
  { prop: 'id', label: '床位编号' },
  { prop: 'bedNo', fields: ['bedNo', 'bedCode', 'bedNumber'], label: '床位号' },
  { prop: 'wardName', fields: ['wardName', 'ward'], label: '病区' },
  { prop: 'roomNo', fields: ['roomNo', 'roomNumber'], label: '房间号' },
  { prop: 'bedType', fields: ['bedType', 'type'], label: '床位类型' },
  { prop: 'status', fields: ['status', 'bedStatus'], label: '状态', tag: true },
  { prop: 'currentAdmissionId', fields: ['currentAdmissionId', 'admissionId'], label: '当前住院ID', minWidth: 120 }
]

const formFields = [
  { prop: 'bedNo', label: '床号', placeholder: '请输入床号', required: true },
  { prop: 'wardName', label: '病区', type: 'select', options: wardNameOptions, allowCreate: true, required: true },
  { prop: 'roomNo', label: '房间号', placeholder: '请输入房间号', required: true },
  { prop: 'bedType', label: '床位类型', type: 'select', options: bedTypeOptions, allowCreate: true, default: '普通床' },
  { prop: 'status', label: '状态', type: 'select', options: bedStatusOptions, default: 'EMPTY' }
]

const actions = [
  {
    label: '分配床位',
    key: 'assign',
    fields: [
      { prop: 'admissionId', label: '住院记录', type: 'select', options: activeAdmissionOptions },
      { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
      { prop: 'assignedBedContext', label: '当前床位', disabled: true, virtual: true }
    ],
    show: (row) => ['EMPTY', 'AVAILABLE'].includes(row.status)
  },
  { label: '释放床位', key: 'release', type: 'warning', show: (row) => row.status === 'OCCUPIED' }
]

function handleAssignFormChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) {
    const admission = findAdmission(form.admissionId)
    const bed = findBedByAdmission(form.admissionId)
    form.admissionContext = admission ? `${admission.admissionNo} / ${admission.patientName || '-'} / ${admission.status}` : ''
    form.assignedBedContext = bed ? `${bed.wardName || '-'} ${bed.roomNo || '-'}房 ${bed.bedNo || '-'}床` : '未分配'
  }
}
</script>
