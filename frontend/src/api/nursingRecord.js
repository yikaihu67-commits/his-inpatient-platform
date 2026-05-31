import { createCrud } from './request'
import request from './request'

const api = createCrud('/nursing/records')
api.listByPatient = (patientId, params) => request.get(`/nursing/records/patient/${patientId}`, { params })
api.listByAdmission = (admissionId, params) => request.get(`/nursing/records/admission/${admissionId}`, { params })
api.execute = (id, data) => request.post(`/nursing/records/${id}/execute`, data || {})
api.review = (id, data) => request.post(`/nursing/records/${id}/review`, data || {})
api.cancel = (id, data) => request.post(`/nursing/records/${id}/cancel`, data || {})
api.bill = (id, data) => request.post(`/nursing/records/${id}/bill`, data || {})

export default api
