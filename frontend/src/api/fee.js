import { createCrud } from './request'
import request from './request'
const api = createCrud('/fees')
api.cancel = (id) => request.post(`/fees/${id}/cancel`)
api.summary = (admissionId) => request.get(`/fees/admission/${admissionId}/summary`)
export default api
