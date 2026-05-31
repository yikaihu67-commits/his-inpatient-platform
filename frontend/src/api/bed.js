import { createCrud } from './request'
import request from './request'
const api = createCrud('/beds')
api.assign = (id, data) => request.post(`/beds/${id}/assign`, data)
api.release = (id) => request.post(`/beds/${id}/release`)
export default api
