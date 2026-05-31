import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.success === 'boolean') {
      if (body.success) return body.data
      const message = body.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const backendMessage = error.response?.data?.message
    const message = backendMessage || (status >= 500 ? '服务器处理失败，请稍后重试或联系管理员' : error.message || '网络异常')
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

export function createCrud(base) {
  return {
    list(params) {
      return service.get(base, { params })
    },
    detail(id) {
      return service.get(`${base}/${id}`)
    },
    create(data) {
      return service.post(base, data)
    },
    update(id, data) {
      return service.put(`${base}/${id}`, data)
    },
    remove(id) {
      return service.delete(`${base}/${id}`)
    }
  }
}
