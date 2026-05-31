import { createCrud } from './request'
import request from './request'
const api = createCrud('/orders')
api.submit = (id) => request.post(`/orders/${id}/submit`)
api.check = (id, data) => request.post(`/orders/${id}/check`, data || {})
api.execute = (id, data) => request.post(`/orders/${id}/execute`, data || {})
api.bill = (id, data) => request.post(`/orders/${id}/bill`, data || {})
api.stop = (id) => request.post(`/orders/${id}/stop`)
api.cancel = (id) => request.post(`/orders/${id}/cancel`)
export default api
