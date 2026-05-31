export function money(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

export function text(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

export function time(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : '-'
}

export function statusType(value) {
  if (['SETTLED', 'COMPLETED', 'REPORTED', 'BILLED', 'IN_HOSPITAL', 'EXECUTED'].includes(value)) return 'success'
  if (['CANCELLED', 'FAILED', 'STOPPED'].includes(value)) return 'danger'
  if (['REQUESTED', 'APPLIED', 'DRAFT', 'UNSETTLED', 'PENDING_SETTLEMENT'].includes(value)) return 'warning'
  if (['SCHEDULED', 'SUBMITTED', 'IN_PROGRESS', 'CHECKED'].includes(value)) return 'primary'
  return 'info'
}

export function statusText(value) {
  const map = {
    IN_HOSPITAL: '在院',
    DISCHARGED: '已出院',
    DRAFT: '草稿',
    REQUESTED: '已申请',
    SCHEDULED: '已安排',
    COMPLETED: '已完成',
    REPORTED: '已出报告',
    BILLED: '已计费',
    SETTLED: '已结算',
    UNSETTLED: '未结算',
    APPLIED: '已申请',
    IN_PROGRESS: '进行中',
    NOT_APPLIED: '未申请出院',
    CANCELLED: '已取消'
  }
  return map[value] || text(value)
}
