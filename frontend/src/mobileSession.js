const key = 'patientMobileSession'

export function getMobileSession() {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setMobileSession(session) {
  localStorage.setItem(key, JSON.stringify(session || {}))
}

export function clearMobileSession() {
  localStorage.removeItem(key)
}

export function mobileParams() {
  const session = getMobileSession()
  const patient = session?.patient
  if (!patient?.patientId || !patient?.admissionId) return null
  return { patientId: patient.patientId, admissionId: patient.admissionId }
}
