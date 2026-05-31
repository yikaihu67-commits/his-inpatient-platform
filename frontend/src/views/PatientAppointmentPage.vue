<template>
  <ManagementPage
    title="患者预约管理"
    subtitle="查看移动端患者预约，支持确认、完成和取消预约。预约不直接改变检查检验或出院主流程。"
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
import api from '../api/patientAppointment'
import { useDemoOptions } from '../useDemoOptions'

const { patientOptions, inHospitalAdmissionOptions } = useDemoOptions()

const appointmentTypeOptions = [
  { label: '检查预约', value: 'EXAM_APPOINTMENT' },
  { label: '检验预约', value: 'LAB_APPOINTMENT' },
  { label: '出院结算预约', value: 'DISCHARGE_SETTLEMENT' },
  { label: '复诊预约', value: 'FOLLOW_UP' },
  { label: '其他', value: 'OTHER' }
]

const statusOptions = [
  { label: '已预约', value: 'REQUESTED' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const searchFields = [
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'appointmentType', label: '预约类型', type: 'select', options: appointmentTypeOptions },
  { prop: 'status', label: '预约状态', type: 'select', options: statusOptions }
]

const columns = [
  { prop: 'patientName', label: '患者姓名', minWidth: 110 },
  { prop: 'admissionNo', label: '住院号', minWidth: 130 },
  { prop: 'appointmentType', label: '预约类型', minWidth: 150 },
  { prop: 'appointmentItem', label: '预约项目', minWidth: 180 },
  { prop: 'appointmentDate', label: '预约日期', minWidth: 130 },
  { prop: 'timeSlot', label: '时间段', minWidth: 130 },
  { prop: 'contactPhone', label: '联系电话', minWidth: 130 },
  { prop: 'status', label: '状态', tag: true },
  { prop: 'createdAt', label: '创建时间', minWidth: 160, format: 'time' }
]

const formFields = [
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: inHospitalAdmissionOptions },
  { prop: 'appointmentType', label: '预约类型', type: 'select', options: appointmentTypeOptions, required: true, default: 'EXAM_APPOINTMENT' },
  { prop: 'appointmentItem', label: '预约项目', required: true },
  { prop: 'appointmentDate', label: '预约日期', type: 'date', valueFormat: 'YYYY-MM-DD', required: true },
  { prop: 'timeSlot', label: '时间段', type: 'select', options: ['上午', '下午', '晚上'].map((value) => ({ label: value, value })), required: true, allowCreate: true },
  { prop: 'contactPhone', label: '联系电话', pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '确认', key: 'confirm', show: (row) => row.status === 'REQUESTED' },
  { label: '完成', key: 'complete', type: 'success', show: (row) => ['REQUESTED', 'CONFIRMED'].includes(row.status) },
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => ['REQUESTED', 'CONFIRMED'].includes(row.status), fields: [{ prop: 'remark', label: '取消原因', type: 'textarea' }] }
]

const canEdit = (row) => ['REQUESTED', 'CONFIRMED'].includes(row.status)
const canDelete = (row) => row.status === 'REQUESTED'
</script>
