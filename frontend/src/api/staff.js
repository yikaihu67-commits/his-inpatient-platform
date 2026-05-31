import { createCrud } from './request'
import request from './request'
const api = createCrud('/staff')
api.enable = (id) => request.post(`/staff/${id}/enable`)
api.disable = (id) => request.post(`/staff/${id}/disable`)
export default api
