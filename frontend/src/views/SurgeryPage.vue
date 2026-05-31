<template>
  <ManagementPage
    title="手术管理"
    subtitle="流程：申请手术 -> 安排手术 -> 开始手术 -> 完成手术 -> 生成手术费用；手术费会进入收费模块费用明细。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
    :on-form-change="handleFormChange"
    :on-action-form-change="handleActionFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/surgery'
import {
  anesthesiaMethodOptions,
  doctorOptions,
  surgeryLevelOptions,
  surgeryStatusOptions,
  surgeryTypeOptions
} from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, findBedByAdmission, findPatient, inHospitalAdmissionOptions, patientOptions, doctorNameOptions } = useDemoOptions()

const operatingRoomOptions = ['OR-1', 'OR-2', 'OR-3', '日间手术室'].map((value) => ({ label: value, value }))
const doctorSelectOptions = doctorNameOptions || doctorOptions

const searchFields = [
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'surgeryNo', label: '手术编号' },
  { prop: 'surgeryName', label: '手术名称' },
  { prop: 'status', label: '状态', type: 'select', options: surgeryStatusOptions }
]

const columns = [
  { prop: 'surgeryNo', label: '手术编号', minWidth: 150 },
  { prop: 'patientName', label: '患者', minWidth: 100 },
  { prop: 'admissionNo', label: '住院号', minWidth: 130 },
  { prop: 'surgeryName', label: '手术名称', minWidth: 180 },
  { prop: 'operatingRoom', label: '手术室' },
  { prop: 'plannedTime', label: '计划时间', minWidth: 160, format: 'time' },
  { prop: 'primaryDoctorName', label: '主刀医生', minWidth: 100 },
  { prop: 'surgeryFee', label: '手术费用', format: 'money' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'surgeryNo', label: '手术编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true, validator: validateInHospitalAdmission },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'admissionContext', label: '住院信息', disabled: true, virtual: true, span: 24 },
  { prop: 'bedContext', label: '当前床位', disabled: true, virtual: true, span: 24 },
  { prop: 'surgeryName', label: '手术名称', required: true },
  { prop: 'preoperativeDiagnosis', label: '术前诊断' },
  { prop: 'surgeryLevel', label: '手术级别', type: 'select', options: surgeryLevelOptions, default: 'II' },
  { prop: 'surgeryType', label: '手术类型', type: 'select', options: surgeryTypeOptions, default: '择期' },
  { prop: 'operatingRoom', label: '手术室', type: 'select', options: operatingRoomOptions, allowCreate: true },
  { prop: 'plannedTime', label: '计划手术时间', type: 'datetime', inputType: 'datetime' },
  { prop: 'primaryDoctorName', label: '主刀医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
  { prop: 'assistantDoctorName', label: '助手医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
  { prop: 'anesthesiaMethod', label: '麻醉方式', type: 'select', options: anesthesiaMethodOptions, allowCreate: true },
  { prop: 'anesthesiologistName', label: '麻醉医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
  { prop: 'surgeryFee', label: '手术费用', type: 'number', default: 0, min: 0 },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  {
    label: '安排',
    key: 'schedule',
    show: (row) => row.status === 'APPLIED',
    fields: [
      { prop: 'plannedTime', label: '计划时间', type: 'datetime', inputType: 'datetime' },
      { prop: 'operatingRoom', label: '手术室', type: 'select', options: operatingRoomOptions, allowCreate: true },
      { prop: 'primaryDoctorName', label: '主刀医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
      { prop: 'assistantDoctorName', label: '助手医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
      { prop: 'anesthesiaMethod', label: '麻醉方式', type: 'select', options: anesthesiaMethodOptions, allowCreate: true },
      { prop: 'anesthesiologistName', label: '麻醉医生', type: 'select', options: doctorSelectOptions, allowCreate: true },
      { prop: 'surgeryFee', label: '手术费用', type: 'number', min: 0 },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  { label: '开始', key: 'start', show: (row) => row.status === 'SCHEDULED', fields: [{ prop: 'actualStartTime', label: '开始时间', type: 'datetime', inputType: 'datetime' }] },
  {
    label: '完成',
    key: 'complete',
    show: (row) => row.status === 'IN_PROGRESS',
    fields: [
      { prop: 'actualEndTime', label: '结束时间', type: 'datetime', inputType: 'datetime' },
      { prop: 'surgeryFee', label: '手术费用', type: 'number', min: 0 },
      { prop: 'remark', label: '手术记录', type: 'textarea' }
    ]
  },
  { label: '生成费用', key: 'bill', type: 'success', show: (row) => row.status === 'COMPLETED', fields: [{ prop: 'surgeryFee', label: '手术费用', type: 'number', min: 0 }] },
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => ['APPLIED', 'SCHEDULED'].includes(row.status), fields: [{ prop: 'remark', label: '取消原因', type: 'textarea' }] }
]

const canEdit = (row) => ['APPLIED', 'SCHEDULED'].includes(row.status)
const canDelete = (row) => row.status === 'APPLIED'

function validateInHospitalAdmission(value) {
  const admission = findAdmission(value)
  if (!admission) return '请选择住院记录'
  if (admission.status !== 'IN_HOSPITAL') return '只有在院患者可以申请手术'
  return ''
}

function handleFormChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) {
    const admission = findAdmission(form.admissionId)
    const bed = findBedByAdmission(form.admissionId)
    if (admission) {
      form.patientId = admission.patientId
      form.admissionContext = `${admission.admissionNo || '-'} / ${admission.departmentName || '-'} / ${admission.wardName || '-'} / ${admission.status}`
      form.bedContext = bed ? `${bed.wardName || '-'} ${bed.roomNo || '-'}房 ${bed.bedNo || '-'}床` : '暂未分配床位'
      if (!form.primaryDoctorName) form.primaryDoctorName = admission.doctorName
    }
  }
  if (field.prop === 'patientId' || field.prop === null) {
    const patient = findPatient(form.patientId)
    if (!form.preoperativeDiagnosis && patient) form.preoperativeDiagnosis = '待完善术前诊断'
  }
}

function handleActionFormChange(field, form, action) {
  if (!action || field.prop !== null) return
  if (action.key === 'bill' && form.surgeryFee === '') {
    form.surgeryFee = 0
  }
}
</script>
