import { createCrud } from './request'
import request from './request'
const api = createCrud('/dict-items')
api.byType = (dictType) => request.get(`/dict-items/type/${dictType}`)
export default api
