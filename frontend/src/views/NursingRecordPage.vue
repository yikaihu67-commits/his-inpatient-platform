<template>
  <ManagementPage
    title="护理管理"
    subtitle="记录生命体征、日常护理、医嘱执行护理，并可生成护理费用进入收费模块。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :row-actions="actions"
    :can-edit="canEdit"
    :hide-delete="true"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/nursingRecord'
import { useDemoOptions } from '../useDemoOptions'

const { activeAdmissionOptions, patientOptions, nurseNameOptions } = useDemoOptions()

const nursingTypeOptions = [
  { label: '生命体征', value: 'VITAL_SIGN' },
  { label: '日常护理', value: 'DAILY_CARE' },
  { label: '医嘱执行', value: 'ORDER_EXECUTION' },
  { label: '创面护理', value: 'WOUND_CARE' },
  { label: '输液护理', value: 'INFUSION' },
  { label: '其他', value: 'OTHER' }
]

const statusOptions = ['RECORDED', 'EXECUTED', 'REVIEWED', 'BILLED', 'CANCELLED'].map((value) => ({ label: value, value }))

const searchFields = [
  { prop: 'admissionId', label: '住院记录', type: 'select', options: activeAdmissionOptions },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions },
  { prop: 'nursingType', label: '护理类型', type: 'select', options: nursingTypeOptions },
  { prop: 'status', label: '状态', type: 'select', options: statusOptions },
  { prop: 'nurseName', label: '护士' }
]

const columns = [
  { prop: 'recordNo', label: '护理编号', minWidth: 150 },
  { prop: 'admissionId', label: '住院ID' },
  { prop: 'patientId', label: '患者ID' },
  { prop: 'nursingType', label: '护理类型' },
  { prop: 'nursingContent', label: '护理内容', minWidth: 180 },
  { prop: 'temperature', label: '体温' },
  { prop: 'bloodPressure', label: '血压' },
  { prop: 'nurseName', label: '护士' },
  { prop: 'nursingFee', label: '护理费用', format: 'money' },
  { prop: 'status', label: '状态', tag: true },
  { prop: 'recordTime', label: '记录时间', format: 'time', minWidth: 160 }
]

const formFields = [
  { prop: 'recordNo', label: '护理编号', placeholder: '不填则自动生成' },
  { prop: 'orderId', label: '关联医嘱ID', type: 'number' },
  { prop: 'admissionId', label: '住院记录', type: 'select', options: activeAdmissionOptions, required: true },
  { prop: 'patientId', label: '患者', type: 'select', options: patientOptions, required: true },
  { prop: 'nursingType', label: '护理类型', type: 'select', options: nursingTypeOptions, required: true, default: 'VITAL_SIGN' },
  { prop: 'nursingContent', label: '护理内容', span: 24, type: 'textarea', required: true },
  { prop: 'temperature', label: '体温', type: 'number' },
  { prop: 'pulse', label: '脉搏', type: 'number' },
  { prop: 'respiration', label: '呼吸', type: 'number' },
  { prop: 'bloodPressure', label: '血压' },
  { prop: 'intakeAmount', label: '入量', type: 'number', min: 0 },
  { prop: 'outputAmount', label: '出量', type: 'number', min: 0 },
  { prop: 'nursingLevel', label: '护理等级' },
  { prop: 'nurseName', label: '记录护士', type: 'select', options: nurseNameOptions, allowCreate: true },
  { prop: 'recordTime', label: '记录时间', type: 'datetime', inputType: 'datetime' },
  { prop: 'billable', label: '是否产生费用', type: 'select', options: [{ label: '是', value: true }, { label: '否', value: false }], default: false },
  { prop: 'nursingFee', label: '护理费用', type: 'number', min: 0, default: 0 },
  { prop: 'remark', label: '备注', span: 24, type: 'textarea' }
]

const actions = [
  { label: '执行护理', key: 'execute', show: (row) => ['RECORDED', 'REVIEWED'].includes(row.status), fields: [{ prop: 'nurseName', label: '护士', type: 'select', options: nurseNameOptions, allowCreate: true }] },
  { label: '查看确认', key: 'review', show: (row) => !['CANCELLED', 'BILLED'].includes(row.status), fields: [{ prop: 'nurseName', label: '护士', type: 'select', options: nurseNameOptions, allowCreate: true }] },
  { label: '生成费用', key: 'bill', show: (row) => row.billable && !row.billed && row.status !== 'CANCELLED', fields: [{ prop: 'remark', label: '备注', type: 'textarea' }] },
  { label: '取消', key: 'cancel', type: 'danger', show: (row) => !['BILLED', 'CANCELLED'].includes(row.status) }
]

const canEdit = (row) => !['BILLED', 'CANCELLED'].includes(row.status)
</script>
