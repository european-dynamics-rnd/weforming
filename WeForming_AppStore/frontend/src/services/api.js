import axios from 'axios'

export const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8000'

const api = axios.create({
  baseURL: API_BASE,
})

// Absolute URL for an app's DB-served icon, or '' when it has none.
export const iconUrl = (app) =>
  app && app.icon_url ? `${API_BASE}${app.icon_url}` : ''

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Clear the session and bounce to login when the token is rejected/expired.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userEmail')
      localStorage.removeItem('isDeveloper')
      localStorage.removeItem('roles')
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  }
)


export const searchApps = (query) => {
  return api.get('/api/apps/search/', { params: { q: query } })
}

export const getAppDetails = (id) => {
  return api.get(`/api/apps/${id}`)
}

export const login = (email, password) => {
  const body = new URLSearchParams({ username: email, password })
  return api.post('/api/token', body, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

export const getMe = () => {
  return api.get('/api/me')
}

export const createApp = (formData) => {
  return api.post('/api/apps/', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getApps = () => {
  return api.get('/api/apps/')
}

export const getApp = (id) => {
  return api.get(`/api/apps/${id}`)
}

export const getAssets = () => {
  return api.get('/api/assets/')
}

export const createAsset = (formData) => {
  return api.post('/api/assets/', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const updateAsset = (id, formData) => {
  return api.put(`/api/assets/${id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const deleteAsset = (id) => {
  return api.delete(`/api/assets/${id}`)
}

export const getPlatformTypes = () => {
  return api.get('/api/platform-types/')
}

// Absolute URL for a house's DB-served photo, or '' when it has none.
export const assetImageUrl = (asset) =>
  asset && asset.image_url ? `${API_BASE}${asset.image_url}` : ''

export const getUserApps = () => {
  return api.get('/api/user-apps/')
}

export const installApp = (appId, assetId, jsonConfig, installedVersion) => {
  return api.post('/api/user-apps/', {
    app_id: appId,
    asset_id: assetId,
    json_config: JSON.stringify(jsonConfig),
    installed_version: installedVersion,
  })
}

export const getInstallStatus = (installUuid) => {
  return api.get(`/api/installs/${installUuid}`)
}
