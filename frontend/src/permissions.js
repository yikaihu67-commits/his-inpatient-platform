export const roles = [
  { label: '系统管理员', value: 'ADMIN' },
  { label: '医生', value: 'DOCTOR' },
  { label: '护士', value: 'NURSE' },
  { label: '收费员', value: 'CASHIER' },
  { label: '手术室管理员', value: 'SURGERY_MANAGER' }
]

export const menus = [
  { label: '首页工作台', path: '/app/dashboard', roles: ['ADMIN', 'DOCTOR', 'NURSE', 'CASHIER', 'SURGERY_MANAGER'] },
  { label: '患者管理', path: '/app/patients', roles: ['ADMIN', 'DOCTOR', 'NURSE'] },
  { label: '医生站', path: '/app/doctor-station', roles: ['ADMIN', 'DOCTOR'] },
  { label: '护士站', path: '/app/nurse-station', roles: ['ADMIN', 'NURSE'] },
  { label: '入院登记', path: '/app/admissions', roles: ['ADMIN'] },
  { label: '床位管理', path: '/app/beds', roles: ['ADMIN', 'NURSE'] },
  { label: '医嘱管理', path: '/app/orders', roles: ['ADMIN', 'DOCTOR', 'NURSE', 'CASHIER'] },
  { label: '护士执行', path: '/app/nursing-executions', roles: ['ADMIN', 'NURSE'] },
  { label: '护理管理', path: '/app/nursing-records', roles: ['ADMIN', 'NURSE', 'CASHIER'] },
  { label: '药品管理', path: '/app/drugs', roles: ['ADMIN'] },
  { label: '药房工作台', path: '/app/pharmacy-station', roles: ['ADMIN'] },
  { label: '药房发药', path: '/app/dispenses', roles: ['ADMIN'] },
  { label: '费用账单', path: '/app/fees', roles: ['ADMIN', 'CASHIER'] },
  { label: '收费工作台', path: '/app/charging-station', roles: ['ADMIN', 'CASHIER'] },
  { label: '预交金管理', path: '/app/deposits', roles: ['ADMIN', 'CASHIER'] },
  { label: '出院结算', path: '/app/discharges', roles: ['ADMIN', 'DOCTOR', 'CASHIER'] },
  { label: '检查检验', path: '/app/exam-labs', roles: ['ADMIN', 'DOCTOR', 'CASHIER'] },
  { label: '病历记录', path: '/app/medical-records', roles: ['ADMIN', 'DOCTOR'] },
  { label: '手术管理', path: '/app/surgeries', roles: ['ADMIN', 'DOCTOR', 'SURGERY_MANAGER', 'CASHIER'] },
  { label: '患者预约管理', path: '/app/patient-appointments', roles: ['ADMIN', 'DOCTOR', 'NURSE', 'CASHIER'] },
  { label: '科室管理', path: '/app/departments', roles: ['ADMIN'] },
  { label: '人员管理', path: '/app/staff', roles: ['ADMIN'] },
  { label: '基础字典', path: '/app/dict-items', roles: ['ADMIN'] },
  { label: '操作日志', path: '/app/operation-logs', roles: ['ADMIN'] }
]

const allActions = [
  'create',
  'edit',
  'delete',
  'remove',
  'submit',
  'check',
  'execute',
  'fail',
  'stop',
  'cancel',
  'confirm',
  'schedule',
  'start',
  'complete',
  'report',
  'archive',
  'review',
  'bill',
  'settle',
  'pay',
  'refund',
  'enable',
  'disable'
]

const byRoute = {
  '/app/orders': {
    DOCTOR: ['create', 'edit', 'delete', 'submit', 'stop', 'cancel'],
    NURSE: ['check', 'execute'],
    CASHIER: ['bill']
  },
  '/app/nursing-executions': {
    NURSE: ['create', 'edit', 'delete', 'execute', 'fail', 'cancel']
  },
  '/app/nursing-records': {
    NURSE: ['create', 'edit', 'execute', 'review', 'cancel'],
    CASHIER: ['bill']
  },
  '/app/medical-records': {
    DOCTOR: ['create', 'edit', 'delete', 'submit', 'review', 'archive', 'cancel']
  },
  '/app/surgeries': {
    DOCTOR: ['create', 'edit', 'delete', 'cancel'],
    SURGERY_MANAGER: ['schedule', 'start', 'complete', 'cancel'],
    CASHIER: ['bill']
  },
  '/app/exam-labs': {
    DOCTOR: ['create', 'edit', 'delete', 'schedule', 'complete', 'report', 'cancel'],
    CASHIER: ['bill']
  },
  '/app/fees': {
    CASHIER: ['create', 'edit', 'delete', 'cancel']
  },
  '/app/deposits': {
    CASHIER: ['create', 'pay', 'refund', 'cancel']
  },
  '/app/discharges': {
    DOCTOR: ['create', 'edit', 'cancel'],
    CASHIER: ['settle']
  },
  '/app/patient-appointments': {
    DOCTOR: ['create', 'edit', 'confirm', 'complete', 'cancel'],
    NURSE: ['confirm', 'complete', 'cancel'],
    CASHIER: ['confirm', 'complete', 'cancel']
  }
}

export function canAccessMenu(role, path) {
  if (role === 'ADMIN') return true
  return menus.find((item) => item.path === path)?.roles.includes(role) === true
}

export function canUseAction(role, path, actionKey) {
  if (role === 'ADMIN') return true
  const allowed = byRoute[path]?.[role] || []
  return allowed.includes(actionKey)
}

export function isKnownAction(actionKey) {
  return allActions.includes(actionKey)
}
