import { ref, computed } from 'vue'
import { login as apiLogin, getMe } from '../services/api'

const token = ref(localStorage.getItem('token') || null)
const userEmail = ref(localStorage.getItem('userEmail') || null)
const isDeveloper = ref(localStorage.getItem('isDeveloper') === 'true')
const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value)

  async function login(email, password) {
    const response = await apiLogin(email, password)
    token.value = response.data.access_token
    localStorage.setItem('token', token.value)

    // Fetch the user's classification (developer vs plain) from the backend,
    // which reads it from the Keycloak token's client roles.
    const me = (await getMe()).data
    userEmail.value = me.email || email
    roles.value = me.roles || []
    isDeveloper.value = !!me.is_developer
    localStorage.setItem('userEmail', userEmail.value)
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('isDeveloper', String(isDeveloper.value))

    return response
  }

  function logout() {
    token.value = null
    userEmail.value = null
    roles.value = []
    isDeveloper.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('roles')
    localStorage.removeItem('isDeveloper')
  }

  return { isAuthenticated, login, logout, userEmail, isDeveloper, roles }
}
