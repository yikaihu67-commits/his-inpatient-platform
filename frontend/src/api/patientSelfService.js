import request from './request'

const base = '/patient-self-service'

export default {
  verify(data) {
    return request.post(`${base}/verify`, data)
  },
  admissionInfo(params) {
    return request.get(`${base}/admission-info`, { params })
  },
  billSummary(params) {
    return request.get(`${base}/bill-summary`, { params })
  },
  billDetails(params) {
    return request.get(`${base}/bill-details`, { params })
  },
  examLabs(params) {
    return request.get(`${base}/exam-labs`, { params })
  },
  surgeries(params) {
    return request.get(`${base}/surgeries`, { params })
  },
  discharge(params) {
    return request.get(`${base}/discharge`, { params })
  }
}
