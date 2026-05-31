import request from './request'
const api = {
  list(params) {
    return request.get('/deposits', { params })
  },
  detail(id) {
    return request.get(`/deposits/${id}`)
  },
  create(data) {
    return request.post('/deposits/pay', data)
  },
  update(id, data) {
    return request.post('/deposits/pay', data)
  },
  remove(id) {
    return request.post(`/deposits/${id}/cancel`)
  },
  pay(data) {
    return request.post('/deposits/pay', data)
  },
  refund(data) {
    return request.post('/deposits/refund', data)
  },
  cancel(id) {
    return request.post(`/deposits/${id}/cancel`)
  },
  summary(admissionId) {
    return request.get(`/deposits/admission/${admissionId}/summary`)
  }
}
export default api
