import { createCrud } from './request'
import request from './request'
const api = createCrud('/pharmacy/dispenses')
api.dispense = (id, data) => request.post(`/pharmacy/dispenses/${id}/dispense`, data || {})
api.returnDrug = (id, data) => request.post(`/pharmacy/dispenses/${id}/return`, data || {})
api.cancel = (id, data) => request.post(`/pharmacy/dispenses/${id}/cancel`, data || {})
export default api
