export function money(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

export function text(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

export function time(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

export function statusText(value) {
  const map = {
    REQUESTED: '已预约',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    IN_HOSPITAL: '在院',
    DISCHARGED: '已出院',
    UNSETTLED: '未结算',
    SETTLED: '已结算',
    REPORTED: '已出报告',
    BILLED: '已计费',
    SCHEDULED: '已安排',
    APPLIED: '已申请',
    NOT_APPLIED: '未申请'
  }
  return map[value] || text(value)
}

export function tagType(value) {
  if (['CONFIRMED', 'COMPLETED', 'SETTLED', 'REPORTED', 'BILLED', 'IN_HOSPITAL'].includes(value)) return 'success'
  if (['CANCELLED'].includes(value)) return 'danger'
  if (['REQUESTED', 'UNSETTLED', 'APPLIED'].includes(value)) return 'warning'
  return 'info'
}
