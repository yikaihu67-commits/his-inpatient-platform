<template>
  <ManagementPage
    title="出院结算"
    subtitle="流程：仅在院患者可创建出院结算；已结算记录不可重复结算，结算后自动释放床位。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
    :on-form-change="handleDischargeFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/discharge'
import { dischargeStatusOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, findBedByAdmission, findPatient, inHospitalAdmissionOptions, patientOptions } = useDemoOptions()

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'status', label: '状态', type: 'select', options: dischargeStatusOptions }
]

const columns = [
  { prop: 'dischargeNo', label: '出院结算编号', minWidth: 160 },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'admissionId', label: '入院ID' },
  { prop: 'totalFeeAmount', label: '总费用', format: 'money' },
  { prop: 'depositBalance', label: '预交金余额', format: 'money', minWidth: 120 },
  { prop: 'unpaidAmount', label: '欠费金额', format: 'money' },
  { prop: 'actualPayment', label: '实付金额', format: 'money' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'dischargeNo', label: '结算编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true, validator: validateInHospitalAdmission },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'patientStatusText', label: '患者状态', disabled: true, virtual: true },
  { prop: 'admissionContext', label: '住院状态', disabled: true, virtual: true },
  { prop: 'bedContext', label: '当前床位', disabled: true, virtual: true },
  { prop: 'operatorName', label: '操作员' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '执行结算', key: 'settle', show: (row) => row.status === 'DRAFT', fields: [{ prop: 'operatorName', label: '结算操作员' }] },
  { label: '取消草稿', key: 'cancel', type: 'danger', show: (row) => row.status === 'DRAFT' }
]

const canEdit = (row) => row.status === 'DRAFT'
const canDelete = (row) => row.status === 'DRAFT'

function validateInHospitalAdmission(value) {
  const admission = findAdmission(value)
  if (!admission) return '请选择住院记录'
  if (admission.status !== 'IN_HOSPITAL') return '只有在院患者才能创建出院结算'
  return ''
}

function handleDischargeFormChange(field, form) {
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
