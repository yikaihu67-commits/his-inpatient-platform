const key = 'patientMiniappSession'

export function getSession() {
  return uni.getStorageSync(key) || null
}

export function setSession(value) {
  uni.setStorageSync(key, value)
}

export function clearSession() {
  uni.removeStorageSync(key)
}

export function params() {
  const session = getSession()
  const patient = session?.patient
  if (!patient?.patientId || !patient?.admissionId) return null
  return { patientId: patient.patientId, admissionId: patient.admissionId }
}
