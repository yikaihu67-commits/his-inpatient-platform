<template>
  <ManagementPage
    title="费用账单"
    subtitle="流程：仅在院患者可新增费用；选择住院记录后自动带出患者、科室、病区和床位信息。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :on-form-change="handleFeeFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/fee'
import { feeCategoryOptions, feeSourceOptions, feeStatusOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, findBedByAdmission, findPatient, inHospitalAdmissionOptions, patientOptions } = useDemoOptions()

const unitOptions = ['次', '天', '盒', '支', '瓶', '项'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'itemName', label: '项目名称' },
  { prop: 'itemCategory', label: '类别', type: 'select', options: feeCategoryOptions },
  { prop: 'status', label: '状态', type: 'select', options: feeStatusOptions }
]

const columns = [
  { prop: 'feeNo', label: '费用编号', minWidth: 150 },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'admissionId', label: '入院ID' },
  { prop: 'itemName', label: '项目名称', minWidth: 160 },
  { prop: 'itemCategory', label: '类别' },
  { prop: 'totalAmount', label: '金额', format: 'money' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'feeNo', label: '费用编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true, validator: validateInHospitalAdmission },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'patientStatusText', label: '患者状态', disabled: true, virtual: true },
  { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
  { prop: 'bedContext', label: '当前床位', disabled: true, virtual: true },
  { prop: 'sourceType', label: '来源类型', type: 'select', options: feeSourceOptions, default: 'MANUAL' },
  { prop: 'sourceId', label: '来源ID', type: 'number' },
  { prop: 'itemCode', label: '项目编码' },
  { prop: 'itemName', label: '项目名称', required: true },
  { prop: 'itemCategory', label: '项目类别', type: 'select', options: feeCategoryOptions, default: 'OTHER' },
  { prop: 'quantity', label: '数量', type: 'number', default: 1, required: true, min: 1 },
  { prop: 'unit', label: '单位', type: 'select', options: unitOptions, allowCreate: true },
  { prop: 'unitPrice', label: '单价', type: 'number', default: 0, required: true, min: 0 },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '取消费用', key: 'cancel', type: 'danger', show: (row) => row.status !== 'CANCELLED' }
]

function validateInHospitalAdmission(value) {
  const admission = findAdmission(value)
  if (!admission) return '请选择住院记录'
  if (admission.status !== 'IN_HOSPITAL') return '只有在院患者才能生成费用'
  return ''
}

function handleFeeFormChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) {
    const admission = findAdmission(form.admissionId)
    const bed = findBedByAdmission(form.admissionId)
    if (admission) {
      form.patientId = admission.patientId
      form.admissionContext = `${admission.admissionNo || '-'} / ${admission.departmentName || '-'} / ${admission.wardName || '-'} / ${admission.status}`
      form.bedContext = bed ? `${bed.wardName || '-'} ${bed.roomNo || '-'}房 ${bed.bedNo || '-'}床` : '暂未分配床位'
    }
  }
  if (field.prop === 'patientId' || field.prop === 'admissionId' || field.prop === null) {
    const patient = findPatient(form.patientId)
    form.patientStatusText = patient ? `${patient.name} / ${patient.status}` : ''
  }
}
</script>
