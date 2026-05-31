let base = import.meta.env.VITE_API_BASE || '/api'

// #ifdef MP-WEIXIN
base = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'
// #endif

export const API_BASE = base.replace(/\/$/, '')
