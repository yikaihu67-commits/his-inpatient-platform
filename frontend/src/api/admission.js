import { createCrud } from './request'
import request from './request'
const api = createCrud('/admissions')
api.admit = (id) => request.post(`/admissions/${id}/admit`)
api.cancel = (id) => request.post(`/admissions/${id}/cancel`)
export default api
