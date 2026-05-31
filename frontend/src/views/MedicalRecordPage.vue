<template>
  <ManagementPage
    title="病历记录"
    subtitle="流程：新增草稿病历 -> 提交 -> 审核 -> 归档；已归档病历禁止修改，非草稿病历禁止直接删除。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :can-delete="canDelete"
    :on-form-change="handleFormChange"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/medicalRecord'
import { medicalRecordStatusOptions, medicalRecordTypeOptions } from '../options'
import { useDemoOptions } from '../useDemoOptions'

const { findAdmission, inHospitalAdmissionOptions, patientOptions } = useDemoOptions()

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'recordType', label: '类型', type: 'select', options: medicalRecordTypeOptions },
  { prop: 'status', label: '状态', type: 'select', options: medicalRecordStatusOptions },
  { prop: 'doctorName', label: '医生' },
  { prop: 'title', label: '标题' }
]

const columns = [
  { prop: 'recordNo', label: '病历编号', minWidth: 150 },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'recordType', label: '记录类型', minWidth: 150 },
  { prop: 'title', label: '标题', minWidth: 160 },
  { prop: 'doctorName', label: '医生' },
  { prop: 'recordTime', label: '记录时间', minWidth: 160, format: 'time' },
  { prop: 'status', label: '状态', tag: true }
]

const formFields = [
  { prop: 'recordNo', label: '病历编号', placeholder: '不填则后端自动生成' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions, required: true },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'recordType', label: '记录类型', type: 'select', options: medicalRecordTypeOptions, default: 'DAILY_COURSE_RECORD' },
  { prop: 'title', label: '标题', required: true },
  { prop: 'doctorName', label: '记录医生', required: true },
  { prop: 'recordTime', label: '记录时间', type: 'datetime', inputType: 'datetime' },
  { prop: 'content', label: '内容', span: 24, type: 'textarea', rows: 8, required: true },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '提交', key: 'submit', show: (row) => row.status === 'DRAFT' },
  { label: '审核', key: 'review', show: (row) => row.status === 'SUBMITTED' },
  { label: '归档', key: 'archive', show: (row) => ['SUBMITTED', 'REVIEWED'].includes(row.status) },
  { label: '作废', key: 'cancel', type: 'danger', show: (row) => row.status === 'SUBMITTED' }
]

const canEdit = (row) => row.status === 'DRAFT'
const canDelete = (row) => row.status === 'DRAFT'

function handleFormChange(field, form) {
  if (field.prop === 'admissionId' || field.prop === null) {
    const admission = findAdmission(form.admissionId)
    if (admission) {
      form.patientId = admission.patientId
      if (!form.doctorName) form.doctorName = admission.doctorName || '王医生'
    }
  }
  if ((field.prop === 'recordType' || field.prop === null) && !form.title) {
    form.title = titleOf(form.recordType)
  }
  if ((field.prop === 'recordType' || field.prop === null) && !form.content) {
    form.content = templateOf(form.recordType)
  }
}

function titleOf(type) {
  const map = {
    ADMISSION_RECORD: '入院记录',
    FIRST_COURSE_RECORD: '首次病程记录',
    DAILY_COURSE_RECORD: '日常病程记录',
    SUPERIOR_ROUND_RECORD: '上级医师查房记录',
    DISCHARGE_RECORD: '出院记录',
    NURSING_RECORD: '护理记录'
  }
  return map[type] || '病历记录'
}

function templateOf(type) {
  if (type === 'ADMISSION_RECORD') {
    return '主诉：\n现病史：\n既往史：\n查体：\n初步诊断：\n处理意见：'
  }
  if (type === 'DISCHARGE_RECORD') {
    return '入院情况：\n住院经过：\n出院情况：\n出院诊断：\n出院医嘱：'
  }
  return '病情变化：\n查体：\n辅助检查：\n诊疗计划：'
}
</script>
