import request from './request'

const base = '/patient-mobile'

export default {
  login(data) {
    return request.post(`${base}/login`, data)
  },
  home(params) {
    return request.get(`${base}/home`, { params })
  },
  billSummary(params) {
    return request.get(`${base}/bill-summary`, { params })
  },
  billDetails(params) {
    return request.get(`${base}/bill-details`, { params })
  },
  appointments(params) {
    return request.get(`${base}/appointments`, { params })
  },
  createAppointment(data) {
    return request.post(`${base}/appointments`, data)
  },
  cancelAppointment(appointmentId, data) {
    return request.post(`${base}/appointments/cancel`, data || {}, { params: { appointmentId } })
  },
  reports(params) {
    return request.get(`${base}/reports`, { params })
  },
  surgeries(params) {
    return request.get(`${base}/surgeries`, { params })
  },
  discharge(params) {
    return request.get(`${base}/discharge`, { params })
  }
}
