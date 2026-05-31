import { createCrud } from './request'
import request from './request'
const api = createCrud('/nursing/executions')
api.execute = (id, data) => request.post(`/nursing/executions/${id}/execute`, data || {})
api.fail = (id, data) => request.post(`/nursing/executions/${id}/fail`, data || {})
api.cancel = (id, data) => request.post(`/nursing/executions/${id}/cancel`, data || {})
export default api
