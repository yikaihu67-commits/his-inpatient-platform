import request from './request'
const api = {
  list(params) {
    return request.get('/discharges', { params })
  },
  detail(id) {
    return request.get(`/discharges/${id}`)
  },
  create(data) {
    return request.post('/discharges/create', data)
  },
  update(id, data) {
    return request.put(`/discharges/${id}`, data)
  },
  remove(id) {
    return request.delete(`/discharges/${id}`)
  },
  settle(id, data) {
    return request.post(`/discharges/${id}/settle`, data || {})
  },
  cancel(id, data) {
    return request.post(`/discharges/${id}/cancel`, data || {})
  }
}
export default api
