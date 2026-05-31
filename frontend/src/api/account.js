import request from './request'

export default {
  summary(admissionId) {
    return request.get(`/inpatient-accounts/${admissionId}/summary`)
  }
}
