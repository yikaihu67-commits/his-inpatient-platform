<template>
  <ManagementPage
    title="入院登记"
    subtitle="流程：选择患者 -> 填写科室病区和医生 -> 保存登记 -> 办理入院后进入床位分配。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
    :on-form-change="handleAdmissionFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/admission'
import {
  admissionStatusOptions,
  chargeTypeOptions,
  nursingLevelOptions
} from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { departmentNameOptions, doctorNameOptions, findPatient, patientOptions, wardNameOptions } = useDemoOptions()

const searchFields = [
  { prop: 'admissionNo', label: '住院号' },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'patientName', label: '患者姓名' },
  { prop: 'departmentName', label: '科室', type: 'select', options: departmentNameOptions, allowCreate: true },
  { prop: 'wardName', label: '病区', type: 'select', options: wardNameOptions, allowCreate: true },
  { prop: 'status', label: '状态', type: 'select', options: admissionStatusOptions }
]

const columns = [
  { prop: 'admissionNo', label: '住院号', minWidth: 150 },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'patientName', label: '患者' },
  { prop: 'departmentName', label: '科室' },
  { prop: 'wardName', label: '病区' },
  { prop: 'doctorName', label: '医生' },
  { prop: 'admissionTime', label: '入院时间', format: 'time', minWidth: 160 },
  { prop: 'status', label: '状态', tag: true, minWidth: 130 }
]

const formFields = [
  { prop: 'admissionNo', label: '住院号', placeholder: '不填则后端自动生成' },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'patientStatusText', label: '患者状态', disabled: true, virtual: true },
  { prop: 'departmentName', label: '入院科室', type: 'select', options: departmentNameOptions, required: true, allowCreate: true },
  { prop: 'wardName', label: '入院病区', type: 'select', options: wardNameOptions, allowCreate: true },
  { prop: 'admissionTime', label: '入院时间', type: 'datetime', inputType: 'datetime', required: true },
  { prop: 'admissionDiagnosis', label: '诊断', span: 24 },
  { prop: 'chargeType', label: '费别', type: 'select', options: chargeTypeOptions, allowCreate: true },
  { prop: 'nursingLevel', label: '护理级别', type: 'select', options: nursingLevelOptions },
  { prop: 'doctorName', label: '经管医生', type: 'select', options: doctorNameOptions, required: true, allowCreate: true },
  { prop: 'status', label: '状态', type: 'select', options: admissionStatusOptions, default: 'REGISTERED' }
]

const actions = [
  { label: '办理入院', key: 'admit', show: (row) => ['DRAFT', 'REGISTERED'].includes(row.status) },
  { label: '取消入院', key: 'cancel', type: 'danger', show: (row) => ['DRAFT', 'REGISTERED'].includes(row.status) }
]

const canEdit = (row) => ['DRAFT', 'REGISTERED'].includes(row.status)
const canDelete = (row) => ['DRAFT', 'REGISTERED'].includes(row.status)

function handleAdmissionFormChange(field, form) {
  if (field.prop === 'patientId' || field.prop === null) {
    const patient = findPatient(form.patientId)
    form.patientStatusText = patient ? `${patient.name} / ${patient.status}` : ''
  }
}
</script>
