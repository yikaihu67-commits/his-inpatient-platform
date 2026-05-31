import { createCrud } from './request'
import request from './request'
const api = createCrud('/medical-records')
api.submit = (id, data) => request.post(`/medical-records/${id}/submit`, data || {})
api.review = (id, data) => request.post(`/medical-records/${id}/review`, data || {})
api.archive = (id, data) => request.post(`/medical-records/${id}/archive`, data || {})
api.cancel = (id, data) => request.post(`/medical-records/${id}/cancel`, data || {})
export default api
