export function money(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

export function time(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

export function text(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

export function statusText(value) {
  const map = {
    REQUESTED: '已预约',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    IN_HOSPITAL: '在院',
    DISCHARGED: '已出院',
    DISCHARGE_REQUESTED: '已申请出院',
    PENDING_SETTLEMENT: '待结算',
    SETTLED: '已结算',
    UNSETTLED: '未结算',
    REPORTED: '已出报告',
    BILLED: '已计费',
    SCHEDULED: '已安排',
    APPLIED: '已申请',
    DRAFT: '草稿',
    NOT_APPLIED: '未申请'
  }
  return map[value] || text(value)
}

export function typeText(value) {
  const map = {
    DRUG: '药品',
    ORDER: '医嘱',
    NURSING: '护理',
    SURGERY: '手术',
    EXAM: '检查',
    LAB: '检验',
    OTHER: '其他',
    EXAM_APPOINTMENT: '检查预约',
    LAB_APPOINTMENT: '检验预约',
    DISCHARGE_SETTLEMENT: '出院结算预约',
    FOLLOW_UP: '复诊预约'
  }
  return map[value] || text(value)
}

export function statusClass(value) {
  if (['COMPLETED', 'CONFIRMED', 'BILLED', 'SETTLED', 'REPORTED', 'IN_HOSPITAL'].includes(value)) return 'success'
  if (['CANCELLED'].includes(value)) return 'danger'
  if (['REQUESTED', 'APPLIED', 'PENDING_SETTLEMENT', 'UNSETTLED'].includes(value)) return 'warning'
  return 'info'
}
