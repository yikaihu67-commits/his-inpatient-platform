<template>
  <ManagementPage
    title="医嘱管理"
    subtitle="流程：仅在院患者可开医嘱；选择住院记录后自动带出患者、科室、病区和床位信息。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
    :on-form-change="handleOrderFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/order'
import { orderCategoryOptions, orderTypeOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, findBedByAdmission, findPatient, inHospitalAdmissionOptions, doctorNameOptions, nurseNameOptions, patientOptions } = useDemoOptions()

const statusOptions = ['DRAFT', 'SUBMITTED', 'CHECKED', 'EXECUTED', 'BILLED', 'STOPPED', 'CANCELLED'].map((value) => ({ label: value, value }))
const frequencyOptions = ['每日一次', '每日两次', '每日三次', '每晚一次', '必要时', '立即执行'].map((value) => ({ label: value, value }))
const routeOptions = ['口服', '静脉滴注', '静脉注射', '肌肉注射', '皮下注射', '外用', '检查执行'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'orderType', label: '类型', type: 'select', options: orderTypeOptions },
  { prop: 'orderCategory', label: '分类', type: 'select', options: orderCategoryOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions },
  { prop: 'itemName', label: '项目名称' }
]

const columns = [
  { prop: 'orderNo', label: '医嘱编号', minWidth: 150 },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'orderType', label: '类型' },
  { prop: 'orderCategory', label: '分类' },
  { prop: 'itemName', label: '项目名称', minWidth: 160 },
  { prop: 'doctorName', label: '医生' },
  { prop: 'unitPrice', label: '单价', format: 'money' },
  { prop: 'quantity', label: '数量' },
  { prop: 'totalAmount', label: '金额', format: 'money' },
  { prop: 'billed', label: '已计费' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'orderNo', label: '医嘱编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true, validator: validateInHospitalAdmission },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'patientStatusText', label: '患者状态', disabled: true, virtual: true },
  { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
  { prop: 'bedContext', label: '当前床位', disabled: true, virtual: true },
  { prop: 'orderType', label: '医嘱类型', type: 'select', options: orderTypeOptions, required: true, default: 'LONG_TERM' },
  { prop: 'orderCategory', label: '医嘱分类', type: 'select', options: orderCategoryOptions, default: 'DRUG' },
  { prop: 'itemName', label: '项目名称', required: true },
  { prop: 'orderContent', label: '医嘱内容', span: 24, type: 'textarea' },
  { prop: 'dosage', label: '剂量' },
  { prop: 'dosageUnit', label: '剂量单位' },
  { prop: 'frequency', label: '频次', type: 'select', options: frequencyOptions, allowCreate: true },
  { prop: 'route', label: '用法', type: 'select', options: routeOptions, allowCreate: true },
  { prop: 'startTime', label: '开始时间', type: 'datetime', inputType: 'datetime' },
  { prop: 'doctorName', label: '开立医生', type: 'select', options: doctorNameOptions, required: true, allowCreate: true },
  { prop: 'executionDepartment', label: '执行科室' },
  { prop: 'executorName', label: '执行人' },
  { prop: 'unitPrice', label: '单价', type: 'number', min: 0, default: 0 },
  { prop: 'quantity', label: '数量', type: 'number', min: 0, default: 1 },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions, default: 'DRAFT' }
]

const actions = [
  { label: '提交', key: 'submit', show: (row) => row.status === 'DRAFT' },
  { label: '核对', key: 'check', show: (row) => row.status === 'SUBMITTED', fields: [{ prop: 'nurseName', label: '核对护士', type: 'select', options: nurseNameOptions, allowCreate: true }] },
  { label: '执行', key: 'execute', show: (row) => ['SUBMITTED', 'CHECKED'].includes(row.status), fields: [{ prop: 'nurseName', label: '执行护士', type: 'select', options: nurseNameOptions, allowCreate: true }] },
  { label: '生成费用', key: 'bill', show: (row) => row.status === 'EXECUTED' && !row.billed, fields: [{ prop: 'remark', label: '备注', type: 'textarea' }] },
  { label: '停止', key: 'stop', type: 'warning', show: (row) => ['CHECKED', 'EXECUTED'].includes(row.status) },
  { label: '作废', key: 'cancel', type: 'danger', show: (row) => row.status === 'SUBMITTED' }
]

const canEdit = (row) => row.status === 'DRAFT'
const canDelete = (row) => row.status === 'DRAFT'

function validateInHospitalAdmission(value) {
  const admission = findAdmission(value)
  if (!admission) return '请选择住院记录'
  if (admission.status !== 'IN_HOSPITAL') return '只有在院患者才能开医嘱'
  return ''
}

function handleOrderFormChange(field, form) {
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
