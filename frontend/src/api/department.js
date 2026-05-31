import { createCrud } from './request'
import request from './request'
const api = createCrud('/departments')
api.enable = (id) => request.post(`/departments/${id}/enable`)
api.disable = (id) => request.post(`/departments/${id}/disable`)
export default api
