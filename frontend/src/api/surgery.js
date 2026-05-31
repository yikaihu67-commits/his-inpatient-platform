import { createCrud } from './request'
import request from './request'

const api = createCrud('/surgeries')

api.listByPatient = (patientId, params) => request.get(`/surgeries/patient/${patientId}`, { params })
api.listByAdmission = (admissionId, params) => request.get(`/surgeries/admission/${admissionId}`, { params })
api.schedule = (id, data) => request.post(`/surgeries/${id}/schedule`, data || {})
api.start = (id, data) => request.post(`/surgeries/${id}/start`, data || {})
api.complete = (id, data) => request.post(`/surgeries/${id}/complete`, data || {})
api.cancel = (id, data) => request.post(`/surgeries/${id}/cancel`, data || {})
api.bill = (id, data) => request.post(`/surgeries/${id}/bill`, data || {})

export default api
