import { createCrud } from './request'
import request from './request'
const api = createCrud('/exam-lab-requests')
api.submit = (id, data) => request.post(`/exam-lab-requests/${id}/submit`, data || {})
api.schedule = (id, data) => request.post(`/exam-lab-requests/${id}/schedule`, data || {})
api.complete = (id, data) => request.post(`/exam-lab-requests/${id}/complete`, data || {})
api.report = (id, data) => request.post(`/exam-lab-requests/${id}/report`, data || {})
api.bill = (id, data) => request.post(`/exam-lab-requests/${id}/bill`, data || {})
api.cancel = (id, data) => request.post(`/exam-lab-requests/${id}/cancel`, data || {})
export default api
