const key = 'kioskSession'

export function getKioskSession() {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setKioskSession(session) {
  localStorage.setItem(key, JSON.stringify(session || {}))
}

export function clearKioskSession() {
  localStorage.removeItem(key)
}

export function sessionParams() {
  const session = getKioskSession()
  if (!session?.patientId || !session?.admissionId) return null
  return { patientId: session.patientId, admissionId: session.admissionId }
}
