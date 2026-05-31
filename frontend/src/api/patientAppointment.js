import { createCrud } from './request'
import request from './request'

const api = createCrud('/patient-appointments')
api.confirm = (id, data) => request.post(`/patient-appointments/${id}/confirm`, data || {})
api.complete = (id, data) => request.post(`/patient-appointments/${id}/complete`, data || {})
api.cancel = (id, data) => request.post(`/patient-appointments/${id}/cancel`, data || {})
export default api
