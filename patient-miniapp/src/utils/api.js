import { API_BASE } from './config'

function normalizeError(body, fallback) {
  if (body?.message) return body.message
  if (typeof body === 'string') return body
  return fallback
}

function request(path, options = {}) {
  const url = API_BASE + path
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header: { 'content-type': 'application/json; charset=utf-8' },
      success(res) {
        const body = res.data
        if (res.statusCode >= 400 || body?.success === false) {
          const message = normalizeError(body, res.statusCode >= 500 ? '服务器处理失败，请稍后重试' : '请求失败')
          uni.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
          return
        }
        resolve(body?.data ?? body)
      },
      fail(err) {
        uni.showToast({ title: '网络异常，请检查服务地址', icon: 'none' })
        reject(err)
      }
    })
  })
}

function query(data = {}) {
  return Object.entries(data)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')
}

export const api = {
  login(data) {
    return request('/patient-mobile/login', { method: 'POST', data })
  },
  profile(params) {
    return request(`/patient-mobile/profile?${query(params)}`)
  },
  billSummary(params) {
    return request(`/patient-mobile/bill-summary?${query(params)}`)
  },
  billDetails(params) {
    return request(`/patient-mobile/bill-details?${query(params)}`)
  },
  appointments(params) {
    return request(`/patient-mobile/appointments?${query(params)}`)
  },
  createAppointment(data) {
    return request('/patient-mobile/appointments', { method: 'POST', data })
  },
  cancelAppointment(appointmentId) {
    return request(`/patient-mobile/appointments/cancel?appointmentId=${appointmentId}`, {
      method: 'POST',
      data: { remark: '患者小程序取消预约' }
    })
  },
  reports(params) {
    return request(`/patient-mobile/exam-lab?${query(params)}`)
  },
  surgeries(params) {
    return request(`/patient-mobile/surgery?${query(params)}`)
  },
  discharge(params) {
    return request(`/patient-mobile/discharge?${query(params)}`)
  }
}
