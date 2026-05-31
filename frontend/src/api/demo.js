import request from './request'

export default {
  reset() {
    return request.post('/demo/reset')
  }
}
