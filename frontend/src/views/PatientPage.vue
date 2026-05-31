<template>
  <ManagementPage
    title="患者管理"
    subtitle="流程：先完成患者建档；详情页可查看当前住院、床位、费用和最近操作日志。"
    :api="api"
    :search-fields="searchFields"
    :table-columns="columns"
    :form-fields="formFields"
    :detail-sections="detailSections"
    :detail-loader="loadPatientDetail"
  />
</template>

<script setup>
import ManagementPage from '../components/ManagementPage.vue'
import api from '../api/patient'
import accountApi from '../api/account'
import admissionApi from '../api/admission'
import bedApi from '../api/bed'
import depositApi from '../api/deposit'
import dischargeApi from '../api/discharge'
import feeApi from '../api/fee'
import orderApi from '../api/order'
import operationLogApi from '../api/operationLog'
import { genderOptions, patientStatusOptions } from '../options'

const searchFields = [
  { prop: 'patientNo', label: '患者编号', placeholder: '请输入患者编号' },
  { prop: 'name', label: '姓名', placeholder: '请输入姓名' },
  { prop: 'gender', label: '性别', type: 'select', options: genderOptions },
  { prop: 'status', label: '患者状态', type: 'select', options: patientStatusOptions },
  { prop: 'idCard', label: '身份证号', placeholder: '请输入身份证号' },
  { prop: 'phone', label: '手机号', placeholder: '请输入手机号' }
]

const columns = [
  { prop: 'patientNo', fields: ['patientNo', 'patientCode', 'patientNumber'], label: '患者编号', minWidth: 140 },
  { prop: 'name', fields: ['name', 'patientName'], label: '姓名' },
  { prop: 'gender', fields: ['gender', 'sex'], label: '性别' },
  { prop: 'phone', fields: ['phone', 'mobile', 'phoneNumber'], label: '手机号', minWidth: 130 },
  { prop: 'idCard', fields: ['idCard', 'idCardNo', 'identityNo'], label: '身份证号', minWidth: 170 },
  { prop: 'status', fields: ['status', 'patientStatus'], label: '状态', tag: true }
]

const formFields = [
  { prop: 'patientNo', label: '患者编号', placeholder: '不填则后端自动生成' },
  { prop: 'name', label: '姓名', placeholder: '请输入姓名', required: true },
  { prop: 'gender', label: '性别', type: 'select', options: genderOptions, required: true },
  { prop: 'idCard', label: '身份证号', placeholder: '请输入身份证号', pattern: /^$|^[0-9A-Za-z]{15,18}$/, message: '身份证号格式不正确' },
  { prop: 'phone', label: '手机号', placeholder: '请输入手机号', pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
  { prop: 'birthDate', label: '出生日期', type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: '请选择出生日期' },
  { prop: 'address', label: '地址', span: 24, placeholder: '请输入地址' },
  { prop: 'status', label: '状态', type: 'select', options: patientStatusOptions, default: 'ACTIVE' }
]

const detailSections = [
  {
    title: '基本信息',
    fields: [
      { prop: 'patientNo', label: '患者编号' },
      { prop: 'name', label: '姓名' },
      { prop: 'gender', label: '性别' },
      { prop: 'idCard', label: '身份证号' },
      { prop: 'phone', label: '手机号' },
      { prop: 'birthDate', label: '出生日期' },
      { prop: 'address', label: '地址' }
    ]
  },
  {
    title: '住院信息',
    fields: [
      { prop: 'currentAdmissionText', label: '当前入院记录' },
      { prop: 'currentAdmissionNo', label: '住院号' },
      { prop: 'currentDepartmentName', label: '入院科室' },
      { prop: 'currentWardName', label: '病区' },
      { prop: 'currentBedText', label: '床位' },
      { prop: 'currentAdmissionStatus', label: '入院状态' }
    ]
  },
  {
    title: '费用信息',
    fields: [
      { prop: 'totalDepositAmount', label: '预交金', format: 'money' },
      { prop: 'totalFeeAmount', label: '总费用', format: 'money' },
      { prop: 'settledAmount', label: '已结算金额', format: 'money' },
      { prop: 'unsettledAmount', label: '未结金额', format: 'money' },
      { prop: 'settlementStatusText', label: '结算状态' }
    ]
  },
  {
    title: '医嘱列表',
    fields: [
      { prop: 'medicalOrder1', label: '医嘱 1' },
      { prop: 'medicalOrder2', label: '医嘱 2' },
      { prop: 'medicalOrder3', label: '医嘱 3' }
    ]
  },
  {
    title: '费用列表',
    fields: [
      { prop: 'feeItem1', label: '费用 1' },
      { prop: 'feeItem2', label: '费用 2' },
      { prop: 'feeItem3', label: '费用 3' }
    ]
  },
  {
    title: '时间信息',
    fields: [
      { prop: 'createdAt', label: '创建时间', format: 'time' },
      { prop: 'updatedAt', label: '更新时间', format: 'time' }
    ]
  },
  {
    title: '操作日志',
    fields: [
      { prop: 'operationLog1', label: '日志 1' },
      { prop: 'operationLog2', label: '日志 2' },
      { prop: 'operationLog3', label: '日志 3' },
      { prop: 'operationLog4', label: '日志 4' },
      { prop: 'operationLog5', label: '日志 5' }
    ]
  }
]

function firstRecord(page) {
  return page?.records?.[0] || page?.list?.[0] || page?.content?.[0] || page?.rows?.[0] || null
}

async function loadPatientDetail(patient) {
  const admissionPage = await admissionApi.list({ page: 1, pageSize: 10, patientId: patient.id })
  const admissions = admissionPage?.records || admissionPage?.list || admissionPage?.content || []
  const currentAdmission = admissions.find((item) => item.status === 'IN_HOSPITAL') || admissions[0]
  if (!currentAdmission) {
    return {
      currentAdmissionStatus: '无在院记录',
      currentAdmissionText: '-',
      currentAdmissionNo: '-',
      currentDepartmentName: '-',
      currentWardName: '-',
      currentBedText: '-',
      totalDepositAmount: 0,
      totalFeeAmount: 0,
      settledAmount: 0,
      unsettledAmount: 0,
      settlementStatusText: '未结算',
      medicalOrder1: '暂无医嘱',
      medicalOrder2: '-',
      medicalOrder3: '-',
      feeItem1: '暂无费用',
      feeItem2: '-',
      feeItem3: '-'
    }
  }
  const [bedPage, feePage, depositPage, accountSummary, orderPage, dischargePage, logPage] = await Promise.all([
    bedApi.list({ page: 1, pageSize: 100, currentAdmissionId: currentAdmission.id }),
    feeApi.list({ page: 1, pageSize: 100, admissionId: currentAdmission.id }),
    depositApi.list({ page: 1, pageSize: 100, admissionId: currentAdmission.id }),
    accountApi.summary(currentAdmission.id).catch(() => null),
    orderApi.list({ page: 1, pageSize: 5, admissionId: currentAdmission.id }).catch(() => null),
    dischargeApi.list({ page: 1, pageSize: 5, admissionId: currentAdmission.id }).catch(() => null),
    operationLogApi.list({ page: 1, pageSize: 5, businessId: patient.id }).catch(() => null)
  ])
  const beds = bedPage?.records || bedPage?.list || bedPage?.content || bedPage?.rows || []
  const fees = feePage?.records || feePage?.list || feePage?.content || feePage?.rows || []
  const deposits = depositPage?.records || depositPage?.list || depositPage?.content || depositPage?.rows || []
  const orders = orderPage?.records || orderPage?.list || orderPage?.content || orderPage?.rows || []
  const discharges = dischargePage?.records || dischargePage?.list || dischargePage?.content || dischargePage?.rows || []
  const logs = logPage?.records || logPage?.list || logPage?.content || logPage?.rows || []
  const bed = beds.find((item) => Number(item.currentAdmissionId) === Number(currentAdmission.id))
  const settledAmount = fees.filter((item) => item.status === 'SETTLED').reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
  const unsettledAmount = fees.filter((item) => item.status === 'UNSETTLED').reduce((sum, item) => sum + Number(item.totalAmount || 0), 0)
  const totalDepositAmount = deposits
    .filter((item) => item.status === 'SUCCESS' && item.transactionType === 'PAY')
    .reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const logDetail = {}
  logs.slice(0, 5).forEach((item, index) => {
    logDetail[`operationLog${index + 1}`] = `${item.operationTime || item.createdAt || '-'} ${item.operationType || '-'} ${item.requestUri || ''} ${item.resultStatus || ''}`
  })
  const orderDetail = { medicalOrder1: '暂无医嘱', medicalOrder2: '-', medicalOrder3: '-' }
  orders.slice(0, 3).forEach((item, index) => {
    orderDetail[`medicalOrder${index + 1}`] = `${item.orderNo || '-'} ${item.itemName || '-'} / ${item.orderType || '-'} / ${item.status || '-'}`
  })
  const feeDetail = { feeItem1: '暂无费用', feeItem2: '-', feeItem3: '-' }
  fees.slice(0, 3).forEach((item, index) => {
    const amount = Number(item.totalAmount || 0).toFixed(2)
    feeDetail[`feeItem${index + 1}`] = `${item.feeNo || '-'} ${item.itemName || '-'} / ${amount} / ${item.status || '-'}`
  })
  const latestDischarge = discharges[0]
  return {
    currentAdmissionStatus: currentAdmission.status,
    currentAdmissionText: `${currentAdmission.admissionNo || '-'} / ${currentAdmission.departmentName || '-'} / ${currentAdmission.wardName || '-'}`,
    currentAdmissionNo: currentAdmission.admissionNo || '-',
    currentDepartmentName: currentAdmission.departmentName || '-',
    currentWardName: currentAdmission.wardName || '-',
    currentBedText: bed ? `${bed.wardName || '-'} ${bed.roomNo || '-'}房 ${bed.bedNo || '-'}床` : '暂未分配床位',
    totalDepositAmount,
    totalFeeAmount: accountSummary?.totalFeeAmount ?? fees.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0),
    settledAmount,
    unsettledAmount,
    settlementStatusText: latestDischarge ? `${latestDischarge.dischargeNo || '-'} / ${latestDischarge.status || '-'}` : '未结算',
    ...orderDetail,
    ...feeDetail,
    ...logDetail
  }
}
</script>
