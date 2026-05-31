import request from './request'

export default {
  summary() {
    return request.get('/dashboard/summary')
  }
}
